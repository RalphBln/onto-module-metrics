package xyz.aspectowl.ontometrics.cohesion;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.jgrapht.util.WeightCombiner;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.ontometrics.util.GraphFactory;

/**
 * @author Ralph Schäfermeier
 */
public abstract class CohesionCouplingMetric {

  protected HashMap<OWLOntology, Graph<OWLEntity, DefaultWeightedEdge>> moduleGraphs =
      new HashMap<>();
  protected DijkstraShortestPath<OWLEntity, DefaultWeightedEdge> ontologyDijkstra;
  protected HashMap<
          Graph<OWLEntity, DefaultWeightedEdge>,
          DijkstraShortestPath<OWLEntity, DefaultWeightedEdge>>
      moduleDijkstras = new HashMap<>();

  // Represents the union of all ontology modules.
  // This graph is empty at first and gets filled as modules are added.
  protected Graph<OWLEntity, DefaultWeightedEdge> ontologyGraph = GraphFactory.newGraph();

  //  protected CohesionCouplingMetric(
  //      OWLLogicalAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
  //          axiomVisitor) {
  //    this.axiomVisitor = axiomVisitor;
  //  }

  public void setModularizedOntology(OWLOntology mainOntology) {
    System.out.format(
        "*** %s: Adding ontology %s%n",
        this.getClass().getSimpleName(), mainOntology.getOntologyID().getOntologyIRI().get());
    mainOntology
        .getOWLOntologyManager()
        .importsClosure(mainOntology)
        .forEach(module -> addModule(module));
  }

  public void addModule(OWLOntology module) {
    System.out.format(
        "%s: Adding module %s%n",
        this.getClass().getSimpleName(), module.getOntologyID().getOntologyIRI().get());
    var moduleGraph = graphFromOntologyModule(module);

    // for debugging
    // todo remove
    //    GraphRenderer.exportToGml(moduleGraph, module);

    moduleGraphs.put(module, moduleGraph);
    moduleDijkstras.put(moduleGraph, new DijkstraShortestPath<>(moduleGraph));

    ontologyDijkstra = new DijkstraShortestPath<>(ontologyGraph);
  }

  public Stream<OWLOntology> modules() {
    return moduleGraphs.keySet().stream();
  }

  public double getCohesion(OWLOntology module) {

    var graph = module == null ? ontologyGraph : moduleGraphs.get(module);

    var vertices = graph.vertexSet();
    if (vertices.size() < 2) {
      return 1;
    }

    // todo use graph instead of ontology
    double cardinality = getCardinality(graph);

    //    vertices.stream()
    //        .flatMap(
    //            v1 ->
    //                vertices.stream()
    //                    .filter(v2 -> !v1.equals(v2))
    //                    .map(v2 -> getShortestPath(graph, v1, v2)))
    //        .filter(Objects::nonNull)
    //        .forEach(
    //            p ->
    //                System.out.printf(
    //                    "Path [%s] ; l=%d%n",
    //                    p.getVertexList().stream()
    //                        .map(v -> v.getIRI().getShortForm())
    //                        .collect(Collectors.joining("->")),
    //                    p.getLength()));

    return vertices.stream()
            .flatMap(
                v1 ->
                    vertices.stream()
                        .filter(v2 -> !v1.equals(v2))
                        .map(v2 -> getShortestPath(graph, v1, v2)))
            .filter(Objects::nonNull)
            .map(GraphPath::getWeight)
            .filter(pathWeight -> pathWeight != 0)
            .mapToDouble(pathWeight -> 1d / pathWeight.doubleValue())
            .sum()
        / (cardinality * (cardinality - 1))
        * 2d;
  }

  public double getCoupling(OWLOntology module) {
    var moduleGraph = moduleGraphs.get(module);

    double moduleCardinality = getCardinality(moduleGraph);
    double restCardinality = getCardinality(ontologyGraph) - moduleCardinality;

    return moduleGraph.vertexSet().stream()
            .flatMap(
                v1 ->
                    ontologyGraph.vertexSet().stream()
                        .filter(v2 -> !moduleGraph.containsVertex(v2))
                        .map(v2 -> getShortestPath(ontologyGraph, v1, v2)))
            .filter(Objects::nonNull)
            .map(GraphPath::getWeight)
            .filter(pathWeight -> pathWeight != 0)
            .mapToDouble(pathWeight -> 1d / pathWeight.doubleValue())
            .sum()
        / (moduleCardinality * restCardinality)
        * 2d;
  }

