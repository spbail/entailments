package uk.ac.manchester.cs.owl.entailments.extractor;

import org.semanticweb.owl.explanation.api.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.entailments.util.Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static uk.ac.manchester.cs.owl.entailments.util.Util.*;

/**
 * Created by
 * User: Samantha Bail
 * Date: 28/01/2012
 * Time: 22:16
 * The University of Manchester
 */


public class EntailmentExtractor {

    private static final IRI SOURCE_ONTOLOGY_IRI = IRI.create("http://owl.cs.manchester.ac.uk/ontology#sourceOntology");
    private OWLReasonerFactory rf;
    private Properties conf;
    private OWLOntology mergedOntology;
    private Set<OWLAxiom> blacklist;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();
    private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * constructor
     * @param ontology
     * @param conf
     */
    public EntailmentExtractor(OWLOntology ontology, Properties conf) {
        this.mergedOntology = getMergedOntology(ontology);
        setConfig(conf);
    }

    public OWLOntology getMergedOntology() {
        return mergedOntology;
    }


    /**
     * sets the config
     * @param conf
     */
    public void setConfig(Properties conf) {
        this.rf = getReasonerFactory(conf.getProperty("reasoner"));
        this.conf = conf;
        blacklist = new HashSet<OWLAxiom>();
    }


