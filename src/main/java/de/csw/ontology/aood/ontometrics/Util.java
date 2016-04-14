/**
 * 
 */
package de.csw.ontology.aood.ontometrics;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author ralph
 */
public class Util {

	public static Set<OWLOntology> loadOntologies(File dir) {

		OWLOntologyManager om = createOntologyManager(dir);

		HashSet<OWLOntology> result = new HashSet<OWLOntology>();
		
		File[] ontoFiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isFile()) {
					String fileName = file.getName().toLowerCase();
					if (fileName.endsWith(".owl") ||
						fileName.endsWith(".ofn")) {
						return true;
					}
				}
				return false;
			}
		});
		
		for (File ontoFile : ontoFiles) {
			try {
				OWLOntology onto = om.loadOntologyFromOntologyDocument(ontoFile);
				result.add(onto);
			} catch (OWLOntologyCreationException e) {
				System.err.println("Error loading ontology file " + ontoFile.getAbsolutePath() + ". " + e.getMessage());
			}
		}
		
		return result;
	}
	
	public static OWLOntology loadOntology(File file) throws OWLOntologyCreationException {
		OWLOntologyManager om = createOntologyManager(file);
		return om.loadOntologyFromOntologyDocument(file);
	}
	
	private static OWLOntologyManager createOntologyManager(File file) {
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		
		OWLOntologyLoaderConfiguration config = om.getOntologyLoaderConfiguration();
		config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
		config = config.setStrict(false);
		config = config.setLoadAnnotationAxioms(false);
		
		om.setOntologyLoaderConfiguration(config);
		
		Set<OWLOntologyIRIMapper> catalogIRIMappers = getCatalogIRIMappers(file);
		om.setIRIMappers(catalogIRIMappers);
		
		return om;
	}
	
	private static Set<OWLOntologyIRIMapper> getCatalogIRIMappers(File dir) {
		if (dir.isFile()) {
			dir = dir.getParentFile();
		}
		
		Set<OWLOntologyIRIMapper> catalogIRIMappers = new HashSet<OWLOntologyIRIMapper>();
		
		File[] catalogFiles = dir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isFile() && file.getName().startsWith("catalog") && file.getName().endsWith(".xml");
			}
		});
		
		for (File catalogFile : catalogFiles) {
			try {
				catalogIRIMappers.add(new XMLCatalogIRIMapper(catalogFile));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return catalogIRIMappers;
	}
	
}
