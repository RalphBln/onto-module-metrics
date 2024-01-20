/**
 * 
 */
package xyz.aspectowl.ontometrics;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author ralph
 */
public class Cohesion {

	/**
	 * 
	 */
	public Cohesion() {
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File(args[0]);
		Set<OWLOntology> ontos = Util.loadOntologies(dir);
	}

}
