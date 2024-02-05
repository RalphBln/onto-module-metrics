package xyz.aspectowl.ontometrics.cohesion;

import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.util.WeightCombiner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAxiomVisitorExAdapter;
import xyz.aspectowl.ontometrics.util.GraphFactory;

/**
 * @author Ralph Schäfermeier
 */
public class Kumar2017 extends CohesionCouplingMetric {

  private OWLAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      axiomVisitor;

  @Override
  protected OWLAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      getAxiomVisitor() {
    if (axiomVisitor == null) axiomVisitor = new Kumar2017AxiomVisitor();
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

  private static class Kumar2017AxiomVisitor
      extends OWLAxiomVisitorExAdapter<
          SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>> {

    @Override
    public SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> visit(
        OWLSubClassOfAxiom axiom) {

      var g = GraphFactory.newGraphBuilder();

      OWLClassExpression preliminarySubClassExp = axiom.getSubClass();
      OWLClassExpression preliminarySuperClassExp = axiom.getSuperClass();

      double preliminaryWeight = 1d;
      if (preliminarySubClassExp instanceof OWLObjectSomeValuesFrom) {
        // For Kumar, existential quantification weakens the link strength to 1/3 (= increases the
        // weight by the factor 3).
        // For Oh, the link strength remains 1.
        // in any case, the link strength calculations will be applied to the filler of the
        // existentially quantified restriction
        preliminaryWeight = 3d;
        preliminarySubClassExp = ((OWLObjectSomeValuesFrom) preliminarySubClassExp).getFiller();
      }
      if (preliminarySubClassExp instanceof OWLObjectSomeValuesFrom) {
        // Same for existential quantification in the superClass position
        preliminaryWeight = 3d;
        preliminarySubClassExp = ((OWLObjectSomeValuesFrom) preliminarySubClassExp).getFiller();
      }

      if (preliminarySubClassExp instanceof OWLObjectAllValuesFrom) {
        // For Kumar and Oh, universal quantification does not weaken the link.
        // In any case, the link strength calculations will be applied to the filler of the
        // existentially quantified restriction
        preliminarySubClassExp = ((OWLObjectAllValuesFrom) preliminarySubClassExp).getFiller();
      }
      if (preliminarySubClassExp instanceof OWLObjectAllValuesFrom) {
        // Same for universal quantification in the superClass position
        preliminarySubClassExp = ((OWLObjectAllValuesFrom) preliminarySubClassExp).getFiller();
      }

      // weight needs to be effectively final because we use it in a lambda expression, that's why
      // we have to have
      // preliminaryWeight first.
      double weight = preliminaryWeight;
      OWLClassExpression subClassExp = preliminarySubClassExp;
      OWLClassExpression superClassExp = preliminarySuperClassExp;

      if (subClassExp.isNamed() && superClassExp.isNamed()) {
        // simple case
        g.addEdge(subClassExp.asOWLClass(), superClassExp.asOWLClass(), 1.0 * weight);
      } else if (subClassExp.isNamed() && superClassExp instanceof OWLObjectUnionOf) {
        // Kumar2017 case #1
        double n = (double) ((OWLObjectUnionOf) superClassExp).operands().count();
        double weightEachOperand = 3d * (n - 1d) / n;
        ((OWLObjectUnionOf) superClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(
                c ->
                    g.addEdge(
                        subClassExp.asOWLClass(),
                        c.asOWLClass(),
                        1.0 * weightEachOperand * weight));
      } else if (subClassExp instanceof OWLObjectIntersectionOf && superClassExp.isNamed()) {
        // Kumar2017 case #2
        double n = (double) ((OWLObjectIntersectionOf) subClassExp).operands().count();
        double weightEachOperand = 3d * (n - 1d);
        ((OWLObjectIntersectionOf) subClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(
                c ->
                    g.addEdge(
                        c.asOWLClass(),
                        superClassExp.asOWLClass(),
                        1.0 * weightEachOperand * weight));
      } else if (subClassExp.isNamed() && superClassExp instanceof OWLObjectIntersectionOf) {
        // Kumar2017 case #3
        // independent of number concepts, so no additional weighting
        ((OWLObjectIntersectionOf) superClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(c -> g.addEdge(subClassExp.asOWLClass(), c.asOWLClass(), 1.0 * weight));
      } else if (subClassExp instanceof OWLObjectUnionOf && superClassExp.isNamed()) {
        // Strong relation, since each of the classes in the ObjectUnionOf is a subClass of the
        // superClass, and thereby
        // strongly dependent
        ((OWLObjectUnionOf) subClassExp)
            .operands()
            .filter(IsAnonymous::isNamed)
            .forEach(c -> g.addEdge(c.asOWLClass(), superClassExp.asOWLClass(), 1.0 * weight));
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
                  g.addEdge(e1.asOWLClass(), e2.asOWLClass(), 2.0);
                  g.addEdge(e2.asOWLClass(), e1.asOWLClass(), 2.0);
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
                  g.addEdge(i1.asOWLNamedIndividual(), i2.asOWLNamedIndividual(), 2.0);
                  g.addEdge(i2.asOWLNamedIndividual(), i1.asOWLNamedIndividual(), 2.0);
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
      Graph<OWLEntity, DefaultWeightedEdge> g = GraphFactory.newGraph();
      for (var sca : axiom.asOWLSubClassOfAxioms()) {
        g = new AsGraphUnion<>(g, visit(sca), WeightCombiner.MIN);
      }

      var result = GraphFactory.newGraph();
      Graphs.addGraph(result, g);
      //      Graphs.addGraph(result, g);
      for (DefaultWeightedEdge edge : result.edgeSet()) {
        result.setEdgeWeight(edge, result.getEdgeWeight(edge) * 2.0d);
      }

      return result;
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
                  g.addEdge(i1.asOWLNamedIndividual(), i2.asOWLNamedIndividual(), 2d);
                  g.addEdge(i2.asOWLNamedIndividual(), i1.asOWLNamedIndividual(), 2d);
                }
              });
      return g.build();
    }
  }
}
