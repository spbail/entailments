package uk.ac.manchester.cs.owl.entailments.test;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.entailments.EntailmentExtractor;
import uk.ac.manchester.cs.owl.entailments.EntailmentExtractorUI;
import uk.ac.manchester.cs.owl.entailments.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Created by
 * User: Samantha Bail
 * Date: 08/04/2013
 * Time: 22:54
 * The University of Manchester
 */


public class TestEntailmentExtractor {

    private static String ontFile = "/Users/samantha/code/entailments/testfiles/puma.owl";


    @Test
    public void testNoAsserted() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>();

        asserted.addAll(ontology.getAxioms(AxiomType.SUBCLASS_OF));

//        System.out.println("ASSERTED");
//        Util.printNumbered(asserted);
//        System.out.println("");
        Set<OWLAxiom> entailments = getEntailments("jfact", ontology);

        int containsAsserted = 0;

        for (OWLAxiom ax : asserted) {
            if (entailments.contains(ax)) {
                containsAsserted++;
            }
        }

        assertTrue(containsAsserted == 0);
//        Util.printNumbered(entailments);
    }


    @Test
    public void testInferredPlusAssertedIsIncludeAsserted() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>();
        asserted.addAll(ontology.getAxioms(AxiomType.SUBCLASS_OF));


        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }

        conf.setProperty("includeAsserted", "true");
        Set<OWLAxiom> entailmentsIncAs = getEntailments(conf, ontology);

        conf.setProperty("includeAsserted", "false");
        Set<OWLAxiom> entailmentsExAs = getEntailments(conf, ontology);

        Set<OWLAxiom> merged = new HashSet<OWLAxiom>(entailmentsExAs);
        merged.addAll(asserted);

        assertTrue(merged.containsAll(entailmentsIncAs) && (merged.size() == entailmentsIncAs.size()));

    }


    @Test
    public void testNonStrictExcluded() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>();
        asserted.addAll(ontology.getAxioms(AxiomType.SUBCLASS_OF));

        System.out.println("ASSERTED");
        Util.printNumbered(asserted);
        System.out.println("");

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }

        conf.setProperty("includeAsserted", "true");
        conf.setProperty("includeNonStrict", "true");

        Set<OWLAxiom> entailments = getEntailments(conf, ontology);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass co = df.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2011/1/puma.owl#Cougar"));
        OWLClass pu = df.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2011/1/puma.owl#Puma"));
        OWLClass ml = df.getOWLClass(IRI.create("http://www.semanticweb.org/ontologies/2011/1/puma.owl#MountainLion"));

        Set<OWLAxiom> eqCls = new HashSet<OWLAxiom>();
        eqCls.add(df.getOWLSubClassOfAxiom(co, pu));
        eqCls.add(df.getOWLSubClassOfAxiom(co, ml));
        eqCls.add(df.getOWLSubClassOfAxiom(ml, pu));
        eqCls.add(df.getOWLSubClassOfAxiom(ml, co));
        eqCls.add(df.getOWLSubClassOfAxiom(pu, ml));
        eqCls.add(df.getOWLSubClassOfAxiom(pu, co));

        System.out.println("ENTAILMENTS");
        Util.printNumbered(entailments);

//        assertTrue(containsNonStrict==0);

    }


    @Test
    public void testReasonerSameResult() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        Set<OWLAxiom> pelletEnts = getEntailments("pellet", ontology);
        Set<OWLAxiom> hermitEnts = getEntailments("hermit", ontology);
        Set<OWLAxiom> jfactEnts = getEntailments("jfact", ontology);

        assertTrue(pelletEnts.equals(hermitEnts) && pelletEnts.equals(jfactEnts));
        System.out.println("entailment size: " + pelletEnts.size());
    }

    private Set<OWLAxiom> getEntailments(String name, OWLOntology ontology) {
        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        conf.setProperty("reasoner", name);

        try {
            conf.load(new FileInputStream(confFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);
        return ex.getEntailments();

    }

    private Set<OWLAxiom> getEntailments(Properties conf, OWLOntology ontology) {
        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);
        return ex.getEntailments();

    }

}
