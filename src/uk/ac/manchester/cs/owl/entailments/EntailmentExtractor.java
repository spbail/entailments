package uk.ac.manchester.cs.owl.entailments;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.entailments.util.Util;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * Created by
 * User: Samantha Bail
 * Date: 28/01/2012
 * Time: 22:16
 * The University of Manchester
 */


public class EntailmentExtractor {

    private OWLReasonerFactory rf;
    private Properties conf;
    private OWLOntology ontology;
    private Set<OWLAxiom> blacklist;
    private OWLDataFactory df = OWLManager.getOWLDataFactory();
    private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public EntailmentExtractor(OWLOntology ontology, Properties conf) {
        this.ontology = ontology;
        setConfig(conf);
    }

    public void setConfig(Properties conf) {
        this.rf = Util.getReasonerFactory(conf.getProperty("reasoner"));
        this.conf = conf;
        blacklist = new HashSet<OWLAxiom>();
    }

    /**
     * Computes entailments from a given OWL ontology
     * @return a set of {@link org.semanticweb.owlapi.model.OWLAxiom}
     */
    public Set<OWLAxiom> getEntailments() {
        logger.info("creating reasoner " + rf.getReasonerName());
        OWLReasoner reasoner = rf.createReasoner(ontology);
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
        for (OWLClass cl : ontology.getClassesInSignature()) {
            if (conf.getProperty("includeAtomicSubs").equals("true")) {
                addAtomicSubsumptionAxioms(cl, reasoner, result);
            } else if (conf.getProperty("includeAtomicEquiv").equals("true")) {
                addAtomicSubsumptionAxioms(cl, reasoner, result);
            } else if (conf.getProperty("includeUnsatClasses").equals("true")) {
                addUnsatisfiableClasses(cl, reasoner, result);
            }
        }

        // add or remove asserted
        if (conf.getProperty("includeAsserted").equals("true")) {
            addAssertedSubsumptions(result);
        } else {
            removeAssertedSubsumptions(result);
        }

        if (conf.getProperty("includeNonStrict").equals("false")) {
            removeNonStrictSubsumptions(result, reasoner);
        } else {
            addNonStrictSubsumptions(result, reasoner);
        }

        if (conf.getProperty("includeImported").equals("false")) {
            if (conf.getProperty("includeMixed").equals("false")) {
                removeNonNativeEntailments(result, reasoner);
            } else {
                removeImportedEntailments(result, reasoner);
            }
        } else {
            if (conf.getProperty("includeMixed").equals("true")) {
                removeNativeEntailments(result, reasoner);
            } else {
                removeMixedEntailments(result, reasoner);
            }
        }


        return result;
    }

    private void removeMixedEntailments(Set<OWLAxiom> result, OWLReasoner reasoner) {
        // TODO: implement - not really important though
    }

    private void removeNativeEntailments(Set<OWLAxiom> result, OWLReasoner reasoner) {
        // TODO: implement - not really important though

    }

    private void removeImportedEntailments(Set<OWLAxiom> result, OWLReasoner reasoner) {
        // TODO: implement - 2nd priority
    }

    private void removeNonNativeEntailments(Set<OWLAxiom> result, OWLReasoner reasoner) {
        // TODO: implement - 1st priority, this is the most relevant one for now
    }

    private void addNonStrictSubsumptions(Set<OWLAxiom> result, OWLReasoner reasoner) {
        for (OWLClass cl : ontology.getClassesInSignature()) {
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

    private void removeNonStrictSubsumptions(Set<OWLAxiom> result, OWLReasoner reasoner) {
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

    private void addAssertedSubsumptions(Set<OWLAxiom> result) {
        for (OWLClass cl : ontology.getClassesInSignature()) {
            for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(cl)) {
                if (!blacklist.contains(ax)) {
                    result.add(ax);
                }
            }
        }
    }

    private void removeAssertedSubsumptions(Set<OWLAxiom> result) {
        for (OWLClass cl : ontology.getClassesInSignature()) {
            for (OWLSubClassOfAxiom ax : ontology.getSubClassAxiomsForSubClass(cl)) {
                result.remove(ax);
                blacklist.add(ax);
            }
        }
    }

    /**
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

    public void addUnsatisfiableClasses(OWLClass entity, OWLReasoner reasoner, Set<OWLAxiom> result) {
        if (reasoner.isSatisfiable(entity)) {
            OWLAxiom sc = df.getOWLSubClassOfAxiom(entity, df.getOWLNothing());
            if (!blacklist.contains(sc)) {
                result.add(sc);
            }
        }
    }

    public void addAtomicEquivalentClassesAxioms(OWLClass entity, OWLReasoner reasoner, Set<OWLAxiom> result) {

    }

    /**
     * @param ontology the ontology
     * @return the set of subclassof axioms in the ontology
     */
    private Set<OWLAxiom> getAssertedEntailments(OWLOntology ontology) {
        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>();
        for (OWLSubClassOfAxiom ax : ontology.getAxioms(AxiomType.SUBCLASS_OF)) {
            // if we have an atomic subsumption
            if (!ax.getSubClass().isAnonymous() && !ax.getSuperClass().isAnonymous()) {
                asserted.add(ax);
            }
        }
        return asserted;
    }


    /**
     * takes a set of entailments and removes self-supporting axioms and axioms of type A sub Top
     * @param entailments
     * @return
     */
    private Set<OWLAxiom> getPrunedEntailments(Set<OWLAxiom> entailments) {
        Set<OWLAxiom> pruned = new HashSet<OWLAxiom>();
        for (OWLAxiom ax : entailments) {
            if (ax.isLogicalAxiom()) {
                if (!isTautology(ax)) {
                    pruned.add(ax);
                }
            } else {
                pruned.add(ax);
            }
        }
        return pruned;
    }

    /**
     * tests whether the axiom is of type
     * A sub Top
     * Bottom sub A
     * A sub A
     * @param ax the axiom to test
     * @return true if none of the tests holds, false otherwise
     */
    private boolean isTautology(OWLAxiom ax) {
        OWLSubClassOfAxiom sc = (OWLSubClassOfAxiom) ax;
        if (sc.getSuperClass().equals(df.getOWLThing())) {
            return true;
        }
        if (sc.getSubClass().equals(df.getOWLNothing())) {
            return true;
        }
        if (sc.getSubClass().equals(sc.getSuperClass())) {
            return true;
        }
        return false;
    }


}
