/**
 * 
 */
package xyz.aspectowl.ontometrics.test.cohesion;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultUndirectedGraph;
import org.junit.jupiter.api.Test;

/**
 * @author ralph
 */
public class TestGraph {

	@Test
	public void test() {
		
		DefaultUndirectedGraph<String, DefaultEdge> graph = new DefaultUndirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		
		graph.addVertex("A");
		graph.addVertex("B");
		graph.addVertex("C");
		
		// Let's insert some duplicate vertices (should be ignored)
		graph.addVertex("A");
		graph.addVertex("A");

		assert(graph.vertexSet().size() == 3);

		graph.addEdge("A", "B");
		graph.addEdge("B", "C");
		
		// Let's insert a duplicate edge (should be ignored)
		graph.addEdge("A", "B");
		
		// And an edge that already exists but in the other direction (should also be ignored, since we have an undirected graph)
		graph.addEdge("B", "A");
		
		assert(graph.edgeSet().size() == 2);
	}

}
