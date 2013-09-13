package uk.ac.manchester.cs.owl.entailments.util;

import org.semanticweb.owlapi.model.IRI;

/**
 * Created with IntelliJ IDEA.
 * User: sam
 * Date: 06/09/2013
 * Time: 13:36
 * To change this template use File | Settings | File Templates.
 */
public class Constants {

    // private constructor prevents instantiation of this class
    private Constants() {
    }

    public static final IRI AXIOM_ID_IRI =
            IRI.create("http://owl.cs.manchester.ac.uk/ontology#axid");
}
