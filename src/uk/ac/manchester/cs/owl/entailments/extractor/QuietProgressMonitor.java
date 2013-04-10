package uk.ac.manchester.cs.owl.entailments.extractor;

import org.semanticweb.owl.explanation.api.Explanation;
import org.semanticweb.owl.explanation.api.ExplanationGenerator;
import org.semanticweb.owl.explanation.api.ExplanationProgressMonitor;
import org.semanticweb.owl.explanation.telemetry.TelemetryTimer;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.HashSet;
import java.util.Set;


public class QuietProgressMonitor implements ExplanationProgressMonitor<OWLAxiom> {

    public void foundExplanation(ExplanationGenerator<OWLAxiom> gen, Explanation<OWLAxiom> expl, Set<Explanation<OWLAxiom>> allExpls) {
        System.out.print(".");
    }

    public boolean isCancelled() {
        return false;
    }
}
