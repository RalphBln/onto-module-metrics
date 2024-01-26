/**
 * 
 */
package xyz.aspectowl.ontometrics.ecai2016;

import java.io.File;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import xyz.aspectowl.ontometrics.util.loader.OntologyModuleLoader;
import xyz.aspectowl.ontometrics.Cohesion;

/**
 * @author Ralph Schaefermeier
 */
public class ECAI2016EvaluationOutput {

	public static void main(String[] args) {
		try {
			System.out.println("module\tcoh\tcpl h\tcpl nh\tcpl");
			System.out.println("Contextual Projection - Pattern");
			OWLOntology onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/contextual-projection/pattern/projection.owl"));
			Cohesion oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			System.out.println("Contextual Projection - Aspects");
			onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/contextual-projection/aspect/aspect.owl"));
			oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			System.out.println("View Inheritance - Patterns");
			onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/view-inheritance/pattern/pattern.owl"));
			oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			System.out.println("View Inheritance - Aspects");
			onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/view-inheritance/aspect/aspect.owl"));
			oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			System.out.println("n-ary - Pattern");
			onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/n-ary/pattern/pattern.owl"));
			oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			System.out.println("n-ary - Aspects");
			onto = OntologyModuleLoader.loadOntology(new File("/Users/ralph/Documents/Arbeit/LaTeX/ECAI2016/experiments/n-ary/aspect/aspect.owl"));
			oh = new Cohesion(onto);
			oh.modules().forEach(module -> {
				System.out.println(module.getName() + "\t" + module.cohesion() + "\t" + module.couplingHierarchical() + "\t" + module.couplingNonHierarchical() +  "\t" + module.coupling());
			});
			
			
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}

	}
}
