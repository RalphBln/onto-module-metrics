/**
 * 
 */
package xyz.aspectowl.ontometrics;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Represents an ontology modularization. Contains a number of ontologies that
 * represent the modules.
 * 
 * @author ralph
 */
public interface OntologyModules {
	public void loadAllOntologyModules();
	
	public Stream<OWLOntology> modules();
	
	/**
	 * Returns the module (the ontology) in which the given object is defined.
	 * @param obj
	 * @return
	 */
	public OWLOntology getModule(OWLObject obj);
}
