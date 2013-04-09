package uk.ac.manchester.cs.owl.entailments.test;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLAxiom;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import uk.ac.manchester.cs.owl.entailments.EntailmentExtractorUI;
import uk.ac.manchester.cs.owl.entailments.util.Util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by
 * User: Samantha Bail
 * Date: 08/04/2013
 * Time: 22:54
 * The University of Manchester
 */


public class TestEntailmentExtractorUI {

    private static String ontFile = "/Users/samantha/code/entailments/testfiles/puma.owl";


    public static void main(String[] args) {
        String[] params = {"-i",
                ontFile,
                "-c", "/Users/samantha/code/entailments/testfiles/conf1.txt"};


        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File(ontFile));
            Set<OWLAxiom> axioms = new HashSet<OWLAxiom>(ont.getLogicalAxioms());
            Util.printNumbered(axioms);

            EntailmentExtractorUI.main(params);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }



}
