package com.econest.view;

import com.econest.dao.ClientDAO;
import com.econest.model.Client;
import com.econest.model.Order;
import com.econest.model.OrderItem;
import com.econest.util.PDFUtil;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Modal dialog presenting a printable Commercial Invoice with PDF export.
 */
public class InvoicePreviewDialog extends JDialog {
    private final Order order;
    private final Client client;

    public InvoicePreviewDialog(Frame parent, Order order) {
        super(parent, "Commercial Invoice - Order #" + order.getOrderId(), true);
        this.order = order;
        this.client = new ClientDAO().getClientById(order.getClientId());

        initUI();
    }

    private void initUI() {
        setSize(650, 720);
        setLocationRelativeTo(getParent());
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("EcoNest Invoice Document - " + order.getOrderId());
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Invoice HTML Document Viewer
        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(buildInvoiceHtml());
        editorPane.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnClose = UITheme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dispose());

        JButton btnExportPDF = UITheme.createPrimaryButton("Export PDF Invoice");
        btnExportPDF.addActionListener(e -> exportPDF());

        JButton btnPrint = UITheme.createAccentButton("Print Invoice");
        btnPrint.addActionListener(e -> {
            try {
                editorPane.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionPanel.add(btnClose);
        actionPanel.add(btnPrint);
        actionPanel.add(btnExportPDF);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private String buildInvoiceHtml() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Segoe UI, sans-serif; margin:20px; color:#1B2E24; background-color:#FFFFFF;'>");
        
        // Brand Header
        sb.append("<table width='100%' style='border-bottom:2px solid #245C43; padding-bottom:10px;'><tr>");
        sb.append("<td><h2 style='color:#245C43; margin:0;'>EcoNest Retail Solutions</h2>");
        sb.append("<p style='color:#6A7B72; font-size:12px; margin:2px 0;'>Sustainable Smart Home &amp; Garden Retail<br>");
        sb.append("88, Green Park Way, Colombo 03, Sri Lanka | Tel: +94 11 254 7890</p></td>");
        sb.append("<td align='right'><h3 style='margin:0; color:#245C43;'>COMMERCIAL INVOICE</h3>");
        sb.append("<p style='font-size:12px; margin:2px 0;'><strong>Invoice:</strong> INV-").append(order.getOrderId()).append("<br>");
        sb.append("<strong>Date:</strong> ").append(UITheme.formatDateTime(order.getOrderDate())).append("<br>");
        sb.append("<strong>Status:</strong> ").append(order.getOrderStatus()).append("</p></td>");
        sb.append("</tr></table><br>");

        // Customer & Delivery Info
        String cName = client != null ? client.getClientName() : order.getClientId();
        String cPhone = client != null ? client.getPhone() : "N/A";
        String cEmail = client != null ? client.getEmail() : "N/A";

        sb.append("<table width='100%' style='margin-bottom:15px; font-size:12px;'><tr>");
        sb.append("<td width='50%' style='background:#F7F8F2; padding:10px; border-radius:6px;'>");
        sb.append("<strong>BILLED TO:</strong><br>");
        sb.append(cName).append("<br>");
        sb.append("Tel: ").append(cPhone).append("<br>");
        sb.append("Email: ").append(cEmail).append("</td>");
        sb.append("<td width='50%' style='background:#F7F8F2; padding:10px; border-radius:6px;'>");
        sb.append("<strong>DELIVERY &amp; PAYMENT:</strong><br>");
        sb.append("Address: ").append(order.getDeliveryAddress()).append("<br>");
        sb.append("Payment: ").append(order.getPaymentMethod()).append("<br>");
        sb.append("Remarks: ").append(order.getNotes() != null ? order.getNotes() : "None").append("</td>");
        sb.append("</tr></table>");

        // Items Table
        sb.append("<table width='100%' border='1' cellspacing='0' cellpadding='6' style='border-collapse:collapse; border-color:#DEE2D9; font-size:12px;'>");
        sb.append("<tr style='background-color:#245C43; color:#FFFFFF;'>");
        sb.append("<th>#</th><th>Product Description</th><th>Unit Price</th><th>Qty</th><th>Subtotal</th></tr>");

        int i = 1;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                String pName = item.getProductName() != null ? item.getProductName() : item.getProductId();
                sb.append("<tr>");
                sb.append("<td align='center'>").append(i++).append("</td>");
                sb.append("<td>").append(pName).append("</td>");
                sb.append("<td align='right'>").append(UITheme.formatCurrency(item.getUnitPrice())).append("</td>");
                sb.append("<td align='center'>").append(item.getQuantity()).append("</td>");
                sb.append("<td align='right'>").append(UITheme.formatCurrency(item.getSubtotal())).append("</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</table><br>");

        // Totals
        sb.append("<table width='100%' style='font-size:13px;'><tr><td width='60%'></td><td width='40%'>");
        sb.append("<table width='100%' style='border-collapse:collapse;'>");
        sb.append("<tr><td>Subtotal:</td><td align='right'><strong>").append(UITheme.formatCurrency(order.getSubtotal())).append("</strong></td></tr>");
        sb.append("<tr><td>Discount:</td><td align='right' style='color:#D32F2F;'>- ").append(UITheme.formatCurrency(order.getDiscount())).append("</td></tr>");
        sb.append("<tr style='border-top:2px solid #245C43; font-size:16px;'><td style='color:#245C43; padding-top:4px;'><strong>Grand Total:</strong></td>");
        sb.append("<td align='right' style='color:#245C43; padding-top:4px;'><strong>").append(UITheme.formatCurrency(order.getTotalAmount())).append("</strong></td></tr>");
        sb.append("</table></td></tr></table>");

        // Terms Footer
        sb.append("<br><hr style='border:0; border-top:1px dashed #8FAF91;'><p align='center' style='font-size:11px; color:#6A7B72;'>");
        sb.append("Thank you for shopping green with EcoNest! Live plant care instructions are enclosed with your order.<br>");
        sb.append("For corporate invoicing support or queries, contact billing@econest.lk.</p>");

        sb.append("</body></html>");
        return sb.toString();
    }

    private void exportPDF() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("EcoNest_Invoice_" + order.getOrderId() + ".pdf"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String cName = client != null ? client.getClientName() : order.getClientId();
                String cPhone = client != null ? client.getPhone() : null;
                PDFUtil.generateInvoicePDF(order, cName, cPhone, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Invoice PDF generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