  /*
  Has side effects! Adds graph presentation of axioms to ontologyGraph!
   */
  private Graph<OWLEntity, DefaultWeightedEdge> graphFromOntologyModule(OWLOntology module) {
    var g = new GraphBuilder<>(GraphFactory.newGraph());

    module
        .classesInSignature(Imports.EXCLUDED)
        .filter(clazz -> module.isDeclared(clazz, Imports.EXCLUDED))
        .forEach(g::addVertex);
    module
        .individualsInSignature(Imports.EXCLUDED)
        .filter(individual -> module.isDeclared(individual, Imports.EXCLUDED))
        .forEach(g::addVertex);

    Graph<OWLEntity, DefaultWeightedEdge> graph = g.build();

    for (OWLLogicalAxiom axiom :
        module.logicalAxioms(Imports.EXCLUDED).collect(Collectors.toList())) {
      var axiomAsGraph = axiom.accept(getAxiomVisitor());

      // visitor method may return null (because not all axiom types have been implemented)
      if (axiomAsGraph != null) {

        // add axiom to combined ontology, no matter what
        ontologyGraph = new AsGraphUnion<>(ontologyGraph, axiomAsGraph, WeightCombiner.MIN);

        // only add axioms with no disconnected links to the module
        if (checkAllEntitiesDeclaredInModule(graph, axiom)) {
          // AsGraphUnion is the most convenient way to add new edges, as it is possible in our case
          // that there are multiple
          // links between to entities, but we only consider the one with the lowest weight/distance
          // (highest link strength).
          // It might not be the most efficient, though, so we might opt to change this and add the
          // edges to the graph (and
          // handle the weight comparison and possible deletion and replacement of an existing edge
          // manually).
          graph =
              new AsGraphUnion<OWLEntity, DefaultWeightedEdge>(
                  graph, axiomAsGraph, WeightCombiner.MIN);
        }

        var aspectsAsGraph = aspectsAsGraph(axiom, module);
        ontologyGraph = new AsGraphUnion<>(ontologyGraph, aspectsAsGraph, WeightCombiner.MIN);
        if (checkAllEntitiesDeclaredInModule(aspectsAsGraph, axiom)) {
          graph =
              new AsGraphUnion<OWLEntity, DefaultWeightedEdge>(
                  graph, aspectsAsGraph, WeightCombiner.MIN);
        }
      }
    }

    return graph;
  }

  protected GraphPath<OWLEntity, DefaultWeightedEdge> getShortestPath(
      Graph<OWLEntity, DefaultWeightedEdge> graph, OWLEntity a, OWLEntity b) {
    return Optional.ofNullable(moduleDijkstras.get(graph)).orElse(ontologyDijkstra).getPath(a, b);
  }

  public double getCardinality(Graph moduleGraph) {
    return (double) moduleGraph.vertexSet().size();
  }

  protected boolean checkAllEntitiesDeclaredInModule(
      Graph<OWLEntity, DefaultWeightedEdge> graph, OWLAxiom axiom) {
    return Stream.of(axiom.individualsInSignature(), axiom.classesInSignature())
        .flatMap(entity -> entity)
        .allMatch(entity -> graph.containsVertex(entity));
  }

  protected abstract OWLAxiomVisitorEx<SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      getAxiomVisitor();

  /**
   * Returns the strength factor between two entities in the graph (directly or indirectly linked or
   * not linked at all). Must return 0 if source == target or if there is no path between source and
   * target.
   */
  protected abstract double calculateStrengthFactor(
      Graph<OWLEntity, DefaultWeightedEdge> graph, OWLEntity source, OWLEntity target);

  protected abstract SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> aspectsAsGraph(
      OWLLogicalAxiom axiom, OWLOntology module);
}
