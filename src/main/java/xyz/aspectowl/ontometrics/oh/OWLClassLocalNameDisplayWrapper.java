/**
 * 
 */
package xyz.aspectowl.ontometrics.oh;

import org.semanticweb.owlapi.model.OWLClass;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * @author ralph
 */
public class OWLClassLocalNameDisplayWrapper extends OWLClassImpl {

	OWLClass clazz;
	
	/**
	 * 
	 */
	public OWLClassLocalNameDisplayWrapper(OWLClass clazz) {
		super(clazz.getIRI());
		this.clazz = clazz;
	}
	
	/**
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLObjectImpl#toString()
	 */
	@Override
	public String toString() {
		return getIRI().getShortForm();
	}
	
	
	
//	/* (non-Javadoc)
//	 * @see java.lang.Object#equals(java.lang.Object)
//	 */
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof OWLClassLocalNameDisplayWrapper) {
//			return clazz.equals(((OWLClassLocalNameDisplayWrapper)obj).clazz);
//		}
//		return false;
//	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return clazz.hashCode()*13;
	}
}
