/**
 * 
 */
package xyz.aspectowl.ontometrics.oh;

import java.util.HashSet;

import org.javatuples.Pair;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
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
 * @author ralph
 */
public class OntologyModule {

	private OWLOntology onto;
	
	DirectedGraph<OWLObject, DefaultEdge> internalHieriarchicalRelations = new SimpleDirectedGraph<OWLObject, DefaultEdge>(DefaultEdge.class);
	UndirectedGraph<OWLObject, DefaultEdge> internalNonHieriarchicalRelations = new SimpleGraph<OWLObject, DefaultEdge>(DefaultEdge.class);
	
	private HashSet<Pair<OWLObject, OWLObject>> externalHieriarchicalRelations = new HashSet<Pair<OWLObject, OWLObject>>();
	private HashSet<Pair<OWLObject, OWLObject>> externalNonHieriarchicalRelations = new HashSet<Pair<OWLObject, OWLObject>>();
	
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
			double entityCount = onto.classesInSignature(Imports.EXCLUDED).count() + onto.individualsInSignature(Imports.EXCLUDED).count();
			cohesionCachedValue = entityCount == 1 ? 1d : (sr() + (double)internalNonHieriarchicalRelations.edgeSet().size()) / (entityCount * (entityCount - 1)) * 2d;
		}
		return cohesionCachedValue;
	}
	
	private double sr() {
		return internalHieriarchicalRelations.vertexSet().stream()
				.flatMap(v1 -> internalHieriarchicalRelations.vertexSet().stream()
						.map(v2 -> new DijkstraShortestPath<OWLObject, DefaultEdge>(internalHieriarchicalRelations, v1, v2).getPathLength()))
				.filter(d -> d != 0d && d != Double.POSITIVE_INFINITY)
				.mapToDouble(x -> 1d/x)
				.sum();
	}
	
	public double coupling() {
		if (coulingCachedValue == -1d) {
			coulingCachedValue = ratio((double)(externalHieriarchicalRelations.size() + externalNonHieriarchicalRelations.size()), (double)(internalHieriarchicalRelations.edgeSet().size() + internalNonHieriarchicalRelations.edgeSet().size()));
		}
		return coulingCachedValue;
	}
	
	public double couplingHierarchical() {
		if (couplingHierarchicalCachedValue == -1d) {
			couplingHierarchicalCachedValue = ratio((double)externalHieriarchicalRelations.size(), (double)internalHieriarchicalRelations.edgeSet().size());
		}
		return couplingHierarchicalCachedValue;
	}
	
	public double couplingNonHierarchical() {
		if (couplingNonHierarchicalCachedValue == -1d) {
			couplingNonHierarchicalCachedValue = ratio((double)externalNonHieriarchicalRelations.size(), (double)internalNonHieriarchicalRelations.edgeSet().size());
		}
		return couplingNonHierarchicalCachedValue;
	}
	
	private double ratio(double external, double internal) {
		if (external == 0d)
			return 0d;
		return external / (external + internal);
	}
	
	
	public void processInternalHierarchicalRelation(OWLObject c1, OWLObject c2) {
		processInternalRelation(internalHieriarchicalRelations, c1, c2);
	}

	public void processInternalNonHierarchicalRelation(OWLObject c1, OWLObject c2) {
		processInternalRelation(internalNonHieriarchicalRelations, c1, c2);
	}
	
	private void processInternalRelation(Graph<OWLObject, DefaultEdge> g, OWLObject c1, OWLObject c2) {
		if (c1.equals(c2))
			return;
		
		if (!contains(c1))
			throw new IllegalArgumentException(c1 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		if (!contains (c2))
			throw new IllegalArgumentException(c2 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		
		g.addVertex(c1);
		g.addVertex(c2);
		g.addEdge(c1, c2);
	}
	


	public void processExternalHierarchicalRelation(OWLObject c1, OWLObject c2) {
		processExternalRelation(externalHieriarchicalRelations, c1, c2);
	}

	public void processExternalNonHierarchicalRelation(OWLObject c1, OWLObject c2) {
		processExternalRelation(externalNonHieriarchicalRelations, c1, c2);
	}
	
	/*
	 * It is assumed that c1 is the internal class and c2 the external
	 */
	private void processExternalRelation(HashSet<Pair<OWLObject, OWLObject>> externalRelationsSet, OWLObject c1, OWLObject c2) {
		if (!contains(c1))
			throw new IllegalArgumentException(c1 + " should belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());
		if (contains (c2))
			throw new IllegalArgumentException(c2 + " should NOT belong to " + onto.getOntologyID().getOntologyIRI().get().getShortForm());

		externalRelationsSet.add(Pair.with(c1, c2));
	}
	
	public boolean contains(OWLObject object) {
		if (object instanceof OWLEntity)
			return onto.getEntitiesInSignature(((OWLEntity)object).getIRI(), Imports.EXCLUDED).contains(object);
		if (object instanceof OWLAxiom)
			return onto.containsAxiom(((OWLAxiom)object), Imports.EXCLUDED, AxiomAnnotations.IGNORE_AXIOM_ANNOTATIONS);
		return false;
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
