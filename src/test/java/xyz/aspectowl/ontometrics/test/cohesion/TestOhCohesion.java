/** */
package xyz.aspectowl.ontometrics.test.cohesion;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import xyz.aspectowl.ontometrics.Cohesion;
import xyz.aspectowl.ontometrics.cohesion.CohesionCouplingMetric;
import xyz.aspectowl.ontometrics.cohesion.Kumar2017;
import xyz.aspectowl.ontometrics.metrics.impl.Oh2011;
import xyz.aspectowl.ontometrics.util.loader.OntologyModuleLoader;

/**
 * The tests in this test case are taken from the examples in the paper by Oh2011 et al.
 *
 * @author Ralph Schaefermeier
 */
public class TestOhCohesion extends TestBase {

  private Map<String, Map<String, Double>> expectedValues =
      Collections.unmodifiableMap(
          Stream.of(
                  entry("M1", Map.of("value", 7d / 12d, "delta", 0d)),
                  entry(
                      "M2",
                      Map.of(
                          "value", 1d,
                          "delta", 0d)),
                  entry(
                      "M3",
                      Map.of(
                          "value", 1d,
                          "delta", 0d)),
                  entry("Ma", Map.of("value", 2d / 3d, "delta", 0d)),
                  entry("Mb", Map.of("value", 11d / 20d, "delta", 0d)),
                  entry(
                      "ontology1",
                      Map.of(
                          "value", .6d,
                          "delta", .01d)),
                  entry(
                      "ontology2",
                      Map.of(
                          "value", .6d,
                          "delta", .01d)),
                  entry(
                      "ontology3",
                      Map.of(
                          "value", .64d,
                          "delta", .01d)),
                  entry(
                      "ontology4",
                      Map.of(
                          "value", .5d,
                          "delta", .01d)),
                  entry(
                      "ontology7",
                      Map.of(
                          "value", .43d,
                          "delta", .01d)),
                  entry(
                      "ontology8",
                      Map.of(
                          "value", .43d,
                          "delta", .01d)),
                  entry(
                      "ontology9",
                      Map.of(
                          "value", .43d,
                          "delta", .01d)))
              .collect(entriesToMap()));

  public static <K, V> Map.Entry<K, V> entry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
    return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
  }

  @Test
  public void testCohesionM1() throws Exception {
    OWLOntology onto =
        OntologyModuleLoader.loadOntology(
            new File(
                URI.create(
                    TestOhCohesion.class.getResource("/oh/modularization1/all.owl").toString())));
    testOhCohesion(onto);
    testOhAndKumarCohesion(onto);
  }

  @Test
  public void testCohesionM2() throws Exception {
    OWLOntology onto =
        OntologyModuleLoader.loadOntology(
            new File(
                URI.create(
                    TestOhCohesion.class.getResource("/oh/modularization2/all.owl").toString())));
    testOhCohesion(onto);
    testOhAndKumarCohesion(onto);
  }

  @Test
  public void testKumarExamples() throws Exception {
    File baseFolder = new File(URI.create(TestOhCohesion.class.getResource("/kumar").toString()));
    Arrays.stream(baseFolder.listFiles(file -> file.isDirectory()))
        .forEach(
            folder ->
                Arrays.stream(
                        folder.listFiles(file -> file.isFile() && file.getName().endsWith(".owl")))
                    .forEach(
                        owlFile -> {
                          try {
                            System.out.format("%n%s%n", owlFile.getName());
                            testOhAndKumarCohesion(OntologyModuleLoader.loadOntology(owlFile));
                          } catch (OWLOntologyCreationException e) {
                            throw new RuntimeException(e);
                          }
                        }));
  }

  @Test
  public void testEquivalences() {
    var onto = loadOntology("/equivalence/equivalence1.owl");
    var oh = new xyz.aspectowl.ontometrics.cohesion.Oh2011();
    oh.addModule(onto);
    System.out.println(oh.getCohesion(onto));
  }

  private void testOhCohesion(OWLOntology onto) {
    Cohesion oh = new Cohesion(onto, Oh2011.class);
    oh.modules()
        .forEach(
            module -> {
              Map<String, Double> expectedValue = expectedValues.get(module.getName());
              if (expectedValue != null)
                Assertions.assertEquals(
                    expectedValue.get("value"), module.cohesion(), expectedValue.get("delta"));
              System.out.format(
                  "%s: coh=%f ; cpH=%f ; cpNH=%f ; cp=%f%n",
                  module.getName(),
                  module.cohesion(),
                  module.couplingHierarchical(),
                  module.couplingNonHierarchical(),
                  module.coupling());
            });
  }

  private void testOhAndKumarCohesion(OWLOntology onto) {
    testCohesion(onto, new xyz.aspectowl.ontometrics.cohesion.Oh2011());
    testCohesion(onto, new Kumar2017());
  }

  private void testCohesion(OWLOntology onto, CohesionCouplingMetric metric) {
    onto.getOWLOntologyManager().ontologies().forEach(o -> metric.addModule(o));
    onto.getOWLOntologyManager()
        .ontologies()
        .forEach(
            module ->
                System.out.format(
                    "%s - %s: coh=%f%n",
                    module.getOntologyID().getOntologyIRI().get().getShortForm(),
                    metric.getClass().getSimpleName(),
                    metric.getCohesion(module)));
  }
}
