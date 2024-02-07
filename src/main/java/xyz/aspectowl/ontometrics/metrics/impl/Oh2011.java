package xyz.aspectowl.ontometrics.metrics.impl;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.ontometrics.metrics.OntologyModuleBase;

/**
 * Oh2011 et al. only look at class relations. They make a distinction between hierarchical and
 * non-hierarchical class relations and weigh them differently.
 *
 * <p>Hierarchical class relations are sub-/superclass relations.
 *
 * <p>Non-hierarchical class relations are:
 *
 * <p>ClassExpression := Class | ObjectIntersectionOf | ObjectUnionOf | ObjectComplementOf |
 * ObjectOneOf | ObjectSomeValuesFrom | ObjectAllValuesFrom | ObjectHasValue | ObjectHasSelf |
 * ObjectMinCardinality | ObjectMaxCardinality | ObjectExactCardinality | DataSomeValuesFrom |
 * DataAllValuesFrom | DataHasValue | DataMinCardinality | DataMaxCardinality | DataExactCardinality
 *
 * @author Ralph Schäfermeier
 * @deprecated Use {@link xyz.aspectowl.ontometrics.cohesion.Oh2011 instead. This class will be
 *     deleted in the near future.}
 */
public class Oh2011 extends OntologyModuleBase {

  public Oh2011(OWLOntology onto) {
    super(onto);
  }

  @Override
  public double cohesion() {
    if (cohesionCachedValue == -1) {
      double entityCount =
          onto.classesInSignature(Imports.EXCLUDED).count()
              + onto.individualsInSignature(Imports.EXCLUDED).count();
      cohesionCachedValue =
          entityCount == 1
              ? 1d
              : (strengthOfRelation() + (double) internalNonHieriarchicalRelations.edgeSet().size())
                  / (entityCount * (entityCount - 1))
                  * 2d;
    }
    return cohesionCachedValue;
  }

  @Override
  public double coupling() {
    if (couplingCachedValue == -1d) {
      couplingCachedValue =
          ratio(
              (double)
                  (externalHierarchicalRelations.size() + externalNonHierarchicalRelations.size()),
              (double)
                  (internalHieriarchicalRelations.edgeSet().size()
                      + internalNonHieriarchicalRelations.edgeSet().size()));
    }
    return couplingCachedValue;
  }

  @Override
  public double couplingHierarchical() {
    if (couplingHierarchicalCachedValue == -1d) {
      couplingHierarchicalCachedValue =
          ratio(
              (double) externalHierarchicalRelations.size(),
              (double) internalHieriarchicalRelations.edgeSet().size());
    }
    return couplingHierarchicalCachedValue;
  }

  @Override
  public double couplingNonHierarchical() {
    if (couplingNonHierarchicalCachedValue == -1d) {
      couplingNonHierarchicalCachedValue =
          ratio(
              (double) externalNonHierarchicalRelations.size(),
              (double) internalNonHieriarchicalRelations.edgeSet().size());
    }
    return couplingNonHierarchicalCachedValue;
  }

  @Override
  public double strengthOfRelation() {
    var dijkstra = new DijkstraShortestPath<>(internalHieriarchicalRelations);

    return internalHieriarchicalRelations.vertexSet().stream()
        .flatMap(
            v1 ->
                internalHieriarchicalRelations.vertexSet().stream()
                    .map(v2 -> dijkstra.getPath(v1, v2)))
        .filter(path -> path != null)
        .map(path -> path.getLength())
        .filter(pathLength -> pathLength != 0)
        .mapToDouble(pathLength -> 1d / pathLength.doubleValue())
        .sum();
  }

  private double ratio(double external, double internal) {
    if (external == 0d) return 0d;
    return external / (external + internal);
  }
}
