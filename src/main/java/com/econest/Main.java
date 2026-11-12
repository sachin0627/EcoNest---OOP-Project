package com.econest;

import com.econest.util.DatabaseConnection;
import com.econest.util.EnvConfig;
import com.econest.util.UITheme;
import com.econest.view.LoginFrame;

import javax.swing.*;

/**
 * Main Application Entry Point for EcoNest – Smart Home & Garden Retail Management System.
 * 
 * University Object-Oriented Programming (OOP) Group Project (HDIT 2026).
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize Environment Variables and Config (.env)
        EnvConfig.init();

        // 2. Setup FlatLaf Modern Nature-Inspired Look and Feel
        UITheme.setupLookAndFeel();

        // 3. Test Database Connection
        boolean connected = DatabaseConnection.getInstance().testConnection();
        if (!connected) {
            System.err.println("[EcoNest Warning] Could not connect to MySQL Server 'econest_db' at localhost:3306.");
            System.err.println("Please ensure MySQL is running and execute econest_db.sql schema if not already initialized.");
        } else {
            System.out.println("[EcoNest] Successfully connected to MySQL database 'econest_db'.");
        }

        // 4. Launch Desktop Application on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, 
                    "Application startup error: " + e.getMessage(), 
                    "Startup Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
