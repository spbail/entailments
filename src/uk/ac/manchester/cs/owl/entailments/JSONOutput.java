package uk.ac.manchester.cs.owl.entailments;

import net.minidev.json.JSONObject;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by
 * User: Samantha Bail
 * Date: 29/08/2013
 * Time: 14:41
 * The University of Manchester
 */


public class JSONOutput {
    public void saveEntailments(Set<OWLAxiom> entailments, File outputFile) throws IOException {
        JSONObject obj = new JSONObject();

        int i = 0;

        List<OWLSubClassOfAxiom> unsat = getUnsatClassesAxioms(entailments);
        List<OWLSubClassOfAxiom> sat = getSatClassesAxioms(entailments);

        for (OWLSubClassOfAxiom sub : unsat) {

            String id = "a" + pad(i++, 3);

            JSONObject data = new JSONObject();
            String subcls = render(sub.getSubClass());
            String supcls = render(sub.getSuperClass());
            String txt = subcls + " -> " + supcls;

            data.put("txt", txt);
            data.put("id", id);
            data.put("unsat", "unsat");

            obj.put(id, data);

        }
        for (OWLSubClassOfAxiom sub : sat) {

            String id = "a" + pad(i++, 3);

            JSONObject data = new JSONObject();
            String subcls = render(sub.getSubClass());
            String supcls = render(sub.getSuperClass());
            String txt = subcls + " -> " + supcls;

            data.put("txt", txt);
            data.put("id", id);
            data.put("unsat", "sat");

            obj.put(id, data);

        }


        write(obj, outputFile);


    }

    private List<OWLSubClassOfAxiom> getSatClassesAxioms(Set<OWLAxiom> entailments) {
        List<OWLSubClassOfAxiom> list = new ArrayList<OWLSubClassOfAxiom>();
        for (OWLAxiom ax : entailments) {
            OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom) ax;
            if (!sub.getSuperClass().isOWLNothing()) {
                list.add(sub);
            }
        }
        Collections.sort(list);
        return list;
    }

    private List<OWLSubClassOfAxiom> getUnsatClassesAxioms(Set<OWLAxiom> entailments) {
        List<OWLSubClassOfAxiom> list = new ArrayList<OWLSubClassOfAxiom>();
        for (OWLAxiom ax : entailments) {
            OWLSubClassOfAxiom sub = (OWLSubClassOfAxiom) ax;
            if (sub.getSuperClass().isOWLNothing()) {
                list.add(sub);
            }
        }

        Collections.sort(list);
        return list;

    }

    public static String render(OWLObject o) {
        SimpleRenderer r = new SimpleRenderer();
        r.setShortFormProvider(new SimpleShortFormProvider());
        return r.render(o);
    }

    private void write(JSONObject obj, File outputFile) throws IOException {
        FileWriter fw = new FileWriter(outputFile);
        BufferedWriter out = new BufferedWriter(fw);
        out.write(obj.toJSONString());
        out.close();
    }

    /**
     * @param number any integer
     * @param length how many positions do we want to pad the string to?
     * @return number padded with zeros to make up 4 digits
     */
    public static String pad(int number, int length) {
        String s = Integer.toString(number);
        return ZEROS[length - s.length()] + s;
    }

    private static String[] ZEROS = {"", "0", "00", "000"};


}
