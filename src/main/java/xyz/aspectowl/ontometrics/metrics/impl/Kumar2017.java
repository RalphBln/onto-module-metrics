package xyz.aspectowl.ontometrics.metrics.impl;

import org.semanticweb.owlapi.model.OWLOntology;
import xyz.aspectowl.ontometrics.metrics.OntologyModuleBase;

/**
 * @author Ralph Schäfermeier
 */
public class Kumar2017 extends OntologyModuleBase {

    /**
     * @param onto
     */
    public Kumar2017(OWLOntology onto) {
        super(onto);
    }

    @Override
    public double cohesion() {
        return 0;
    }

    @Override
    public double coupling() {
        return 0;
    }

    @Override
    public double couplingHierarchical() {
        return 0;
    }

    @Override
    public double couplingNonHierarchical() {
        return 0;
    }

    @Override
    public double strengthOfRelation() {
        return 0;
    }
}
