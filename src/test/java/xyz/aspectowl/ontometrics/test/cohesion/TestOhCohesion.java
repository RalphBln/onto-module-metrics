/**
 * 
 */
package xyz.aspectowl.ontometrics.test.cohesion;

import java.io.File;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntology;

import xyz.aspectowl.ontometrics.Util;
import xyz.aspectowl.ontometrics.oh.OhCohesion;

/**
 * The tests in this test case are taken from the examples in the paper by Oh et al.
 * 
 * @author ralph
 */
public class TestOhCohesion {
	
	private Map<String, Double> expectedValues = Collections.unmodifiableMap(Stream.of(
			entry("M1", 7d / 12d),
			entry("M2", 1d),
			entry("M3", 1d),
			entry("Ma", 2d / 3d),
			entry("Mb", 11d / 20d)			
			).collect(entriesToMap()));

	public static <K, V> Map.Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}
	
	public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
    }
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testCohesionM1() throws Exception {
		OWLOntology onto = Util.loadOntology(new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization1/all.owl").toString())));
		testOhCohesion(onto);
	}

	@Test
	public void testCohesionM2() throws Exception {
		OWLOntology onto = Util.loadOntology(new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization2/all.owl").toString())));
		testOhCohesion(onto);
	}

	private void testOhCohesion(OWLOntology onto) {
		OhCohesion oh = new OhCohesion(onto);
		oh.modules().forEach(module -> {
			Double expectedValue = expectedValues.get(module.getName());
			if (expectedValue != null)
				Assert.assertEquals((double)expectedValue, module.cohesion(), 0d);
			System.out.println(module.getName() + ": " + module.couplingHierarchical() + ", " + module.couplingNonHierarchical() + ", " + module.coupling());
		});
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

}
