package xyz.aspectowl.ontometrics.metrics;

/**
 * @author Ralph Schäfermeier
 */
public interface ModularityMetric {

  public double cohesion();

  public double coupling();

  public double couplingHierarchical();

  public double couplingNonHierarchical();

  public double strengthOfRelation();
}
