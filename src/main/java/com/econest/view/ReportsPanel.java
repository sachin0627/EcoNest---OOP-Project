package com.econest.view;

import com.econest.controller.ReportController;
import com.econest.util.CSVUtil;
import com.econest.util.PDFUtil;
import com.econest.util.UITheme;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

/**
 * View panel for Module 07: Reports & Analytics.
 */
public class ReportsPanel extends JPanel {
    private final ReportController controller;

    private JComboBox<String> cmbReportType;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JPanel chartContainer;
    private JComboBox<String> cmbChartType;

    public ReportsPanel() {
        this.controller = new ReportController();
        initUI();
        loadReport();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.COLOR_BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // Top Control Bar
        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new BorderLayout(14, 0));

        // Left Controls
        JPanel leftControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftControls.setOpaque(false);

        JLabel lblSelect = new JLabel("Report Type:");
        lblSelect.setFont(UITheme.FONT_BODY_BOLD);

        String[] reportTypes = {
            "Monthly Sales Report",
            "Product Sales Report",
            "Inventory Report",
            "Low Stock Report",
            "Delivery Performance Report",
            "Client Order Report"
        };
        cmbReportType = new JComboBox<>(reportTypes);
        cmbReportType.setFont(UITheme.FONT_BODY);
        cmbReportType.addActionListener(e -> loadReport());

        JLabel lblChart = new JLabel("Chart:");
        lblChart.setFont(UITheme.FONT_BODY_BOLD);
        String[] chartTypes = {"Monthly Revenue Bar Chart", "Product Category Pie Chart"};
        cmbChartType = new JComboBox<>(chartTypes);
        cmbChartType.setFont(UITheme.FONT_BODY);
        cmbChartType.addActionListener(e -> updateChart());

        JButton btnGenerate = UITheme.createSecondaryButton("Generate");
        btnGenerate.addActionListener(e -> loadReport());

        leftControls.add(lblSelect);
        leftControls.add(cmbReportType);
        leftControls.add(lblChart);
        leftControls.add(cmbChartType);
        leftControls.add(btnGenerate);
        topBar.add(leftControls, BorderLayout.WEST);

        // Right Actions (Export PDF, CSV)
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JButton btnExportPDF = UITheme.createPrimaryButton("Export Report PDF");
        btnExportPDF.addActionListener(e -> exportPDF());

        JButton btnExportCSV = UITheme.createSecondaryButton("Export CSV");
        btnExportCSV.addActionListener(e -> exportCSV());

        rightControls.add(btnExportPDF);
        rightControls.add(btnExportCSV);
        topBar.add(rightControls, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Center Split: Chart on Top, Table on Bottom
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        // Top Chart Container
        chartContainer = UITheme.createCardPanel();
        chartContainer.setLayout(new BorderLayout());
        chartContainer.setPreferredSize(new Dimension(800, 260));
        splitPane.setTopComponent(chartContainer);

        // Bottom Report Table Card
        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        reportTable = new JTable(tableModel);
        UITheme.styleTable(reportTable);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableCard.add(scrollPane, BorderLayout.CENTER);
        splitPane.setBottomComponent(tableCard);

        add(splitPane, BorderLayout.CENTER);

        updateChart();
    }

    public void loadReport() {
        String reportType = (String) cmbReportType.getSelectedItem();
        DefaultTableModel model = controller.getReportTable(reportType, null, null);
        reportTable.setModel(model);
        UITheme.styleTable(reportTable);
    }

    private void updateChart() {
        chartContainer.removeAll();
        String selected = (String) cmbChartType.getSelectedItem();
        JFreeChart chart;

        if ("Product Category Pie Chart".equalsIgnoreCase(selected)) {
            chart = controller.getCategoryPieChart();
        } else {
            chart = controller.getMonthlySalesChart();
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(Color.WHITE);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.revalidate();
        chartContainer.repaint();
    }

    private void exportPDF() {
        String reportType = (String) cmbReportType.getSelectedItem();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_" + reportType.replace(" ", "_") + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                PDFUtil.generateReportPDF(reportType, "EcoNest Retail Management System Analytics", reportTable.getModel(), chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Report exported to PDF successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        String reportType = (String) cmbReportType.getSelectedItem();
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_" + reportType.replace(" ", "_") + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CSVUtil.exportTableToCSV(reportTable, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Report data exported to CSV successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
