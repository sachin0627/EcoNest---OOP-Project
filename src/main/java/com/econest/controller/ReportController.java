package com.econest.controller;

import com.econest.service.ReportService;
import org.jfree.chart.JFreeChart;

import javax.swing.table.DefaultTableModel;

/**
 * Controller for Module 07: Reports & Analytics.
 */
public class ReportController {
    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    public JFreeChart getMonthlySalesChart() {
        return reportService.createMonthlySalesChart();
    }

    public JFreeChart getCategoryPieChart() {
        return reportService.createCategoryDistributionChart();
    }

    public DefaultTableModel getReportTable(String reportType, String startDate, String endDate) {
        return reportService.generateReportTableModel(reportType, startDate, endDate);
    }
}
