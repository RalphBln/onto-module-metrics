/**
 * 
 */
package de.csw.ontology.aood.ontometrics.oh;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

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

	private OWLOntology baseOnto;

	private HashSetValuedHashMap<OWLClass, OntologyModule> ontologyModulesByClass = new HashSetValuedHashMap<OWLClass, OntologyModule>();
	
	private Set<OWLClass> internalSignature;
	private HashSet<OWLClass> mergedExternalSignatures;
	
	/**
	 * 
	 * @param baseOnto The uppermost ontology in the import tree 
	 */
	public OhCohesion(OWLOntology baseOnto) {
		this.baseOnto = baseOnto;
		
		// Fore each class, store all ontology modules where the class appears
		// in the signature
		
		OWLOntologyManager om = baseOnto.getOWLOntologyManager();
		
		om.ontologies()
		.forEach(ontology -> {
			OntologyModule module = new OntologyModule(ontology);
			ontology.classesInSignature(Imports.EXCLUDED)
			.forEach(clazz -> ontologyModulesByClass.put(clazz, module));
		});
		
		baseOnto.classesInSignature(Imports.INCLUDED).forEach(clazz -> {
			baseOnto.axioms(clazz, Imports.INCLUDED).forEach( axiom -> {
				if (axiom.isOfType(Stream.of(AxiomType.SUBCLASS_OF))) {
					OWLClassExpression superClassExp = ((OWLSubClassOfAxiom)axiom).getSuperClass();
					if (superClassExp instanceof OWLClass) {
						processHierarchicalClassRelation((OWLClass)superClassExp, clazz);
					} else {
						((OWLAnonymousClassExpression)superClassExp).classesInSignature().forEach(superClass -> processNonHierarchicalClassRelation(clazz, superClass));
					}
				} else if (axiom.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
					((OWLEquivalentClassesAxiom)axiom).classesInSignature().forEach(equivalentClass -> processNonHierarchicalClassRelation(clazz, equivalentClass));
				} else if (axiom.isOfType(AxiomType.DISJOINT_CLASSES)) {
					((OWLDisjointClassesAxiom)axiom).classesInSignature().forEach(disjointClass -> processNonHierarchicalClassRelation(clazz, disjointClass));
				}
			});
		});
		
	}
	
	private void processHierarchicalClassRelation(OWLClass c1, OWLClass c2) {
		if (c1.equals(c2))
			return;

		ontologyModulesByClass.get(c1).stream().forEach(sourceModule -> {
			if (!sourceModule.contains(c2)) {
				sourceModule.processExternalHierarchicalRelation(c1, c2);
			}
		});

		// the other way round
		ontologyModulesByClass.get(c2).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c1)) {
				// internal relation
				sourceModule.processInternalHierarchicalRelation(c2, c1);
			} else {
				sourceModule.processExternalHierarchicalRelation(c2, c1);
			}
		});

	}

	private void processNonHierarchicalClassRelation(OWLClass c1, OWLClass c2) {
		if (c1.equals(c2))
			return;
		
		ontologyModulesByClass.get(c1).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c2)) {
				// internal relation
				sourceModule.processInternalNonHierarchicalRelation(c1, c2);
			} else {
				sourceModule.processExternalNonHierarchicalRelation(c1, c2);
			}
		});

		// the other way round
		ontologyModulesByClass.get(c2).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c1)) {
				// internal relation
				sourceModule.processInternalNonHierarchicalRelation(c2, c1);
			} else {
				sourceModule.processExternalNonHierarchicalRelation(c2, c1);
			}
		});

	}
	
	public Stream<OntologyModule> modules() {
		return ontologyModulesByClass.values().stream().distinct();
	}

}
