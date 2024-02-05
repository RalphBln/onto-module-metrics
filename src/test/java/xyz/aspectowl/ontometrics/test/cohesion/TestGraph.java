/** */
package xyz.aspectowl.ontometrics.test.cohesion;

import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.junit.jupiter.api.Test;

/**
 * @author Ralph Schaefermeier
 */
public class TestGraph {

  @Test
  public void test() {

    DefaultUndirectedGraph<String, DefaultEdge> graph =
        new DefaultUndirectedGraph<>(DefaultEdge.class);

    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");

    // Let's insert some duplicate vertices (should be ignored)
    graph.addVertex("A");
    graph.addVertex("A");

    assert (graph.vertexSet().size() == 3);

    graph.addEdge("A", "B");
    graph.addEdge("B", "C");

    // Let's insert a duplicate edge (should be ignored)
    graph.addEdge("A", "B");

    // And an edge that already exists but in the other direction (should also be ignored, since we
    // have an undirected graph)
    graph.addEdge("B", "A");

    assert (graph.edgeSet().size() == 2);
  }

  @Test
  public void weightedGraphShortestPath() {
    var g =
        new GraphBuilder<
                String,
                DefaultWeightedEdge,
                DirectedWeightedMultigraph<String, DefaultWeightedEdge>>(
                new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class))
            .addEdge("A", "B", 1d / 3d)
            .addEdge("B", "C", 1d / 3d)
            .addEdge("C", "D", 1d / 3d)
            .addEdge("D", "E", 1d / 3d)
            .addEdge("E", "F", 1d / 3d)
            .addEdge("A", "F", 1d)
            .build();

    System.out.println(g);

    var dijkstra = new AllDirectedPaths<>(g);

    var paths = dijkstra.getAllPaths("A", "F", true, null);
    paths.forEach(
        path -> {
          System.out.println(path.getVertexList());

          path.getEdgeList()
              .forEach(
                  edge ->
                      System.out.printf(
                          "%s%s %f\n",
                          g.getEdgeSource(edge), g.getEdgeTarget(edge), g.getEdgeWeight(edge)));

          double sf =
              1d
                  / path.getEdgeList().stream()
                      .mapToDouble(edge -> 1d / g.getEdgeWeight(edge))
                      .sum();

          System.out.println(sf);
        });

    // Problem: Shortest path algorithm (such as dijkstra) adds the weights
    // our path length metric is different. Its not the sum of weights but the inverse (under
    // multiplication) of the sums of the inverses (again under multiplication) of the weights.
    // not w1 + w2 + w3
    // but 1 / (1/w1 + 1/w2 + 1/w3)
    // Idea: Use the log law
    // log(a*b) = log(a) + log(b)
  }
}
