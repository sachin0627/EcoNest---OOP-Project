package com.econest.util;

import com.econest.model.Order;
import com.econest.model.OrderItem;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility for generating professional PDF Invoices and Reports using iText.
 * 
 * Demonstrates: Document generation, styling, table formatting, and brand presentation.
 */
public class PDFUtil {

    private static final BaseColor COLOR_PRIMARY = new BaseColor(0x24, 0x5C, 0x43);   // Forest Green
    private static final BaseColor COLOR_SECONDARY = new BaseColor(0x8F, 0xAF, 0x91); // Sage Green
    private static final BaseColor COLOR_BG_LIGHT = new BaseColor(0xF7, 0xF8, 0xF2);  // Cream

    private static final Font FONT_HEADER_TITLE = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_HEADER_SUB = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font FONT_SECTION = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARY);
    private static final Font FONT_BODY = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.BLACK);
    private static final Font FONT_BODY_BOLD = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_TABLE_HEADER = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);

    private PDFUtil() {}

    /**
     * Generates a branded printable PDF Invoice for a Client Order.
     */
    public static void generateInvoicePDF(Order order, String clientName, String clientPhone, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        // Header Table (Company Info Left, Invoice Details Right)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});

        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBorder(Rectangle.NO_BORDER);
        leftHeader.addElement(new Paragraph("ECONEST RETAIL SYSTEM", FONT_HEADER_TITLE));
        leftHeader.addElement(new Paragraph("Sustainable Smart Home & Garden Solutions", FONT_HEADER_SUB));
        leftHeader.addElement(new Paragraph("88, Green Park Way, Colombo 03, Sri Lanka\nTel: +94 11 254 7890 | Web: www.econest.lk", FONT_BODY));
        headerTable.addCell(leftHeader);

        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBorder(Rectangle.NO_BORDER);
        rightHeader.setHorizontalAlignment(Element.ALIGN_RIGHT);
        rightHeader.addElement(new Paragraph("COMMERCIAL INVOICE", FONT_SECTION));
        rightHeader.addElement(new Paragraph("Invoice No: INV-" + order.getOrderId(), FONT_BODY_BOLD));
        rightHeader.addElement(new Paragraph("Order ID: " + order.getOrderId(), FONT_BODY));
        rightHeader.addElement(new Paragraph("Date: " + UITheme.formatDateTime(order.getOrderDate()), FONT_BODY));
        rightHeader.addElement(new Paragraph("Status: " + order.getOrderStatus(), FONT_BODY_BOLD));
        headerTable.addCell(rightHeader);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        // Client & Delivery Info Section
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{50, 50});

        PdfPCell billToCell = new PdfPCell();
        billToCell.setBackgroundColor(COLOR_BG_LIGHT);
        billToCell.setPadding(8);
        billToCell.setBorderColor(COLOR_SECONDARY);
        billToCell.addElement(new Paragraph("BILL TO (CLIENT):", FONT_SECTION));
        billToCell.addElement(new Paragraph("Name: " + (clientName != null ? clientName : order.getClientId()), FONT_BODY_BOLD));
        billToCell.addElement(new Paragraph("Client ID: " + order.getClientId(), FONT_BODY));
        if (clientPhone != null) {
            billToCell.addElement(new Paragraph("Phone: " + clientPhone, FONT_BODY));
        }
        infoTable.addCell(billToCell);

        PdfPCell shipToCell = new PdfPCell();
        shipToCell.setBackgroundColor(COLOR_BG_LIGHT);
        shipToCell.setPadding(8);
        shipToCell.setBorderColor(COLOR_SECONDARY);
        shipToCell.addElement(new Paragraph("DELIVERY & PAYMENT DETAILS:", FONT_SECTION));
        shipToCell.addElement(new Paragraph("Address: " + order.getDeliveryAddress(), FONT_BODY));
        shipToCell.addElement(new Paragraph("Payment: " + order.getPaymentMethod(), FONT_BODY));
        infoTable.addCell(shipToCell);

        document.add(infoTable);
        document.add(new Paragraph(" "));

        // Order Items Table
        PdfPTable itemsTable = new PdfPTable(5);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{10, 45, 15, 15, 15});

        String[] headers = {"#", "Product Description", "Unit Price", "Qty", "Total (LKR)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        int index = 1;
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                PdfPCell cIndex = new PdfPCell(new Phrase(String.valueOf(index++), FONT_BODY));
                cIndex.setHorizontalAlignment(Element.ALIGN_CENTER);
                cIndex.setPadding(5);
                itemsTable.addCell(cIndex);

                String pName = item.getProductName() != null ? item.getProductName() : item.getProductId();
                PdfPCell cName = new PdfPCell(new Phrase(pName, FONT_BODY));
                cName.setPadding(5);
                itemsTable.addCell(cName);

                PdfPCell cPrice = new PdfPCell(new Phrase(String.format("%,.2f", item.getUnitPrice()), FONT_BODY));
                cPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cPrice.setPadding(5);
                itemsTable.addCell(cPrice);

                PdfPCell cQty = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), FONT_BODY));
                cQty.setHorizontalAlignment(Element.ALIGN_CENTER);
                cQty.setPadding(5);
                itemsTable.addCell(cQty);

                PdfPCell cSub = new PdfPCell(new Phrase(String.format("%,.2f", item.getSubtotal()), FONT_BODY_BOLD));
                cSub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cSub.setPadding(5);
                itemsTable.addCell(cSub);
            }
        }

        document.add(itemsTable);

        // Totals Table
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(40);
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setWidths(new float[]{50, 50});

        totalsTable.addCell(createSummaryCell("Subtotal:", false));
        totalsTable.addCell(createSummaryCell(UITheme.formatCurrency(order.getSubtotal()), true));

        totalsTable.addCell(createSummaryCell("Discount:", false));
        totalsTable.addCell(createSummaryCell("- " + UITheme.formatCurrency(order.getDiscount()), true));

        PdfPCell grandLabel = new PdfPCell(new Phrase("Grand Total:", FONT_SECTION));
        grandLabel.setBorder(Rectangle.TOP);
        grandLabel.setPadding(6);
        totalsTable.addCell(grandLabel);

        PdfPCell grandVal = new PdfPCell(new Phrase(UITheme.formatCurrency(order.getTotalAmount()), FONT_SECTION));
        grandVal.setBorder(Rectangle.TOP);
        grandVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        grandVal.setPadding(6);
        totalsTable.addCell(grandVal);

        document.add(new Paragraph(" "));
        document.add(totalsTable);

        // Footer / Notes
        document.add(new Paragraph(" "));
        Paragraph notes = new Paragraph("Thank you for choosing EcoNest to make your spaces greener!\n" +
                "Goods received in good condition. All plants have a 7-day health guarantee.", FONT_HEADER_SUB);
        notes.setAlignment(Element.ALIGN_CENTER);
        document.add(notes);

        document.close();
    }

    /**
     * Generates a generic PDF report from a JTable TableModel.
     */
    public static void generateReportPDF(String reportTitle, String subtitle, TableModel model, File outputFile) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(outputFile));
        document.open();

        // Report Title Header
        Paragraph title = new Paragraph("ECONEST RETAIL SYSTEM - " + reportTitle.toUpperCase(), FONT_HEADER_TITLE);
        Paragraph sub = new Paragraph(subtitle + " | Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), FONT_HEADER_SUB);
        document.add(title);
        document.add(sub);
        document.add(new Paragraph(" "));

        int colCount = model.getColumnCount();
        PdfPTable table = new PdfPTable(colCount);
        table.setWidthPercentage(100);

        for (int c = 0; c < colCount; c++) {
            PdfPCell cell = new PdfPCell(new Phrase(model.getColumnName(c), FONT_TABLE_HEADER));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        int rowCount = model.getRowCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Object val = model.getValueAt(r, c);
                PdfPCell cell = new PdfPCell(new Phrase(val == null ? "" : val.toString(), FONT_BODY));
                cell.setPadding(5);
                if (r % 2 == 1) {
                    cell.setBackgroundColor(new BaseColor(0xF9, 0xFA, 0xF9));
                }
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();
    }

    private static PdfPCell createSummaryCell(String text, boolean isRight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FONT_BODY));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(4);
        if (isRight) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return cell;
    }
}
