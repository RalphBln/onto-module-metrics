package xyz.aspectowl.ontometrics.cohesion;

import java.util.Objects;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorExAdapter;
import xyz.aspectowl.ontometrics.util.GraphFactory;

/**
 * @author Ralph Schäfermeier
 */
public class Oh2011 extends CohesionCouplingMetric {

  private OWLAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      axiomVisitor;

  @Override
  public double getCohesion(OWLOntology module) {

    var graph = module == null ? ontologyGraph : moduleGraphs.get(module);

    var vertices = graph.vertexSet();
    if (vertices.size() < 2) {
      return 1;
    }

    // todo use graph instead of ontology
    double cardinality = getCardinality(module);

    return vertices.stream()
            .flatMap(v1 -> vertices.stream().map(v2 -> getShortestPath(graph, v1, v2)))
            .filter(Objects::nonNull)
            .map(GraphPath::getLength)
            .filter(pathLength -> pathLength != 0)
            .mapToDouble(pathLength -> 1d / pathLength.doubleValue())
            .sum()
        / (cardinality * (cardinality - 1))
        * 2d;
  }

  @Override
  public double getCoupling(OWLOntology module1, OWLOntology module2) {
    return 0;
  }

  @Override
  protected OWLAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      getAxiomVisitor() {
    if (axiomVisitor == null) axiomVisitor = new Oh2011AxiomVisitor();
    return axiomVisitor;
  }

  @Override
  protected double calculateStrengthFactor(
      Graph<OWLEntity, DefaultWeightedEdge> graph, OWLEntity source, OWLEntity target) {
    return 1d
        / getShortestPath(graph, source, target).getEdgeList().stream()
            .mapToDouble(graph::getEdgeWeight)
            .sum();
  }

  @Override
  protected SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> aspectsAsGraph(
      OWLLogicalAxiom axiom, OWLOntology module) {
    var g = GraphFactory.newGraphBuilder();
    axiom
        .annotations()
        .filter(anno -> anno.getProperty().getIRI().getShortForm().equals("hasAspect"))
        .forEach(
            anno -> {
              // todo Do some more checks if annotation value is actually an IRI and that the IRI
              // identifies a class
              OWLClass aspect =
                  module
                      .getOWLOntologyManager()
                      .getOWLDataFactory()
                      .getOWLClass(anno.getValue().asIRI().get());
              Stream.of(axiom.classesInSignature(), axiom.individualsInSignature())
                  .flatMap(entity -> entity)
                  .forEach(entity -> g.addEdge(aspect, entity, 1d));
            });
    return g.build();
  }

  private static class Oh2011AxiomVisitor
      extends OWLAxiomVisitorExAdapter<
          SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>> {

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLSubClassOfAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      OWLClassExpression subClassExp = axiom.getSubClass();
      OWLClassExpression superClassExp = axiom.getSuperClass();
      if (subClassExp.isNamed() && superClassExp.isNamed()) {
        // simple case
        g.addEdge(subClassExp.asOWLClass(), superClassExp.asOWLClass(), 1d);
      } else if (subClassExp.isNamed() && superClassExp instanceof OWLObjectUnionOf) {
        // Kumar2017 case #1
        ((OWLObjectUnionOf) superClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(c -> g.addEdge(subClassExp.asOWLClass(), c.asOWLClass(), 1.0));
      } else if (subClassExp instanceof OWLObjectIntersectionOf && superClassExp.isNamed()) {
        // Kumar2017 case #2
        ((OWLObjectIntersectionOf) subClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(c -> g.addEdge(c.asOWLClass(), superClassExp.asOWLClass(), 1.0));
      } else if (subClassExp.isNamed() && superClassExp instanceof OWLObjectIntersectionOf) {
        // Kumar2017 case #3
        ((OWLObjectIntersectionOf) superClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(c -> g.addEdge(subClassExp.asOWLClass(), c.asOWLClass(), 1.0));
      }
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLDisjointClassesAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      axiom
          .asPairwiseAxioms()
          .forEach(
              pairAxiom -> {
                OWLClassExpression e1 = pairAxiom.getOperandsAsList().get(0);
                OWLClassExpression e2 = pairAxiom.getOperandsAsList().get(1);
                if (e1.isNamed() && e2.isNamed()) {
                  g.addEdge(e1.asOWLClass(), e2.asOWLClass(), 1.0);
                  g.addEdge(e2.asOWLClass(), e1.asOWLClass(), 1.0);
                }
              });
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLDifferentIndividualsAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      axiom
          .asPairwiseAxioms()
          .forEach(
              pairAxiom -> {
                OWLIndividual i1 = pairAxiom.getOperandsAsList().get(0);
                OWLIndividual i2 = pairAxiom.getOperandsAsList().get(1);
                if (i1.isNamed() && i2.isNamed()) {
                  g.addEdge(i1.asOWLNamedIndividual(), i2.asOWLNamedIndividual(), 1.0);
                  g.addEdge(i2.asOWLNamedIndividual(), i1.asOWLNamedIndividual(), 1.0);
                }
              });
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLObjectPropertyAssertionAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      OWLIndividual i1 = axiom.getSubject();
      OWLIndividual i2 = axiom.getObject();
      if (i1.isNamed() && i2.isNamed()) {
        g.addEdge(i1.asOWLNamedIndividual(), i2.asOWLNamedIndividual(), 1.0);
        g.addEdge(i2.asOWLNamedIndividual(), i1.asOWLNamedIndividual(), 1.0);
      }
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLClassAssertionAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      var ce = axiom.getClassExpression();
      var i = axiom.getIndividual();
      if (ce.isNamed() && i.isNamed()) {
        g.addEdge(i.asOWLNamedIndividual(), ce.asOWLClass(), 1d);
      }
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLEquivalentClassesAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      axiom
          .asPairwiseAxioms()
          .forEach(
              pairAxiom -> {
                OWLClassExpression e1 = pairAxiom.getOperandsAsList().get(0);
                OWLClassExpression e2 = pairAxiom.getOperandsAsList().get(1);
                if (e1.isNamed() && e2.isNamed()) {
                  g.addEdge(e1.asOWLClass(), e2.asOWLClass(), 1.0);
                  g.addEdge(e2.asOWLClass(), e1.asOWLClass(), 1.0);
                }
              });
      return g.build();
    }

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLSameIndividualAxiom axiom) {
      var g = GraphFactory.newGraphBuilder();
      axiom
          .asPairwiseAxioms()
          .forEach(
              pairAxiom -> {
                OWLIndividual i1 = pairAxiom.getOperandsAsList().get(0);
                OWLIndividual i2 = pairAxiom.getOperandsAsList().get(1);
                if (i1.isNamed() && i2.isNamed()) {
                  g.addEdge(i1.asOWLNamedIndividual(), i2.asOWLNamedIndividual(), 1.0);
                  g.addEdge(i2.asOWLNamedIndividual(), i1.asOWLNamedIndividual(), 1.0);
                }
              });
      return g.build();
    }
  }
}
