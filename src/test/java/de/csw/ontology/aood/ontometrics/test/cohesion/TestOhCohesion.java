/**
 * 
 */
package de.csw.ontology.aood.ontometrics.test.cohesion;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import de.csw.ontology.aood.ontometrics.Util;
import de.csw.ontology.aood.ontometrics.oh.OhCohesion;

/**
 * The tests in this test case are taken from the examples in the paper by Oh et al.
 * 
 * @author ralph
 */
public class TestOhCohesion {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// onto = Util.loadOntology(new
		// File("/Users/ralph/Documents/Diss/Ontologien/CSC_Studie/Modularization/obo-all/go.owl"));
		// onto = Util.loadOntology(new
		// File(URI.create(TestOhCohesion.class.getResource("/person.ofn").toString())));
	}

	@Test
	public void testCohesionOhM1() throws OWLOntologyCreationException {
		OhCohesion oh = new OhCohesion(Util.loadOntology(
				new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization1/M1.owl").toString()))));
		assert (oh.cohesion() == 7d / 12d);
	}

	@Test
	public void testCohesionOhM2() throws OWLOntologyCreationException {

		OhCohesion oh = new OhCohesion(Util.loadOntology(
				new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization1/M2.owl").toString()))));
		assert (oh.cohesion() == 1d);
	}
 
	@Test
	public void testCohesionOhM3() throws OWLOntologyCreationException {

		OhCohesion oh = new OhCohesion(Util.loadOntology(
				new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization1/M3.owl").toString()))));
		assert (oh.cohesion() == 1d);
	}

	@Test
	public void testCohesionOhMa() throws OWLOntologyCreationException {

		OhCohesion oh = new OhCohesion(Util.loadOntology(
				new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization2/Ma.owl").toString()))));
		assert (oh.cohesion() == 2d / 3d);
	}

	@Test
	public void testCohesionOhMb() throws OWLOntologyCreationException {
		OhCohesion oh = new OhCohesion(Util.loadOntology(
				new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization2/Mb.owl").toString()))));
		assert (oh.cohesion() == 11d / 20d);
	}


	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

}
