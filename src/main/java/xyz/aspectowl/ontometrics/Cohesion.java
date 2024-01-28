/**
 * 
 */
package xyz.aspectowl.ontometrics;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.ontometrics.metrics.OntologyModule;
import xyz.aspectowl.ontometrics.metrics.OntologyModuleBase;
import xyz.aspectowl.ontometrics.metrics.OntologyModuleFactory;


/**
 * Oh2011 et al. only look at class relations. They make a distinction between
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
 * @author Ralph Schaefermeier
 */
public class Cohesion {

	private OWLOntology baseOnto;

	private HashSetValuedHashMap<OWLObject, OntologyModule> ontologyModulesByEntity = new HashSetValuedHashMap<>();
	
	private Set<OWLObject> internalSignature;
	private HashSet<OWLObject> mergedExternalSignatures;
	
	/**
	 * 
	 * @param baseOnto The uppermost ontology in the import tree 
	 */
	public Cohesion(OWLOntology baseOnto, Class<? extends OntologyModule> moduleClass) {
		this.baseOnto = baseOnto;
		
		// Fore each entity, store all ontology modules where the entity appears
		// in the signature
		
		OWLOntologyManager om = baseOnto.getOWLOntologyManager();

    om.ontologies()
        .forEach(
            ontology -> {
                OntologyModule module = OntologyModuleFactory.getInstance().createOntologyModule(moduleClass, ontology);
                ontology
                  .classesInSignature(Imports.EXCLUDED)
                  .forEach(clazz -> ontologyModulesByEntity.put(clazz, module));
              ontology
                  .individualsInSignature(Imports.EXCLUDED)
                  .forEach(individual -> ontologyModulesByEntity.put(individual, module));
              ontology
                  .axioms(Imports.EXCLUDED)
                  .forEach(axiom -> ontologyModulesByEntity.put(axiom, module));
            });

		baseOnto.classesInSignature(Imports.INCLUDED).forEach(clazz -> {
			baseOnto.axioms(clazz, Imports.INCLUDED).forEach(axiom -> {
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
		
		baseOnto.individualsInSignature(Imports.INCLUDED).forEach(individual -> {
			baseOnto.axioms(individual, Imports.INCLUDED).forEach(axiom -> {
				if (axiom.isOfType(Stream.of(AxiomType.CLASS_ASSERTION))) {
					OWLClassExpression typeClassExp = ((OWLClassAssertionAxiom)axiom).getClassExpression();
					if (typeClassExp instanceof OWLClass) {
						processHierarchicalClassRelation((OWLClass)typeClassExp, individual);
					} else {
						((OWLAnonymousClassExpression)typeClassExp).classesInSignature().forEach(typeClass -> processNonHierarchicalClassRelation(individual, typeClass));
					}
				} else if (axiom.isOfType(AxiomType.SAME_INDIVIDUAL)) {
					((OWLSameIndividualAxiom)axiom).individualsInSignature().forEach(sameIndividual -> processNonHierarchicalClassRelation(individual, sameIndividual));
				} else if (axiom.isOfType(AxiomType.DIFFERENT_INDIVIDUALS)) {
					((OWLDifferentIndividualsAxiom)axiom).individualsInSignature().forEach(differentIndividual -> processNonHierarchicalClassRelation(individual, differentIndividual));
				} else if (axiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
					((OWLObjectPropertyAssertionAxiom)axiom).individualsInSignature().forEach(connectedIndividual -> processNonHierarchicalClassRelation(individual, connectedIndividual));
				}
			});
		});
		
		baseOnto.axioms(Imports.INCLUDED).forEach(axiom -> {
			axiom.annotations().forEach(annotation -> {
				processNonHierarchicalClassRelation(axiom, annotation.getValue());
			});
		});
		
	}
	
	private void processHierarchicalClassRelation(OWLObject c1, OWLObject c2) {
		if (c1.equals(c2))
			return;

		ontologyModulesByEntity.get(c1).stream().forEach(sourceModule -> {
			if (!sourceModule.contains(c2)) {
				sourceModule.processExternalHierarchicalRelation(c1, c2);
			}
		});

		// the other way round
		ontologyModulesByEntity.get(c2).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c1)) {
				// internal relation
				sourceModule.processInternalHierarchicalRelation(c2, c1);
			} else {
				sourceModule.processExternalHierarchicalRelation(c2, c1);
			}
		});

	}

	private void processNonHierarchicalClassRelation(OWLObject c1, OWLObject c2) {
		if (c1.equals(c2))
			return;
		
		ontologyModulesByEntity.get(c1).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c2)) {
				// internal relation
				sourceModule.processInternalNonHierarchicalRelation(c1, c2);
			} else {
				sourceModule.processExternalNonHierarchicalRelation(c1, c2);
			}
		});

		// the other way round
		ontologyModulesByEntity.get(c2).stream().forEach(sourceModule -> {
			if (sourceModule.contains(c1)) {
				// internal relation
				sourceModule.processInternalNonHierarchicalRelation(c2, c1);
			} else {
				sourceModule.processExternalNonHierarchicalRelation(c2, c1);
			}
		});

	}
	
	public Stream<OntologyModule> modules() {
		return ontologyModulesByEntity.values().stream().distinct();
	}
	
}
