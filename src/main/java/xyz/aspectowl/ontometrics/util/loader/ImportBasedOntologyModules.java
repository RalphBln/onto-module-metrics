/** */
package xyz.aspectowl.ontometrics.util.loader;

import java.io.File;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Ralph Schaefermeier
 */
public class ImportBasedOntologyModules implements OntologyModules {

  private OWLOntologyManager om;

  /** */
  public ImportBasedOntologyModules(File baseOntology) {
    om = OWLManager.createOWLOntologyManager();
  }

  /* (non-Javadoc)
   * @see xyz.aspectowl.ontometrics.OntologyModules#loadAllOntologyModules()
   */
  @Override
  public void loadAllOntologyModules() {}

  /* (non-Javadoc)
   * @see xyz.aspectowl.ontometrics.OntologyModules#modules()
   */
  @Override
  public Stream<OWLOntology> modules() {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see xyz.aspectowl.ontometrics.OntologyModules#getModule(org.semanticweb.owlapi.model.OWLObject)
   */
  @Override
  public OWLOntology getModule(OWLObject obj) {
    // TODO Auto-generated method stub
    return null;
  }
}
