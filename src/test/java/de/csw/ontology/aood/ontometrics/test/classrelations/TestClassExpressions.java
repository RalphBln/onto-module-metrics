/**
 * 
 */
package de.csw.ontology.aood.ontometrics.test.classrelations;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;

import de.csw.ontology.aood.ontometrics.Util;
import de.csw.ontology.aood.ontometrics.oh.OhCohesion;

/**
 * @author ralph
 */
public class TestClassExpressions {
	
	private OWLOntology onto;
	private OhCohesion oh;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		onto = Util.loadOntology(new File(URI.create(TestClassExpressions.class.getResource("/person.ofn").toString())));
//		onto = Util.loadOntology(new File(URI.create(TestClassExpressions.class.getResource("/oh/modularization1/M1.owl").toString())));
		oh = new OhCohesion(onto);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// not much to do here right now..
	}

	@Test
	public void test() {
		oh.doIt();
	}

}
