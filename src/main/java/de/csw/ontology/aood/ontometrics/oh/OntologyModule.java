/**
 * 
 */
package de.csw.ontology.aood.ontometrics.oh;

import java.util.HashSet;

import org.javatuples.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author ralph
 */
public class OntologyModule {

	private OWLOntology onto;
	
	DirectedGraph<OWLClass, DefaultEdge> internalHieriarchicalClassRelations = new SimpleDirectedGraph<OWLClass, DefaultEdge>(DefaultEdge.class);
	UndirectedGraph<OWLClass, DefaultEdge> internalNonHieriarchicalClassRelations = new SimpleGraph<OWLClass, DefaultEdge>(DefaultEdge.class);
	
	private HashSet<Pair<OWLClass, OWLClass>> externalHieriarchicalClassRelations = new HashSet<Pair<OWLClass, OWLClass>>();
	private HashSet<Pair<OWLClass, OWLClass>> externalNonHieriarchicalClassRelations = new HashSet<Pair<OWLClass, OWLClass>>();
	
	private double cohesionCachedValue = -1d;
	private double coulingCachedValue = -1d;
	private double couplingHierarchicalCachedValue = -1d;
	private double couplingNonHierarchicalCachedValue = -1d;


	/**
	 * 
	 */
	public OntologyModule(OWLOntology onto) {
		this.onto = onto;
	}
	
	public double cohesion() {
		if (cohesionCachedValue == -1) {
			double classCount = onto.classesInSignature().count();
			cohesionCachedValue = classCount == 1 ? 1d : (sr() + (double)internalNonHieriarchicalClassRelations.edgeSet().size()) / (classCount * (classCount - 1)) * 2d;
		}
		return cohesionCachedValue;
	}
	
	private double sr() {
		return internalHieriarchicalClassRelations.vertexSet().stream()
				.flatMap(v1 -> internalHieriarchicalClassRelations.vertexSet().stream()
						.map(v2 -> new DijkstraShortestPath<OWLClass, DefaultEdge>(internalHieriarchicalClassRelations, v1, v2).getPathLength()))
				.filter(d -> d != 0d && d != Double.POSITIVE_INFINITY)
				.mapToDouble(x -> 1d/x)
				.sum();
	}
	
	public double coupling() {
		if (coulingCachedValue == -1d) {
			coulingCachedValue = ratio((double)(externalHieriarchicalClassRelations.size() + externalNonHieriarchicalClassRelations.size()), (double)(internalHieriarchicalClassRelations.edgeSet().size() + internalNonHieriarchicalClassRelations.edgeSet().size()));
		}
		return coulingCachedValue;
	}
	
	public double couplingHierarchical() {
		if (couplingHierarchicalCachedValue == -1d) {
			couplingHierarchicalCachedValue = ratio((double)externalHieriarchicalClassRelations.size(), (double)internalHieriarchicalClassRelations.edgeSet().size());
		}
		return couplingHierarchicalCachedValue;
	}
	
	public double couplingNonHierarchical() {
		if (couplingNonHierarchicalCachedValue == -1d) {
			couplingNonHierarchicalCachedValue = ratio((double)externalNonHieriarchicalClassRelations.size(), (double)internalNonHieriarchicalClassRelations.edgeSet().size());
		}
		return couplingNonHierarchicalCachedValue;
	}
	
	private double ratio(double external, double internal) {
		if (external == 0d)
			return 0d;
		return external / (external + internal);
	}
	
	
	public void processInternalHierarchicalRelation(OWLClass c1, OWLClass c2) {
		processInternalRelation(internalHieriarchicalClassRelations, c1, c2);
	}

	public void processInternalNonHierarchicalRelation(OWLClass c1, OWLClass c2) {
		processInternalRelation(internalNonHieriarchicalClassRelations, c1, c2);
	}
	
	private void processInternalRelation(Graph<OWLClass, DefaultEdge> g, OWLClass c1, OWLClass c2) {
		if (c1.equals(c2))
			return;
		
		if (!contains(c1))
			throw new IllegalArgumentException(c1.getIRI().getShortForm() + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		if (!contains (c2))
			throw new IllegalArgumentException(c2.getIRI().getShortForm() + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		
		g.addVertex(c1);
		g.addVertex(c2);
		g.addEdge(c1, c2);
	}
	


	public void processExternalHierarchicalRelation(OWLClass c1, OWLClass c2) {
		processExternalRelation(externalHieriarchicalClassRelations, c1, c2);
	}

	public void processExternalNonHierarchicalRelation(OWLClass c1, OWLClass c2) {
		processExternalRelation(externalNonHieriarchicalClassRelations, c1, c2);
	}
	
	/*
	 * It is assumed that c1 is the internal class and c2 the external
	 */
	private void processExternalRelation(HashSet<Pair<OWLClass, OWLClass>> externalRelationsSet, OWLClass c1, OWLClass c2) {
		if (!contains(c1))
			throw new IllegalArgumentException(c1.getIRI().getShortForm() + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		if (contains (c2))
			throw new IllegalArgumentException(c2.getIRI().getShortForm() + " should NOT belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());

		externalRelationsSet.add(Pair.with(c1, c2));
	}
	
	public boolean contains(OWLClass clazz) {
		return onto.getClassesInSignature(false).contains(clazz);
	}
	
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