    /**
     * Computes entailments from a given OWL ontology
     * @return a set of {@link org.semanticweb.owlapi.model.OWLAxiom}
     */
    public Set<OWLAxiom> getEntailments() {
        logger.info("creating reasoner " + rf.getReasonerName());
        OWLReasoner reasoner = rf.createReasoner(mergedOntology);
        logger.info("done creating reasoner.");

        Set<OWLAxiom> entailments = new HashSet<OWLAxiom>();
        if (reasoner.isConsistent()) {
            // this gets us the entailmented axioms without annotations
            // if the axiom already exists in the ontology, we have to add its annotations back
            try {
                entailments = computeEntailmentsForConsistentOntology(reasoner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            logger.severe("ontology is inconsistent.");
        }
        return entailments;
    }


    /**
     * computes entailments the same way as the InferredAxiomGenerator does
     * @param reasoner reasoner to use
     * @return a set of entailments
     */
    private Set<OWLAxiom> computeEntailmentsForConsistentOntology(OWLReasoner reasoner) {
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();

        // compute entailments first
        for (OWLClass cl : mergedOntology.getClassesInSignature()) {
            if (conf.getProperty("includeAtomicSubs").equals("true")) {
                addAtomicSubsumptionAxioms(cl, reasoner, result);
            } else if (conf.getProperty("includeAtomicEquiv").equals("true")) {
                addAtomicEquivalentClassesAxioms(cl, reasoner, result);
            } else if (conf.getProperty("includeUnsatClasses").equals("true")) {
                addUnsatisfiableClasses(cl, reasoner, result);
            }
        }

        // remove asserted
        if (conf.getProperty("includeAsserted").equals("false")) {
            removeAssertedAxioms(result);
        }

        // add or remove nonstrict subs
        if (conf.getProperty("includeNonStrict").equals("false")) {
            // removal just in case the reasoner adds non-strict subsumptions by default. who knows!
            removeNonStrictAtomicSubsumptions(result, reasoner);
        } else {
            // adding something here is ok - will only add direct subsumptions and
            // no asserted because they're already blacklisted (if)
            addNonStrictAtomicSubsumptions(result, reasoner);
        }

        boolean includeImported = Boolean.parseBoolean(conf.getProperty("includeImported"));
        boolean includeMixed = Boolean.parseBoolean(conf.getProperty("includeMixed"));
        boolean includeNative = Boolean.parseBoolean(conf.getProperty("includeNative"));
//
        if (includeNative) {
            if (!includeImported && !includeMixed) {
                removeNonNativeEntailments(result);
            } else if (!includeImported && includeMixed) {
                removeImportedEntailments(result);
            }
        }
        return result;
    }

    private void removeMixedEntailments(Set<OWLAxiom> result) {
        // TODO: implement - not really important though
    }

    private void removeNativeEntailments(Set<OWLAxiom> result) {
        // TODO: implement - not really important though

    }

    private void removeImportedEntailments(Set<OWLAxiom> result) {
        Set<OWLAxiom> removals = new HashSet<OWLAxiom>();

        for (OWLAxiom ax : result) {
            if (isImportedEntailment(ax)) {
                removals.add(ax);
            }
        }
        System.out.println("");
        result.removeAll(removals);
    }

    private boolean isImportedEntailment(OWLAxiom ax) {
        int importedJustifications = 0;
        int totalJustifications = 0;
        for (Explanation<OWLAxiom> ex : computeJustifications(ax)) {
            IRI rootIRI = mergedOntology.getOntologyID().getOntologyIRI();
            Set<IRI> ontologyIRIs = getOntologyIRIs(ex);
            // if the set of ontology IRIs doesn't at least contain the rootIRI, we have an imported justification
            if (!ontologyIRIs.contains(rootIRI)) {
                importedJustifications++;
            }
            totalJustifications++;
        }
        return (importedJustifications == totalJustifications);
    }


    /**
     * @param result
     */
    private void removeNonNativeEntailments(Set<OWLAxiom> result) {
        Set<OWLAxiom> removals = new HashSet<OWLAxiom>();

        for (OWLAxiom ax : result) {
            if (!isNativeEntailment(ax)) {
                removals.add(ax);
            }
        }
        System.out.println("");
        result.removeAll(removals);
    }


    /**
     * @param ax
     * @return
     */
    private boolean isNativeEntailment(OWLAxiom ax) {

        for (Explanation<OWLAxiom> ex : computeJustifications(ax)) {
            IRI rootIRI = mergedOntology.getOntologyID().getOntologyIRI();
            Set<IRI> ontologyIRIs = getOntologyIRIs(ex);
            // if the set of ontology IRIs isn't just the root IRI
            if (!ontologyIRIs.equals(Collections.singleton(rootIRI))) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param entailment
     * @return
     */
    public Set<Explanation<OWLAxiom>> computeJustifications(OWLAxiom entailment) {
        ExplanationGeneratorFactory<OWLAxiom> genFac = ExplanationManager.createExplanationGeneratorFactory(rf);
        ExplanationProgressMonitor<OWLAxiom> mon = new QuietProgressMonitor();
        ExplanationGenerator<OWLAxiom> exGen = genFac.createExplanationGenerator(mergedOntology, mon);
        Set<Explanation<OWLAxiom>> explanations = new HashSet<Explanation<OWLAxiom>>();
        try {
            explanations = exGen.getExplanations(entailment);
        } catch (ExplanationGeneratorInterruptedException e) {
            e.printStackTrace();
        }
        return explanations;
    }


    /**
     * merges ontology with imports closure and annotates axioms with their source IRI
     * @param ontology
     * @return
     */
    private OWLOntology getMergedOntology(OWLOntology ontology) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        IRI rootIRI = ontology.getOntologyID().getOntologyIRI();
        try {
            OWLOntology merged = manager.createOntology(rootIRI);

            int anonOntologyCount = 1;
            for (OWLOntology ont : ontology.getImportsClosure()) {
                OWLAnnotationProperty sourceOntologyProperty = df.getOWLAnnotationProperty(SOURCE_ONTOLOGY_IRI);
                IRI ontIRI = ont.getOntologyID().getOntologyIRI();
                if (ont.isAnonymous()) {
                    ontIRI = IRI.create("ontology-" + anonOntologyCount);
                }
                OWLAnnotation annotation = df.getOWLAnnotation(sourceOntologyProperty, ontIRI);
                for (OWLAxiom ax : ont.getLogicalAxioms()) {
                    OWLAxiom annotatedAxiom = ax.getAnnotatedAxiom(Collections.singleton(annotation));
                    manager.addAxiom(merged, annotatedAxiom);
                }
                anonOntologyCount++;
            }
            return merged;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * collects all IRIs of the axioms in a justification
     * @param ex
     * @return
     */
    private Set<IRI> getOntologyIRIs(Explanation<OWLAxiom> ex) {
        Set<IRI> ontologyIRIs = new HashSet<IRI>();
        OWLAnnotationProperty ontologySourceAnnotationProperty = df.getOWLAnnotationProperty(SOURCE_ONTOLOGY_IRI);
        for (OWLAxiom ax : ex.getAxioms()) {
            for (OWLAnnotation anno : ax.getAnnotations()) {
                OWLAnnotationProperty property = anno.getProperty();
                if (property.equals(ontologySourceAnnotationProperty)) {
                    OWLAnnotationValue value = anno.getValue();
                    if (value instanceof IRI) {
                        IRI iriValue = (IRI) value;
                        ontologyIRIs.add(iriValue);
                    }
                }
            }
        }
        return ontologyIRIs;
    }


    /**
     * adds non-strict subsumptions to an entailment set. Not so useful, but hey, it's in the paper.
     * @param result
     * @param reasoner
     */
    private void addNonStrictAtomicSubsumptions(Set<OWLAxiom> result, OWLReasoner reasoner) {
        for (OWLClass cl : mergedOntology.getClassesInSignature()) {
            if (reasoner.isSatisfiable(cl)) {
                Set<OWLClass> eqCls = reasoner.getEquivalentClasses(cl).getEntitiesMinus(cl);
                for (OWLClass eq : eqCls) {
                    OWLAxiom sc1 = df.getOWLSubClassOfAxiom(cl, eq);
                    OWLAxiom sc2 = df.getOWLSubClassOfAxiom(eq, cl);
                    if (!blacklist.contains(sc1)) {
                        result.add(sc1);
                    }
                    if (!blacklist.contains(sc2)) {
                        result.add(sc2);
                    }
                }
            }
        }
    }

    /**
     * removes all nonstrict subsumptions.
     * @param result
     * @param reasoner
     */
    private void removeNonStrictAtomicSubsumptions(Set<OWLAxiom> result, OWLReasoner reasoner) {
        Set<OWLAxiom> removals = new HashSet<OWLAxiom>();
        for (OWLAxiom ax : result) {
            if (ax instanceof OWLSubClassOfAxiom) {
                OWLSubClassOfAxiom sc = (OWLSubClassOfAxiom) ax;
                OWLEquivalentClassesAxiom eq = df.getOWLEquivalentClassesAxiom(sc.getSubClass(), sc.getSuperClass());
                if (reasoner.isEntailed(eq)) {
                    removals.add(sc);
                    blacklist.add(sc);
                }
            }
        }
        result.removeAll(removals);
    }


    /**
     * removes all asserted subsumptions
     * @param result
     */
    private void removeAssertedAxioms(Set<OWLAxiom> result) {
        for (OWLClass cl : mergedOntology.getClassesInSignature()) {
            for (OWLSubClassOfAxiom ax : mergedOntology.getSubClassAxiomsForSubClass(cl)) {
                result.remove(ax);
                blacklist.add(ax);
            }
        }
    }

    /**
     * adds all atomic subsumption axioms
     * @param cls      the owl class to get superclasses for
     * @param reasoner the reasoner to use
     * @param result   a set of subclass of axioms
     */
    public void addAtomicSubsumptionAxioms(OWLClass cls, OWLReasoner reasoner, Set<OWLAxiom> result) {
        boolean direct = !Boolean.parseBoolean(conf.getProperty("includeIndirect"));
        if (reasoner.isSatisfiable(cls)) {
            Set<OWLClass> superClasses = reasoner.getSuperClasses(cls, direct).getFlattened();
            for (OWLClass sup : superClasses) {
                boolean includeTop = Boolean.parseBoolean(conf.getProperty("includeTop"));
                OWLAxiom sc = df.getOWLSubClassOfAxiom(cls, sup);
                if (!blacklist.contains(sc)) {
                    // if the superclass isn't Top, just add the axiom
                    if (!sup.isOWLThing()) {
                        result.add(sc);
                    } else if (sup.isOWLThing() && includeTop && superClasses.size() == 1) {
                        // else, if we only have Top and allow including Top, add it to the list
                        result.add(sc);
                    }
                }
            }
        }
    }

    /**
     * adds unsatisfiable classes
     * @param entity
     * @param reasoner
     * @param result
     */
    public void addUnsatisfiableClasses(OWLClass entity, OWLReasoner reasoner, Set<OWLAxiom> result) {
        if (reasoner.isSatisfiable(entity)) {
            OWLAxiom sc = df.getOWLSubClassOfAxiom(entity, df.getOWLNothing());
            if (!blacklist.contains(sc)) {
                result.add(sc);
            }
        }
    }

    /**
     * adds atomic equivalent classes.
     * @param reasoner
     * @param result
     */
    public void addAtomicEquivalentClassesAxioms(OWLClass cls, OWLReasoner reasoner, Set<OWLAxiom> result) {
        if (reasoner.isSatisfiable(cls)) {
            Set<OWLClass> eqClasses = reasoner.getEquivalentClasses(cls).getEntitiesMinus(cls);
            for (OWLClass sup : eqClasses) {
                OWLAxiom ax = df.getOWLEquivalentClassesAxiom(cls, sup);
                if (!blacklist.contains(ax)) {
                    result.add(ax);
                }
            }
        }
    }


}
