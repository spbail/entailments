package uk.ac.manchester.cs.owl.entailments.util;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by
 * User: Samantha Bail
 * Date: 08/04/2013
 * Time: 16:52
 * The University of Manchester
 */


public class Util {
    private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    /**
     * @param reasonerName
     * @return
     * @throws Exception
     */
    public static OWLReasonerFactory getReasonerFactory(String reasonerName) {
        ReasonerType r = ReasonerType.get(reasonerName);
        return getReasonerFactory(r);
    }

    /**
     * @param r
     * @return
     * @throws Exception
     */
    public static OWLReasonerFactory getReasonerFactory(ReasonerType r) {
        switch (r) {
            case PELLET:
                return new PelletReasonerFactory();
            case HERMIT:
                return new Reasoner.ReasonerFactory();
            case FACTPP:
                return new FaCTPlusPlusReasonerFactory();
            case JFACT:
                return new JFactFactory();
            default:
                logger.severe(("invalid reasoner name " + r.name()));
                System.exit(1);
        }
        return null;

    }

    public static enum ReasonerType {
        NONE,
        PELLET,
        FACTPP,
        JFACT,
        HERMIT;

        public static ReasonerType get(String s) {
            if (s != null) {
                return ReasonerType.valueOf(s.toUpperCase());
            } else return NONE;
        }
    }


    /**
     * prints a set of OWL axioms
     * @param axioms the axioms to orintNumbered
     */
    public static void printNumbered(Collection<OWLAxiom> axioms) {
        List<String> axiomList = new ArrayList<String>();
        for (OWLAxiom ax : axioms) {
            axiomList.add(render(ax));
        }
        Collections.sort(axiomList);

        int i = 1;
        for (String s : axiomList) {
            System.out.println(i + ". " + s);
            i++;
        }

    }


    /**
     * @param a the OWL axiom to be printed
     */
    public static String render(OWLAxiom a) {
        OWLAxiom ax = a.getAxiomWithoutAnnotations();
        SimpleRenderer r = new SimpleRenderer();
        r.setShortFormProvider(new SimpleShortFormProvider());
        return r.render(ax);
    }


}
