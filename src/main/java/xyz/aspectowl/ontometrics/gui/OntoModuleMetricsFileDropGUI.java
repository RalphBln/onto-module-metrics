package xyz.aspectowl.ontometrics.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import xyz.aspectowl.ontometrics.cohesion.Kumar2017;
import xyz.aspectowl.ontometrics.util.loader.OntologyModuleLoader;

public class OntoModuleMetricsFileDropGUI extends JFrame {

  private JTable filesTable;
  private DefaultTableModel tableModel;
  private JScrollPane scrollPane;
  private JPanel contentPane;
  private JPanel glassPane;
  private FileDropController controller;

  private JLabel statusLabel;

  public OntoModuleMetricsFileDropGUI() {
    setTitle("File Drop GUI");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 400);

    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    contentPane.setLayout(new BorderLayout(0, 0));
    setContentPane(contentPane);

    glassPane = new JPanel();
    glassPane.setLayout(new BorderLayout(0, 0));
    glassPane.add(
        new JLabel(
            new StretchIcon(OntoModuleMetricsFileDropGUI.class.getResource("/icons/loading.gif"))),
        BorderLayout.CENTER);
    glassPane.setVisible(false);
    setGlassPane(glassPane);

    filesTable = new JTable();
    tableModel = new DefaultTableModel(new Object[] {"Module", "Cohesion", "Coupling"}, 0);
    filesTable.setModel(tableModel);

    scrollPane = new JScrollPane(filesTable);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    var menuBar = new JMenuBar();
    var latexMenu = new JMenu("LaTeX");
    var latexMenuItem = getLatexMenuItem();
    latexMenu.add(latexMenuItem);
    menuBar.add(latexMenu);
    setJMenuBar(menuBar);

    statusLabel = new JLabel("Drop OWL files...");
    contentPane.add(statusLabel, BorderLayout.SOUTH);

    controller = new FileDropController(this);

    new DropTarget(contentPane, DnDConstants.ACTION_COPY, controller);

    setVisible(true);
  }

  private static JMenuItem getLatexMenuItem() {
    var tableMenuItem = new JMenuItem("Copy LaTex code for table");
    tableMenuItem.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            StringBuilder latexTable = new StringBuilder();
            latexTable.append("\\begin{table}[htbp]\n");
            latexTable.append("\\centering\n");
            latexTable.append("\\begin{tabular}{l l r r}\n");
            latexTable.append("Ontology & Module & Cohesion & Coupling\\\\\n");
            latexTable.append("\\hline\n");
            latexTable.append("\\addlinespace\n");

            latexTable.append("%\n");
            latexTable.append("% Insert rows here.\n");
            latexTable.append("%\n");

            latexTable.append("\\end{tabular}\n");
            latexTable.append("\\caption{File Information}\n");
            latexTable.append("\\label{tab:file_info}\n");
            latexTable.append("\\end{table}\n");

            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(latexTable.toString()), null);
          }
        });
    return tableMenuItem;
  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new OntoModuleMetricsFileDropGUI());
  }

  public void setStatus(String status) {
    statusLabel.setText(status);
  }

  public DefaultTableModel getTableModel() {
    return tableModel;
  }

  public void setTableModel(DefaultTableModel tableModel) {
    this.tableModel = tableModel;
  }
}

class FileDropController implements DropTargetListener {

  private static final DecimalFormat DECIMAL_FORMAT;

  static {
    DecimalFormatSymbols decimalSymbols = DecimalFormatSymbols.getInstance();
    decimalSymbols.setDecimalSeparator('.');
    DECIMAL_FORMAT = new DecimalFormat("0.00", decimalSymbols);
  }

  private OntoModuleMetricsFileDropGUI view;
  private LatexEscaper latexEscaper = new LatexEscaper();

  public FileDropController(OntoModuleMetricsFileDropGUI view) {
    this.view = view;
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {}

  @Override
  public void dragOver(DropTargetDragEvent dtde) {}

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {}

  @Override
  public void dragExit(DropTargetEvent dte) {}

  @Override
  public void drop(DropTargetDropEvent dtde) {

    try {

      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      Transferable transferable = dtde.getTransferable();
      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
        List<File> fileList =
            (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

        new SwingWorker<Void, Object[]>() {

          @Override
          protected Void doInBackground() throws Exception {

            view.setStatus("Calculating...");

            view.getGlassPane().setVisible(true);

            view.getTableModel().setRowCount(0); // Clear table

            var metric = new Kumar2017();

            fileList.stream()
                .filter(
                    file ->
                        file.isFile()
                            && (file.getName().endsWith(".owl")
                                || file.getName().endsWith(".ofn")
                                || file.getName().endsWith("aofn")))
                .forEach(
                    file -> {
                      try {
                        OntologyModuleLoader.loadOntology(file);
                        metric.addModule(OntologyModuleLoader.loadOntology(file));
                      } catch (OWLOntologyCreationException e) {
                        throw new RuntimeException(e);
                      }
                    });

            metric
                .modules()
                .sorted()
                .forEach(
                    ontology ->
                        publish(
                            new Object[] {
                              ontology.getOntologyID().getOntologyIRI().get().getShortForm(),
                              format(metric.getCohesion(ontology)),
                              format(metric.getCoupling(ontology))
                            }));

            String latexTable = generateLatexTable();
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(latexTable), null);
            return null;
          }

          @Override
          protected void process(List<Object[]> chunks) {
            chunks.forEach(row -> view.getTableModel().addRow(row));
          }

          @Override
          protected void done() {
            view.getGlassPane().setVisible(false);
            view.setStatus("Copied LaTeX table rows with results to the clipboard.");
          }
        }.execute();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private String format(double d) {
    return DECIMAL_FORMAT.format(d);
  }

  private String generateLatexTable() {

    DefaultTableModel tableModel = view.getTableModel();

    StringBuilder latexTable = new StringBuilder();

    latexTable.append(
        tableModel.getDataVector().stream()
            .map(
                row ->
                    row.stream()
                        .map(o -> latexEscaper.escape(o.toString()))
                        .collect(Collectors.joining(" & ")))
            .map(o -> o.toString())
            .collect(Collectors.joining("\\\\\n")));

    return latexTable.toString();
  }
}
