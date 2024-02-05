package xyz.aspectowl.ontometrics.test.cohesion;

import java.io.File;
import java.net.URI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import xyz.aspectowl.ontometrics.util.loader.OntologyModuleLoader;

/**
 * @author Ralph Schäfermeier
 */
public class TestBase {
  public OWLOntology loadOntology(String path) {
    try {
      return OntologyModuleLoader.loadOntology(
          new File(URI.create(getClass().getResource(path).toString())));
    } catch (OWLOntologyCreationException e) {
      throw new RuntimeException(e);
    }
  }
}
