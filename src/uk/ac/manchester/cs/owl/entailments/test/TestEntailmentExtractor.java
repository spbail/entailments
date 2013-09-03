package uk.ac.manchester.cs.owl.entailments.test;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.entailments.extractor.EntailmentExtractor;
import uk.ac.manchester.cs.owl.entailments.util.*;

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

    //    private static String ontFile = "/Users/samantha/code/entailments/testfiles/puma.owl";
    private static String ontFile = "/Users/samantha/code/entailments/testfiles/biopax-level3_vLevel3_v1.0.owl";
    // this file has 18 native, 23 native + mixed, 24 native+mixed+imported
    private static String ontFileWithImport = "/Users/samantha/code/entailments/testfiles/puma-root.owl";
    private static String rootIRI = "http://www.semanticweb.org/ontologies/2011/1/puma.owl#";


    @Test
    public void testNativeOnly() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFileWithImport));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }
        conf.setProperty("includeNative", "true");
        conf.setProperty("includeImported", "false");
        conf.setProperty("includeMixed", "false");

//        for (OWLClass c : ontology.getClassesInSignature(true)) {
//            System.out.println(c);
//        }

        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);
        Set<OWLAxiom> entailments = ex.getEntailments();
//        System.out.println(entailments.size());
//        Util.printNumbered(entailments);
        assertTrue(entailments.size() == 18);

    }


    @Test
    public void testIncludeMixed() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFileWithImport));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }
        conf.setProperty("includeAsserted", "true");

        conf.setProperty("includeNative", "true");
        conf.setProperty("includeImported", "false");
        conf.setProperty("includeMixed", "true");

//        for (OWLClass c : ontology.getClassesInSignature(true)) {
//            System.out.println(c);
//        }

        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);
        Set<OWLAxiom> entailments = ex.getEntailments();
//        System.out.println(entailments.size());
//        Util.printNumbered(entailments);
        assertTrue("entailment size is " + entailments.size(), entailments.size() == 23);

    }

    @Test
    public void testNoAsserted() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }
        conf.setProperty("includeAsserted", "false");

        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>();
        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);
        Set<OWLAxiom> entailments = ex.getEntailments();
        asserted.addAll(ex.getMergedOntology().getAxioms(AxiomType.SUBCLASS_OF));

        int containsAsserted = 0;

        for (OWLAxiom ent : entailments) {
            if (asserted.contains(ent.getAxiomWithoutAnnotations())) {
                containsAsserted++;
            }
        }
//        Util.printNumbered(entailments);
        assertTrue(containsAsserted == 0);
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

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Set<OWLAxiom> asserted = new HashSet<OWLAxiom>(ontology.getAxioms(AxiomType.SUBCLASS_OF));
        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);

        conf.setProperty("includeAsserted", "true");
        ex.setConfig(conf);
        Set<OWLAxiom> entailmentsIncAs = ex.getEntailments();

        conf.setProperty("includeAsserted", "false");
        ex.setConfig(conf);
        Set<OWLAxiom> entailmentsExAs = ex.getEntailments();

        Set<OWLAxiom> merged = new HashSet<OWLAxiom>();
        for (OWLAxiom ent : entailmentsExAs) {
            merged.add(ent.getAxiomWithoutAnnotations());
        }
        merged.addAll(asserted);

//        Util.printNumbered(merged);
//
//        Util.printNumbered(entailmentsIncAs);
        assertTrue("merged size: " + merged.size() + " entailmentsIncAs size: " + entailmentsIncAs.size(),
                (merged.containsAll(entailmentsIncAs) && merged.size() == entailmentsIncAs.size()));

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

//        System.out.println("ASSERTED");
//        Util.printNumbered(asserted);
//        System.out.println("");

        String confFile = "/Users/samantha/code/entailments/testfiles/conf_noreasoner.txt";
        Properties conf = new Properties();
        try {
            conf.load(new FileInputStream(confFile));
            conf.setProperty("reasoner", "hermit");
        } catch (IOException e) {
            e.printStackTrace();
        }

        conf.setProperty("includeAsserted", "false");
        conf.setProperty("includeNonStrict", "false");

        Set<OWLAxiom> entailments = getEntailments(conf, ontology);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass co = df.getOWLClass(IRI.create(rootIRI + "Cougar"));
        OWLClass pu = df.getOWLClass(IRI.create(rootIRI + "Puma"));
        OWLClass ml = df.getOWLClass(IRI.create(rootIRI + "MountainLion"));

        Set<OWLAxiom> eqCls = new HashSet<OWLAxiom>();
        eqCls.add(df.getOWLSubClassOfAxiom(co, pu));
        eqCls.add(df.getOWLSubClassOfAxiom(co, ml));
        eqCls.add(df.getOWLSubClassOfAxiom(ml, pu));
        eqCls.add(df.getOWLSubClassOfAxiom(ml, co));
        eqCls.add(df.getOWLSubClassOfAxiom(pu, ml));
        eqCls.add(df.getOWLSubClassOfAxiom(pu, co));

        int nonstrict = 0;

        for (OWLAxiom ax : entailments) {
            if (eqCls.contains(ax.getAxiomWithoutAnnotations())) {
                nonstrict++;
            }
        }

        assertTrue("contains " + nonstrict + " nonstrict subsumptions.", nonstrict == 0);
    }


    /**
     * test whether indirect subsumptions are correctly included/excluded
     */
    @Test
    public void testDirectIndirect() {
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
        conf.setProperty("includeNonStrict", "false");
        conf.setProperty("includeIndirect", "false");

        Set<OWLAxiom> entailments = getEntailments(conf, ontology);
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass co = df.getOWLClass(IRI.create(rootIRI + "Cougar"));
        OWLClass pu = df.getOWLClass(IRI.create(rootIRI + "Puma"));
        OWLClass ml = df.getOWLClass(IRI.create(rootIRI + "MountainLion"));
        OWLClass cat = df.getOWLClass(IRI.create(rootIRI + "Cat"));
        OWLClass mam = df.getOWLClass(IRI.create(rootIRI + "Mammal"));
        OWLClass an = df.getOWLClass(IRI.create(rootIRI + "Animal"));
        OWLClass bp = df.getOWLClass(IRI.create(rootIRI + "BabyPuma"));

        Set<OWLAxiom> directsubs = new HashSet<OWLAxiom>();
        directsubs.add(df.getOWLSubClassOfAxiom(bp, pu));
        directsubs.add(df.getOWLSubClassOfAxiom(bp, co));
        directsubs.add(df.getOWLSubClassOfAxiom(bp, ml));
        directsubs.add(df.getOWLSubClassOfAxiom(ml, cat));
        directsubs.add(df.getOWLSubClassOfAxiom(pu, cat));
        directsubs.add(df.getOWLSubClassOfAxiom(co, cat));
        directsubs.add(df.getOWLSubClassOfAxiom(cat, mam));
        directsubs.add(df.getOWLSubClassOfAxiom(mam, an));

        int indirect = 0;
        for (OWLAxiom ax : entailments) {
            if (!directsubs.contains(ax.getAxiomWithoutAnnotations())) {
                indirect++;
            }
        }

//        System.out.println("ENTAILMENTS");
//        Util.printNumbered(entailments);

//        assertTrue(entailments.size() == 24);
        assertTrue("contains " + indirect + " indirect subsumptions.", indirect == 0);

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
