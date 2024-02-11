package xyz.aspectowl.ontometrics.test.cohesion;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import xyz.aspectowl.ontometrics.cohesion.Kumar2017;
import xyz.aspectowl.ontometrics.cohesion.Oh2011;

/**
 * @author Ralph Schäfermeier
 */
public class TestAspects extends TestBase {

  @Test
  public void aspects1() {
    var oh = new Oh2011();
    var onto = loadOntology("/aspects/kumar-case1.owl");
    oh.addModule(onto);
    oh.getCohesion(onto);
  }

  @Test
  public void aspectsCsw() {
    var o1 = loadOntology("/csw/orig_module1.owl");
    var c = loadOntology("/csw/time.owl");
    var a1 = loadOntology("/csw/aspect_module1.owl");
    var aa = loadOntology("/csw/aspect_aspect.owl");

    var kumarAspect = new Kumar2017();
    var kumarOrig = new Kumar2017();

    kumarAspect.addModule(c);
    kumarAspect.addModule(a1);
    kumarAspect.addModule(aa);

    kumarOrig.addModule(c);
    kumarOrig.addModule(o1);

    System.out.println("\nAspectOWL\n");

    Stream.of(c, a1, aa)
        .forEach(
            o ->
                System.out.printf(
                    "%s\tCohesion: %f\tCoupling: %f%n",
                    o.getOntologyID().getOntologyIRI().get().getShortForm(),
                    kumarAspect.getCohesion(o),
                    kumarAspect.getCoupling(o)));

    System.out.println("\nOriginal\n");

    Stream.of(c, o1)
        .forEach(
            o ->
                System.out.printf(
                    "%s\tCohesion: %f\tCoupling: %f%n",
                    o.getOntologyID().getOntologyIRI().get().getShortForm(),
                    kumarOrig.getCohesion(o),
                    kumarOrig.getCoupling(o)));
  }
}
