package xyz.aspectowl.ontometrics.metrics;

import org.semanticweb.owlapi.model.OWLObject;

/**
 * @author Ralph Schäfermeier
 */
public interface OntologyModule {

  public double cohesion();

  public double coupling();

  public double couplingHierarchical();

  public double couplingNonHierarchical();

  public double strengthOfRelation();

  public void processInternalHierarchicalRelation(OWLObject c1, OWLObject c2);

  public void processInternalNonHierarchicalRelation(OWLObject c1, OWLObject c2);

  public void processExternalHierarchicalRelation(OWLObject c1, OWLObject c2);

  public void processExternalNonHierarchicalRelation(OWLObject c1, OWLObject c2);

  public boolean contains(OWLObject object);

  public String getName();
}
