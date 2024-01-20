/**
 * 
 */
package xyz.aspectowl.ontometrics;

import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.HashMultimap;

/**
 * @author ralph
 */
public class ModuleMetrics {

	private OWLOntology onto;
	private Set<OWLOntology> imports;
	
	public ModuleMetrics(OWLOntology onto) {
		this.onto = onto;
	}

	private void doIt() {
		OWLOntologyManager om = onto.getOWLOntologyManager();
		imports = om.getImports(onto);
		int importCount = imports.size();
		
		HashMultimap<OWLClass, OWLClass> toInternalClasses = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> toExternalClasses = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> fromInternalClasses = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> fromExternalClasses = HashMultimap.create();
		
		HashMultimap<OWLClass, OWLClass> toInternalInstances = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> toExternalInstances = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> fromInternalInstances = HashMultimap.create();
		HashMultimap<OWLClass, OWLClass> fromExternalInstances = HashMultimap.create();
		
	}
	

	
}
