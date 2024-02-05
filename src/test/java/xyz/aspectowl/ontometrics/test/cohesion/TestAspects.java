package xyz.aspectowl.ontometrics.test.cohesion;

import org.junit.jupiter.api.Test;
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
}
