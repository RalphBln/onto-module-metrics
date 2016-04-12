/**
 * 
 */
package de.csw.ontology.aood.ontometrics.oh;

import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author ralph
 */
public class OWLClassLocalNameDisplayWrapper {

	OWLClass clazz;
	
	/**
	 * 
	 */
	public OWLClassLocalNameDisplayWrapper(OWLClass clazz) {
		this.clazz = clazz;
	}
	
	/**
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLObjectImpl#toString()
	 */
	@Override
	public String toString() {
		return clazz.getIRI().getShortForm();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OWLClassLocalNameDisplayWrapper) {
			return clazz.equals(((OWLClassLocalNameDisplayWrapper)obj).clazz);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return clazz.hashCode();
	}
}
