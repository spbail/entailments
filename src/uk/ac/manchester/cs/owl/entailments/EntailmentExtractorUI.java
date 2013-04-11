package uk.ac.manchester.cs.owl.entailments;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.entailments.extractor.EntailmentExtractor;
import uk.ac.manchester.cs.owl.entailments.util.Util;

import java.io.*;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static uk.ac.manchester.cs.owl.entailments.util.Util.*;

/**
 * Created by
 * User: Samantha Bail
 * Date: 02/04/2012
 * Time: 15:49
 * The University of Manchester
 */


public class EntailmentExtractorUI {

    private static File inputFile;
    private static File outputFile;
    private static File configFile;
    private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public static void main(String[] args) throws OWLOntologyCreationException {

        logger.setLevel(Level.INFO);

        readArgs(args);
        Properties conf = new Properties();
        if (configFile == null) {
            conf = loadDefaultConfig();
            logger.info("using default configuration.");

        } else {
            try {
                conf.load(new FileInputStream(configFile));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputFile);

        EntailmentExtractor ex = new EntailmentExtractor(ontology, conf);

        Set<OWLAxiom> entailments = ex.getEntailments();
        if (outputFile == null) {
            printNumbered(entailments);
        } else {
            OWLOntology entailmentOntology = manager.createOntology(entailments);
            try {
                manager.saveOntology(entailmentOntology, new FileOutputStream(outputFile));
                logger.info("saved ontology to file: " + outputFile.getAbsolutePath());
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private static Properties loadDefaultConfig() {

        return null;
    }

    private static void readArgs(String[] args) {

        for (int i = 0; i < args.length; i += 2) {
            if (i < args.length && (i + 1) < args.length) {
                String flag = args[i];
                String value = args[i + 1];

                if (flag.equals("-i") || flag.equals("--input")) {
                    inputFile = new File(value);
                    if (!inputFile.exists()) {
                        printErrorAndExit("file not found: " + inputFile.getAbsolutePath());
                    } else {
                        logger.info("loaded input file: " + inputFile.getAbsolutePath());
                    }
                } else if (flag.equals("-o") || flag.equals("--output")) {
                    outputFile = new File(value);
                    logger.info("saving output to file: " + outputFile.getAbsolutePath());

                } else if (flag.equals("-c") || flag.equals("--config")) {
                    configFile = new File(value);
                    if (!configFile.exists()) {
                        printErrorAndExit("file not found: " + configFile.getAbsolutePath());
                    } else {
                        logger.info("loaded config file: " + configFile);
                    }
                } else {
                    printErrorAndExit("unknown flag " + flag);
                }
            }
        }

        if (inputFile == null) {
            printErrorAndExit("no input file specified.");
        }


    }

    public static void printErrorAndExit(String s) {
        logger.severe("ERROR: " + s + " Exiting.");
        printUsage();
        System.exit(1);
    }

    private static void printUsage() {
        System.out.println("\nUSAGE: ");
        System.out.println("entailments -i inputfile.owl [-o outputfile.owl] [-c configfile.txt]");
        System.out.println("    -i inputfile.owl        Set the input ontology to be used.");
        System.out.println("    -o outputfile.owl       Set the file to output entailments to. \n" +
                "                            Default: orintNumbered to stdout.");
        System.out.println("    -i inputfile.owl        Set the configuration for entailment extraction. Standard Java key-value pair config file. \n" +
                "                            Default: use default configuration (see docs).");


    }
}
