package uk.ac.manchester.cs.owl.entailments.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.*;

/**
 * Created by
 * User: Samantha Bail
 * Date: 29/12/2011
 * Time: 15:12
 * The University of Manchester
 */


public class OwlOntologyLabeller {

    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private OWLDataFactory df = OWLManager.getOWLDataFactory();
    private int idCount = 1;
    private String prefix = "sat"; // default prefix if we don't chose any other
    private String altPrefix = "bug";

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private String getPrefix(OWLAxiom ax) {
        if (ax instanceof OWLSubClassOfAxiom) {
            OWLSubClassOfAxiom subcls = (OWLSubClassOfAxiom) ax;
            if (subcls.getSuperClass().isOWLNothing()) {
                return altPrefix;
            }
        }
        return prefix;
    }

    /**
     * labels all the logical axioms in the ontology
     *
     * @param ont the ontology to be labelled
     * @return the ontology with labelled logical axioms
     * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
     *
     */

    public OWLOntology labelOntology(OWLOntology ont) throws OWLOntologyCreationException {
        IRI iri = ont.getOntologyID().getOntologyIRI();
        OWLOntology labelledOnt = manager.createOntology(iri);
        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        for (OWLAxiom ax : ont.getAxioms()) {
            if (ax.isLogicalAxiom()) {
                OWLAxiom labelled = labelAxiom(ax);
                changes.add(new AddAxiom(labelledOnt, labelled));
            } else {
                changes.add(new AddAxiom(labelledOnt, ax));
            }
        }
        manager.applyChanges(changes);
        return labelledOnt;
    }

    /**
     * does the same as label ontology
     *
     * @param axioms
     * @return
     */
    public Set<OWLAxiom> labelAxioms(Set<OWLAxiom> axioms) {
        Set<OWLAxiom> labelledAxioms = new HashSet<OWLAxiom>();
        List<OWLAxiom> orderedAxioms = new ArrayList<OWLAxiom>();
        orderedAxioms.addAll(axioms);
        Collections.sort(orderedAxioms);

        for (OWLAxiom ax : orderedAxioms) {
            if (ax.isLogicalAxiom()) {
                OWLAxiom labelled = labelAxiom(ax);
                labelledAxioms.add(labelled);
            }
        }
        return labelledAxioms;
    }

    /**
     * adds a label to an axiom
     *
     * @param ax the axiom to be labelled
     * @return the annotated axiom
     */
    private OWLAxiom labelAxiom(OWLAxiom ax) {
        HashSet<OWLAnnotation> annotations = new HashSet<OWLAnnotation>(ax.getAnnotations());
        boolean hasLabel = false;
        for (OWLAnnotation a : annotations) {
            OWLAnnotationProperty prop = df.getOWLAnnotationProperty(Constants.AXIOM_ID_IRI);
            if (a.getProperty().equals(prop)) {
                hasLabel = true;
                break;
            }
        }
        if (!hasLabel) {
            OWLAnnotation id = getNewAxiomIDAnnotation(ax);
            annotations.add(id);
            OWLAxiom labelled = ax.getAxiomWithoutAnnotations();
            labelled = labelled.getAnnotatedAxiom(annotations);
            return labelled;
        }
        return ax;
    }

    /**
     * does what it says on the tin
     *
     * @return a new OWLAnnotation with the next id and the prefix
     */
    private OWLAnnotation getNewAxiomIDAnnotation(OWLAxiom ax) {
        String id = getPrefix(ax) + Util.pad(idCount, 3);
        idCount++;
        OWLLiteral lit = df.getOWLLiteral(id);
        return df.getOWLAnnotation(df.getOWLAnnotationProperty(Constants.AXIOM_ID_IRI), lit);
    }


    /**
     * @param ax axiom to extract the ID annotation from
     * @return the axiom ID
     */
    public static String getAxiomId(OWLAxiom ax) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = manager.getOWLDataFactory();
        Set<OWLAnnotation> annos = ax.getAnnotations(df.getOWLAnnotationProperty(Constants.AXIOM_ID_IRI));
        if (annos.size() != 0) {
            OWLAnnotation id = annos.iterator().next();
            OWLLiteral val = (OWLLiteral) id.getValue();
            return val.getLiteral();
        } else return null;
    }


}
