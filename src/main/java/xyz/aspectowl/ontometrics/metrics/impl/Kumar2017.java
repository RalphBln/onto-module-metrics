package xyz.aspectowl.ontometrics.metrics.impl;

import xyz.aspectowl.ontometrics.metrics.ModularityMetricBase;

/**
 * @author Ralph Schäfermeier
 */
public class Kumar2017 extends ModularityMetricBase {
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
