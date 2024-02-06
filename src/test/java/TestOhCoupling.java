import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import xyz.aspectowl.ontometrics.cohesion.CohesionCouplingMetric;
import xyz.aspectowl.ontometrics.cohesion.Kumar2017;
import xyz.aspectowl.ontometrics.cohesion.Oh2011;
import xyz.aspectowl.ontometrics.test.cohesion.TestBase;

/**
 * @author Ralph Schäfermeier
 */
public class TestOhCoupling extends TestBase {
  @Test
  public void testCoupling() {
    Stream.of("/kumar/5/all.owl", "/kumar/6/all.owl")
        .forEach(path -> testCoupling(loadOntology(path)));
  }

  private void testCoupling(OWLOntology onto) {
    testCoupling(onto, new Oh2011());
    testCoupling(onto, new Kumar2017());
  }

  private void testCoupling(OWLOntology onto, CohesionCouplingMetric metric) {
    metric.setModularizedOntology(onto);
    onto.getOWLOntologyManager()
        .importsClosure(onto)
        .forEach(
            module ->
                System.out.format(
                    "%s - %s: coup=%f%n",
                    module.getOntologyID().getOntologyIRI().get().getShortForm(),
                    metric.getClass().getSimpleName(),
                    metric.getCoupling(module)));
  }
}
