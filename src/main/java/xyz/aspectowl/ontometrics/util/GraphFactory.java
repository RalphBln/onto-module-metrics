package xyz.aspectowl.ontometrics.util;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.builder.GraphBuilder;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * @author Ralph Schäfermeier
 */
public class GraphFactory {

  public static SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge> newGraph() {
    return new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
  }

  public static GraphBuilder<
          OWLEntity,
          DefaultWeightedEdge,
          SimpleDirectedWeightedGraph<OWLEntity, DefaultWeightedEdge>>
      newGraphBuilder() {
    return new GraphBuilder<>(newGraph());
  }
}
