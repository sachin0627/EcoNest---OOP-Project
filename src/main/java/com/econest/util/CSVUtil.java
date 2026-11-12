package com.econest.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility for exporting JTable datasets and lists to CSV format.
 * 
 * Demonstrates: File I/O, error handling, string escaping, and UI feedback.
 */
public class CSVUtil {

    private CSVUtil() {}

    /**
     * Exports the data of any JTable to a CSV file.
     * @param table source JTable
     * @param targetFile destination CSV file
     * @throws IOException on write error
     */
    public static void exportTableToCSV(JTable table, File targetFile) throws IOException {
        TableModel model = table.getModel();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            // Write column headers
            int colCount = model.getColumnCount();
            for (int i = 0; i < colCount; i++) {
                writer.write(escapeCSV(model.getColumnName(i)));
                if (i < colCount - 1) writer.write(",");
            }
            writer.newLine();

            // Write row data
            int rowCount = model.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                for (int c = 0; c < colCount; c++) {
                    Object val = model.getValueAt(r, c);
                    writer.write(escapeCSV(val == null ? "" : val.toString()));
                    if (c < colCount - 1) writer.write(",");
                }
                writer.newLine();
            }
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
