/**
 * 
 */
package de.csw.ontology.aood.ontometrics.oh;

import java.util.Map;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;
import com.jgraph.layout.organic.JGraphFastOrganicLayout;

import de.csw.ontology.aood.ontometrics.Cohesion;

/**
 * Oh et al. only look at class relations. They make a distinction between
 * hierarchical and non-hierarchical class relations and weigh them differently.
 * 
 * Hierarchical class relations are sub-/superclass relations.
 * 
 * Non-hierarchical class relations are:
 * 
 * ClassExpression :=
    Class |
    ObjectIntersectionOf | ObjectUnionOf | ObjectComplementOf | ObjectOneOf |
    ObjectSomeValuesFrom | ObjectAllValuesFrom | ObjectHasValue | ObjectHasSelf |
    ObjectMinCardinality | ObjectMaxCardinality | ObjectExactCardinality |
    DataSomeValuesFrom | DataAllValuesFrom | DataHasValue |
    DataMinCardinality | DataMaxCardinality | DataExactCardinality


 * 
 * @author ralph
 */
public class OhCohesion extends Cohesion {

	private OWLOntology onto;
	
	DirectedGraph<OWLClassLocalNameDisplayWrapper, DefaultEdge> hieriarchicalClassRelations;
	UndirectedGraph<OWLClassLocalNameDisplayWrapper, DefaultEdge> nonHieriarchicalClassRelations;
	
	/**
	 * 
	 */
	public OhCohesion(OWLOntology onto) {
		this.onto = onto;
		hieriarchicalClassRelations = new SimpleDirectedGraph<OWLClassLocalNameDisplayWrapper, DefaultEdge>(DefaultEdge.class);
		nonHieriarchicalClassRelations = new SimpleGraph<OWLClassLocalNameDisplayWrapper, DefaultEdge>(DefaultEdge.class);

		onto.classesInSignature(Imports.EXCLUDED).forEach(clazz -> {
			onto.axioms(clazz).forEach( axiom -> {
				if (axiom.isOfType(Stream.of(AxiomType.SUBCLASS_OF))) {
					OWLClassExpression superClassExp = ((OWLSubClassOfAxiom)axiom).getSuperClass();
					if (superClassExp instanceof OWLClass) {
						processRelation(hieriarchicalClassRelations, (OWLClass)superClassExp, clazz);
					} else {
						((OWLAnonymousClassExpression)superClassExp).classesInSignature().forEach(superClass -> processRelation(nonHieriarchicalClassRelations, clazz, superClass));
					}
				} else if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					((OWLEquivalentClassesAxiom)axiom).classesInSignature().forEach(equivalentClass -> processRelation(nonHieriarchicalClassRelations, clazz, equivalentClass));
				} else if (axiom.isOfType(AxiomType.DISJOINT_CLASSES)) {
					((OWLDisjointClassesAxiom)axiom).classesInSignature().forEach(disjointClass -> processRelation(nonHieriarchicalClassRelations, clazz, disjointClass));
				}
			});
		});
	}

	public double cohesion() {
		double classCount = onto.classesInSignature().count();
		return classCount == 1 ? 1d : sr() / (classCount * (classCount - 1)) * 2d;
	}
	
	private double sr() {
		return hieriarchicalClassRelations.vertexSet().stream()
				.flatMap(v1 -> hieriarchicalClassRelations.vertexSet().stream()
						.map(v2 -> new DijkstraShortestPath<OWLClassLocalNameDisplayWrapper, DefaultEdge>(hieriarchicalClassRelations, v1, v2).getPathLength()))
				.filter(d -> d != 0d && d != Double.POSITIVE_INFINITY)
				.mapToDouble(x -> 1d/x)
				.sum();
	}
	
	public double coupling() {
		return 0d;
	}
	
	private void processRelation(Graph<OWLClassLocalNameDisplayWrapper, DefaultEdge> g, OWLClass c1, OWLClass c2) {
		if (c1.equals(c2))
			return;
		
		OWLClassLocalNameDisplayWrapper cl1 = new OWLClassLocalNameDisplayWrapper(c1);
		OWLClassLocalNameDisplayWrapper cl2 = new OWLClassLocalNameDisplayWrapper(c2);
		
		g.addVertex(cl1);
		g.addVertex(cl2);
		g.addEdge(cl1, cl2);
	}

}
