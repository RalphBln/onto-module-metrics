package xyz.aspectowl.ontometrics.metrics;

import org.semanticweb.owlapi.model.OWLOntology;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Ralph Schäfermeier
 */
public class OntologyModuleFactory {

  private static OntologyModuleFactory instance;

  public static OntologyModuleFactory getInstance() {
    if (instance == null) {
      instance = new OntologyModuleFactory();
    }
    return instance;
  }

  public OntologyModule createOntologyModule(
      Class<? extends OntologyModule> moduleClass, OWLOntology ontology) {
    try {
      return moduleClass.getDeclaredConstructor(OWLOntology.class).newInstance(ontology);
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      throw new RuntimeException(e);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
