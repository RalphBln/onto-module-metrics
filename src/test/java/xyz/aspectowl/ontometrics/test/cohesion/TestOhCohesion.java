/**
 * 
 */
package xyz.aspectowl.ontometrics.test.cohesion;

import java.io.File;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.ontometrics.metrics.impl.Oh2011;
import xyz.aspectowl.ontometrics.util.loader.OntologyModuleLoader;
import xyz.aspectowl.ontometrics.Cohesion;

/**
 * The tests in this test case are taken from the examples in the paper by Oh2011 et al.
 * 
 * @author Ralph Schaefermeier
 */
public class TestOhCohesion {
	
	private Map<String, Map<String, Double>> expectedValues = Collections.unmodifiableMap(Stream.of(
			entry("M1", Map.of(
					"value", 7d / 12d,
					"delta", 0d
			)),
			entry("M2", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("M3", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("Ma", Map.of(
					"value", 2d / 3d,
					"delta", 0d
			)),
			entry("Mb", Map.of(
					"value", 11d / 20d,
					"delta", 0d
			)),
			entry("ontology1", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology2", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology3", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology4", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology7", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology8", Map.of(
					"value", 1d,
					"delta", 0d
			)),
			entry("ontology9", Map.of(
					"value", 1d,
					"delta", 0d
			))
			).collect(entriesToMap()));

	public static <K, V> Map.Entry<K, V> entry(K key, V value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}
	
	public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
        return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
    }

	@Test
	public void testCohesionM1() throws Exception {
		OWLOntology onto = OntologyModuleLoader.loadOntology(new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization1/all.owl").toString())));
		testOhCohesion(onto);
	}

	@Test
	public void testCohesionM2() throws Exception {
		OWLOntology onto = OntologyModuleLoader.loadOntology(new File(URI.create(TestOhCohesion.class.getResource("/oh/modularization2/all.owl").toString())));
		testOhCohesion(onto);
	}

	@Test
	public void testKumarExamples() throws Exception {
		File baseFolder = new File(URI.create(TestOhCohesion.class.getResource("/kumar").toString()));
    Arrays.stream(baseFolder.listFiles(file -> file.isDirectory()))
        .forEach(
            folder ->
                Arrays.stream(
                    folder.listFiles(file -> file.isFile() && file.getName().endsWith(".owl"))).forEach(
							owlFile ->
                            {
                                try {
                                    testOhCohesion(OntologyModuleLoader.loadOntology(owlFile));
                                } catch (OWLOntologyCreationException e) {
                                    throw new RuntimeException(e);
                                }
                            }
						));
	}

	private void testOhCohesion(OWLOntology onto) {
		Cohesion oh = new Cohesion(onto, Oh2011.class);
		oh.modules().forEach(module -> {
			Map<String, Double> expectedValue = expectedValues.get(module.getName());
			if (expectedValue != null)
				Assertions.assertEquals(expectedValue.get("value"), module.cohesion(), expectedValue.get("delta"));
			System.out.println(module.getName() + ": " + module.couplingHierarchical() + ", " + module.couplingNonHierarchical() + ", " + module.coupling());
		});
	}

}
