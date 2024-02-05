package xyz.aspectowl.ontometrics.util;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Ralph Schäfermeier
 */
public class GraphRenderer {
  public static void exportToGml(Graph<OWLEntity, DefaultWeightedEdge> g, OWLOntology onto) {
    GraphMLExporter<OWLEntity, DefaultWeightedEdge> exporter = new GraphMLExporter<>();

    exporter.setExportEdgeWeights(true);
    exporter.setVertexIDProvider(OWLEntity::toStringID);
    exporter.setVertexLabelProvider(e -> e.getIRI().getShortForm());

    try {
      var fileWriter =
          new BufferedWriter(
              new FileWriter(
                  "/tmp/"
                      + onto.getOntologyID().getOntologyIRI().get().getShortForm()
                      + ".graphml"));
      exporter.exportGraph(g, fileWriter);
    } catch (IOException | ExportException e) {
      System.err.println(
          String.format(
              "Could not export graph %s to gml. %s",
              onto.getOntologyID().getOntologyIRI().get().getShortForm(), e.getMessage()));
    }
  }
}
