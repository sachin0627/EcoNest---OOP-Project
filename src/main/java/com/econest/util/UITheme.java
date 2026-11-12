package com.econest.util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * EcoNest Theme & Styling System.
 * Nature-inspired sustainable palette with Forest Green, Sage Green, and Soft Orange.
 */
public class UITheme {

    // --- Color Palette ---
    public static final Color COLOR_PRIMARY = new Color(0x24, 0x5C, 0x43);      // Forest Green #245C43
    public static final Color COLOR_PRIMARY_DARK = new Color(0x1B, 0x47, 0x33); // Darker Forest Green
    public static final Color COLOR_SECONDARY = new Color(0x8F, 0xAF, 0x91);    // Sage Green #8FAF91
    public static final Color COLOR_SECONDARY_LIGHT = new Color(0xE3, 0xEC, 0xE4); // Pale Sage Tint
    public static final Color COLOR_ACCENT = new Color(0xE9, 0xA4, 0x5B);       // Soft Orange #E9A45B
    public static final Color COLOR_BACKGROUND = new Color(0xF7, 0xF8, 0xF2);   // Light Cream #F7F8F2
    public static final Color COLOR_CARD_BG = new Color(0xFF, 0xFF, 0xFF);     // Crisp White #FFFFFF
    public static final Color COLOR_TEXT_MAIN = new Color(0x1B, 0x2E, 0x24);   // Dark Slate/Charcoal #1B2E24
    public static final Color COLOR_TEXT_MUTED = new Color(0x6A, 0x7B, 0x72);  // Muted Green-Grey
    public static final Color COLOR_BORDER = new Color(0xDE, 0xE2, 0xD9);      // Gentle Divider Border
    
    // Status Colors
    public static final Color COLOR_SUCCESS = new Color(0x2E, 0x7D, 0x32);     // Forest Success
    public static final Color COLOR_WARNING = new Color(0xE6, 0x7E, 0x22);     // Warm Amber Warning
    public static final Color COLOR_DANGER = new Color(0xD3, 0x2F, 0x2F);      // Soft Crimson Danger
    public static final Color COLOR_INFO = new Color(0x19, 0x76, 0xD2);        // Deep Sky Info

    // --- Typography ---
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_CARD_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_NUMBER = new Font("Segoe UI", Font.BOLD, 26);

    // --- Formatters ---
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("LKR #,##0.00");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private UITheme() {}

    /**
     * Initializes FlatLaf Look and Feel with custom EcoNest defaults.
     */
    public static void setupLookAndFeel() {
        try {
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.default.focusColor", new Color(0x8F, 0xAF, 0x91, 100));
            
            // Table row height & styling
            UIManager.put("Table.rowHeight", 34);
            UIManager.put("Table.selectionBackground", new Color(0xDE, 0xEC, 0xE2));
            UIManager.put("Table.selectionForeground", COLOR_TEXT_MAIN);
            UIManager.put("Table.alternateRowColor", new Color(0xFA, 0xFB, 0xF9));

            FlatLightLaf.setup();
        } catch (Exception e) {
            System.err.println("[UITheme] FlatLaf setup warning: " + e.getMessage());
        }
    }

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatDate(Date date) {
        if (date == null) return "N/A";
        return DATE_FORMAT.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "N/A";
        return DATE_TIME_FORMAT.format(date);
    }

    /**
     * Creates a rounded card panel with shadow/border styling.
     */
    public static JPanel createCardPanel() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(COLOR_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        return card;
    }

    /**
     * Creates a Primary Action Button (Forest Green).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(COLOR_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /**
     * Creates a Secondary Action Button (Sage Green / Outline).
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setBackground(Color.WHITE);
        btn.setForeground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(
            new LineBorder(COLOR_SECONDARY, 1, true),
            new EmptyBorder(7, 16, 7, 16)
        ));
        return btn;
    }

    /**
     * Creates an Accent Action Button (Soft Orange).
     */
    public static JButton createAccentButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(COLOR_ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    /**
     * Creates a Danger Action Button (Soft Crimson).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setBackground(COLOR_DANGER);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        return btn;
    }

    /**
     * Formats a table with EcoNest header and cell styles.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEE, 0xF2, 0xED));
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(0xEE, 0xF4, 0xEF));
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setPreferredSize(new Dimension(table.getWidth(), 38));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
