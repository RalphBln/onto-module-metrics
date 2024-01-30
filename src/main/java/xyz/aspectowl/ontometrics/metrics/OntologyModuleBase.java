/** */
package xyz.aspectowl.ontometrics.metrics;

import java.util.HashSet;

import org.javatuples.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.AxiomAnnotations;
import org.semanticweb.owlapi.model.parameters.Imports;

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
 * @author Ralph Schaefermeier
 */
public abstract class OntologyModuleBase implements OntologyModule {

  protected OWLOntology onto;

  protected SimpleDirectedGraph<OWLObject, DefaultEdge> internalHieriarchicalRelations =
      new SimpleDirectedGraph<OWLObject, DefaultEdge>(DefaultEdge.class);
  protected SimpleGraph<OWLObject, DefaultEdge> internalNonHieriarchicalRelations =
      new SimpleGraph<OWLObject, DefaultEdge>(DefaultEdge.class);

  protected HashSet<Pair<OWLObject, OWLObject>> externalHierarchicalRelations =
      new HashSet<Pair<OWLObject, OWLObject>>();
  protected HashSet<Pair<OWLObject, OWLObject>> externalNonHierarchicalRelations =
      new HashSet<Pair<OWLObject, OWLObject>>();

  protected double cohesionCachedValue = -1d;
  protected double couplingCachedValue = -1d;
  protected double couplingHierarchicalCachedValue = -1d;
  protected double couplingNonHierarchicalCachedValue = -1d;

  /** */
  public OntologyModuleBase(OWLOntology onto) {
    this.onto = onto;
  }

  @Override
  public void processInternalHierarchicalRelation(OWLObject c1, OWLObject c2) {
    processInternalRelation(internalHieriarchicalRelations, c1, c2);
  }

  @Override
  public void processInternalNonHierarchicalRelation(OWLObject c1, OWLObject c2) {
    processInternalRelation(internalNonHieriarchicalRelations, c1, c2);
  }

  private void processInternalRelation(
      Graph<OWLObject, DefaultEdge> g, OWLObject c1, OWLObject c2) {
    if (c1.equals(c2)) return;

    if (!contains(c1))
      throw new IllegalArgumentException(
          c1 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
    if (!contains(c2))
      throw new IllegalArgumentException(
          c2 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());

    g.addVertex(c1);
    g.addVertex(c2);
    g.addEdge(c1, c2);
  }

  @Override
  public void processExternalHierarchicalRelation(OWLObject c1, OWLObject c2) {
    processExternalRelation(externalHierarchicalRelations, c1, c2);
  }

  @Override
  public void processExternalNonHierarchicalRelation(OWLObject c1, OWLObject c2) {
    processExternalRelation(externalNonHierarchicalRelations, c1, c2);
  }

  /*
   * It is assumed that c1 is the internal class and c2 the external
   */
  private void processExternalRelation(
      HashSet<Pair<OWLObject, OWLObject>> externalRelationsSet, OWLObject c1, OWLObject c2) {
    if (!contains(c1))
      throw new IllegalArgumentException(
          c1 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
    if (contains(c2))
      throw new IllegalArgumentException(
          c2
              + " should NOT belong to "
              + onto.getOntologyID().getOntologyIRI().get().getShortForm());

    externalRelationsSet.add(Pair.with(c1, c2));
  }

  @Override
  public boolean contains(OWLObject object) {
    if (object instanceof OWLEntity)
      return onto.getEntitiesInSignature(((OWLEntity) object).getIRI(), Imports.EXCLUDED)
          .contains(object);
    if (object instanceof OWLAxiom)
      return onto.containsAxiom(
          ((OWLAxiom) object), Imports.EXCLUDED, AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS);
    return false;
  }

  @Override
  public String getName() {
    return onto.getOntologyID().getOntologyIRI().get().getShortForm();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return getName();
  }
}
