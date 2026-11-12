package com.econest.view;

import com.econest.controller.OrderController;
import com.econest.controller.ProductController;
import com.econest.dao.*;
import com.econest.model.Order;
import com.econest.model.User;
import com.econest.service.ReportService;
import com.econest.util.UITheme;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Main Application Dashboard Frame for EcoNest Retail Management System.
 * 
 * Features:
 * - Responsive Nature-Inspired Sidebar Navigation
 * - Top Navigation Bar with live clock, quick search, notification badge, and profile
 * - Dynamic Dashboard Overview with KPI Stat Cards, JFreeChart analytics, Recent Orders, and Low Stock Alerts
 * - Modular CardLayout hosting all 9 Core System Modules
 */
public class DashboardFrame extends JFrame {
    private final User currentUser;

    // DAOs and Services for KPI data
    private final ProductDAO productDAO;
    private final ClientDAO clientDAO;
    private final OrderDAO orderDAO;
    private final InventoryDAO inventoryDAO;
    private final DeliveryDAO deliveryDAO;
    private final NotificationDAO notificationDAO;
    private final ReportService reportService;

    // UI Panels
    private JPanel contentCards;
    private CardLayout cardLayout;
    private JLabel lblClock;
    private JLabel lblNotificationBadge;

    // Metric KPI Labels
    private JLabel lblStatProducts;
    private JLabel lblStatClients;
    private JLabel lblStatOrders;
    private JLabel lblStatRevenue;
    private JLabel lblStatLowStock;
    private JLabel lblStatDeliveries;

    // Dashboard Home Sub-components
    private JPanel chartPanelContainer;
    private JTable recentOrdersTable;
    private DefaultTableModel recentOrdersModel;
    private JPanel lowStockContainer;

    // Module Panels
    private ProductPanel productPanel;
    private ClientPanel clientPanel;
    private InventoryPanel inventoryPanel;
    private DeliveryAgentPanel deliveryAgentPanel;
    private OrderPanel orderPanel;
    private DeliveryPanel deliveryPanel;
    private ReportsPanel reportsPanel;
    private NotificationPanel notificationPanel;
    private SettingsPanel settingsPanel;

    // Sidebar active tracker
    private final java.util.List<JButton> navButtons = new java.util.ArrayList<>();

    public DashboardFrame(User currentUser) {
        this.currentUser = currentUser;
        this.productDAO = new ProductDAO();
        this.clientDAO = new ClientDAO();
        this.orderDAO = new OrderDAO();
        this.inventoryDAO = new InventoryDAO();
        this.deliveryDAO = new DeliveryDAO();
        this.notificationDAO = new NotificationDAO();
        this.reportService = new ReportService();

        initUI();
        startClock();
        refreshDashboardData();
    }

    private void initUI() {
        setTitle("EcoNest – Smart Home & Garden Retail Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // 1. Sidebar Navigation (Left)
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Main Container (Top Navigation Bar + CardLayout Content)
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UITheme.COLOR_BACKGROUND);

        JPanel topBar = createTopBar();
        mainArea.add(topBar, BorderLayout.NORTH);

        // CardLayout container for all main views
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(UITheme.COLOR_BACKGROUND);

        // Build individual module panels
        JPanel homePanel = createHomeOverviewPanel();
        productPanel = new ProductPanel();
        clientPanel = new ClientPanel();
        inventoryPanel = new InventoryPanel();
        deliveryAgentPanel = new DeliveryAgentPanel();
        orderPanel = new OrderPanel();
        deliveryPanel = new DeliveryPanel();
        reportsPanel = new ReportsPanel();
        notificationPanel = new NotificationPanel();
        settingsPanel = new SettingsPanel(currentUser);

        contentCards.add(homePanel, "HOME");
        contentCards.add(productPanel, "PRODUCTS");
        contentCards.add(clientPanel, "CLIENTS");
        contentCards.add(inventoryPanel, "INVENTORY");
        contentCards.add(deliveryAgentPanel, "AGENTS");
        contentCards.add(orderPanel, "ORDERS");
        contentCards.add(deliveryPanel, "DELIVERIES");
        contentCards.add(reportsPanel, "REPORTS");
        contentCards.add(notificationPanel, "NOTIFICATIONS");
        contentCards.add(settingsPanel, "SETTINGS");

        mainArea.add(contentCards, BorderLayout.CENTER);
        add(mainArea, BorderLayout.CENTER);
    }

    /**
     * Builds the Nature-Inspired Sidebar Navigation.
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setBackground(UITheme.COLOR_PRIMARY);

        // Logo & Brand Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBackground(UITheme.COLOR_PRIMARY_DARK);
        brandPanel.setBorder(new EmptyBorder(22, 18, 20, 18));

        JLabel lblLogo = new JLabel("🌿 EcoNest");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblLogo.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Smart Retail System");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.COLOR_SECONDARY);

        brandPanel.add(lblLogo);
        brandPanel.add(Box.createVerticalStrut(2));
        brandPanel.add(lblSub);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Nav Links Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(UITheme.COLOR_PRIMARY);
        menuPanel.setBorder(new EmptyBorder(12, 10, 12, 10));

        addNavItem(menuPanel, "📊  Dashboard", "HOME", true);
        addNavItem(menuPanel, "🌱  Product Catalogue", "PRODUCTS", false);
        addNavItem(menuPanel, "👥  Client Management", "CLIENTS", false);
        addNavItem(menuPanel, "📦  Inventory & Stock", "INVENTORY", false);
        addNavItem(menuPanel, "🚚  Delivery Agents", "AGENTS", false);
        addNavItem(menuPanel, "📝  Order Management", "ORDERS", false);
        addNavItem(menuPanel, "📍  Delivery Dispatch", "DELIVERIES", false);
        addNavItem(menuPanel, "📈  Reports & Analytics", "REPORTS", false);
        addNavItem(menuPanel, "✉  Email Notifications", "NOTIFICATIONS", false);
        addNavItem(menuPanel, "⚙  System Settings", "SETTINGS", false);

        JScrollPane scrollMenu = new JScrollPane(menuPanel);
        scrollMenu.setBorder(BorderFactory.createEmptyBorder());
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        sidebar.add(scrollMenu, BorderLayout.CENTER);

        // Bottom User Status & Logout
        JPanel userFoot = new JPanel(new BorderLayout());
        userFoot.setBackground(UITheme.COLOR_PRIMARY_DARK);
        userFoot.setBorder(new EmptyBorder(14, 16, 14, 16));

        JPanel userTxt = new JPanel();
        userTxt.setLayout(new BoxLayout(userTxt, BoxLayout.Y_AXIS));
        userTxt.setOpaque(false);

        JLabel lblUName = new JLabel(currentUser != null ? currentUser.getFullName() : "Administrator");
        lblUName.setFont(UITheme.FONT_BODY_BOLD);
        lblUName.setForeground(Color.WHITE);

        JLabel lblRole = new JLabel(currentUser != null ? currentUser.getRole() : "ADMIN");
        lblRole.setFont(UITheme.FONT_SMALL);
        lblRole.setForeground(UITheme.COLOR_ACCENT);

        userTxt.add(lblUName);
        userTxt.add(lblRole);
        userFoot.add(userTxt, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(UITheme.FONT_SMALL);
        btnLogout.setBackground(new Color(0xD3, 0x2F, 0x2F));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> handleLogout());
        userFoot.add(btnLogout, BorderLayout.EAST);

        sidebar.add(userFoot, BorderLayout.SOUTH);
        return sidebar;
    }

    private void addNavItem(JPanel container, String text, String cardName, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BODY_BOLD);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 40));
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (active) {
            btn.setBackground(new Color(0x2E, 0x75, 0x55));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(UITheme.COLOR_PRIMARY);
            btn.setForeground(new Color(0xE1, 0xEB, 0xE3));
        }

        btn.addActionListener(e -> {
            cardLayout.show(contentCards, cardName);
            for (JButton b : navButtons) {
                b.setBackground(UITheme.COLOR_PRIMARY);
                b.setForeground(new Color(0xE1, 0xEB, 0xE3));
            }
            btn.setBackground(new Color(0x2E, 0x75, 0x55));
            btn.setForeground(Color.WHITE);

            // Trigger refreshes on active view
            switch (cardName) {
                case "HOME": refreshDashboardData(); break;
                case "PRODUCTS": productPanel.loadProducts(); break;
                case "CLIENTS": clientPanel.loadClients(); break;
                case "INVENTORY": inventoryPanel.loadInventory(false); break;
                case "AGENTS": deliveryAgentPanel.loadAgents(); break;
                case "ORDERS": orderPanel.loadOrders(); break;
                case "DELIVERIES": deliveryPanel.loadDeliveries(); break;
                case "REPORTS": reportsPanel.loadReport(); break;
                case "NOTIFICATIONS": notificationPanel.loadNotifications(); break;
            }
        });

        navButtons.add(btn);
        container.add(btn);
        container.add(Box.createVerticalStrut(4));
    }

    /**
     * Builds the Top Navigation Bar.
     */
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.COLOR_BORDER),
            new EmptyBorder(12, 24, 12, 24)
        ));

        // Left Quick Global Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearchIcon = new JLabel("🔍");
        JTextField txtGlobalSearch = new JTextField("Quick find orders, products, clients...", 24);
        txtGlobalSearch.setFont(UITheme.FONT_BODY);
        txtGlobalSearch.setForeground(UITheme.COLOR_TEXT_MUTED);

        searchPanel.add(lblSearchIcon);
        searchPanel.add(txtGlobalSearch);
        topBar.add(searchPanel, BorderLayout.WEST);

        // Right Date, Notification Bell, Refresh
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        lblClock = new JLabel();
        lblClock.setFont(UITheme.FONT_BODY);
        lblClock.setForeground(UITheme.COLOR_TEXT_MAIN);

        lblNotificationBadge = new JLabel("🔔 Notifications (3)");
        lblNotificationBadge.setFont(UITheme.FONT_BODY_BOLD);
        lblNotificationBadge.setForeground(UITheme.COLOR_PRIMARY);
        lblNotificationBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblNotificationBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                cardLayout.show(contentCards, "NOTIFICATIONS");
            }
        });

        JButton btnSync = UITheme.createSecondaryButton("🔄 Refresh All");
        btnSync.addActionListener(e -> refreshDashboardData());

        rightPanel.add(lblClock);
        rightPanel.add(lblNotificationBadge);
        rightPanel.add(btnSync);
        topBar.add(rightPanel, BorderLayout.EAST);

        return topBar;
    }

    /**
     * Builds the Main Home Overview Panel (KPI Cards, Charts, Tables).
     */
    private JPanel createHomeOverviewPanel() {
        JPanel home = new JPanel(new BorderLayout(0, 16));
        home.setBackground(UITheme.COLOR_BACKGROUND);
        home.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. KPI Statistic Cards Grid (6 Cards)
        JPanel statsGrid = new JPanel(new GridLayout(1, 6, 12, 0));
        statsGrid.setOpaque(false);

        lblStatProducts = new JLabel("0");
        lblStatClients = new JLabel("0");
        lblStatOrders = new JLabel("0");
        lblStatRevenue = new JLabel("LKR 0");
        lblStatLowStock = new JLabel("0");
        lblStatDeliveries = new JLabel("0");

        statsGrid.add(createKpiCard("Total Products", lblStatProducts, "🌿 Active Catalogue", UITheme.COLOR_PRIMARY));
        statsGrid.add(createKpiCard("Total Clients", lblStatClients, "👥 Registered Accounts", UITheme.COLOR_SECONDARY));
        statsGrid.add(createKpiCard("Total Orders", lblStatOrders, "📦 Lifetime Orders", UITheme.COLOR_PRIMARY));
        statsGrid.add(createKpiCard("Total Revenue", lblStatRevenue, "💰 Net Dispatched/Paid", new Color(0x1B, 0x7E, 0x4B)));
        statsGrid.add(createKpiCard("Low Stock Alert", lblStatLowStock, "⚠ Reorder Deficit", UITheme.COLOR_DANGER));
        statsGrid.add(createKpiCard("Active Deliveries", lblStatDeliveries, "🚚 In Transit / Scheduled", UITheme.COLOR_ACCENT));

        home.add(statsGrid, BorderLayout.NORTH);

        // 2. Middle Section: Analytics Chart (Left) + Low Stock Summary (Right)
        JPanel midSection = new JPanel(new BorderLayout(14, 0));
        midSection.setOpaque(false);

        // JFreeChart Container
        chartPanelContainer = UITheme.createCardPanel();
        chartPanelContainer.setLayout(new BorderLayout());
        chartPanelContainer.setPreferredSize(new Dimension(740, 260));
        midSection.add(chartPanelContainer, BorderLayout.CENTER);

        // Low Stock Quick Summary Panel
        lowStockContainer = UITheme.createCardPanel();
        lowStockContainer.setLayout(new BoxLayout(lowStockContainer, BoxLayout.Y_AXIS));
        lowStockContainer.setPreferredSize(new Dimension(340, 260));
        midSection.add(lowStockContainer, BorderLayout.EAST);

        // 3. Bottom Section: Recent Orders Table
        JPanel recentOrdersCard = UITheme.createCardPanel();
        recentOrdersCard.setLayout(new BorderLayout(0, 10));

        JPanel recentHead = new JPanel(new BorderLayout());
        recentHead.setOpaque(false);
        JLabel lblRecentTitle = new JLabel("Recent Client Orders");
        lblRecentTitle.setFont(UITheme.FONT_CARD_HEADER);
        lblRecentTitle.setForeground(UITheme.COLOR_PRIMARY);

        JButton btnViewAllOrders = UITheme.createSecondaryButton("View All Orders →");
        btnViewAllOrders.addActionListener(e -> cardLayout.show(contentCards, "ORDERS"));

        recentHead.add(lblRecentTitle, BorderLayout.WEST);
        recentHead.add(btnViewAllOrders, BorderLayout.EAST);
        recentOrdersCard.add(recentHead, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Client Name", "Order Date", "Amount (LKR)", "Order Status", "Delivery Address"};
        recentOrdersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        recentOrdersTable = new JTable(recentOrdersModel);
        UITheme.styleTable(recentOrdersTable);

        JScrollPane scrollRecent = new JScrollPane(recentOrdersTable);
        scrollRecent.setBorder(BorderFactory.createEmptyBorder());
        recentOrdersCard.add(scrollRecent, BorderLayout.CENTER);

        // Combine Mid and Bottom into Vertical Split / Box
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);
        centerContainer.add(midSection, BorderLayout.NORTH);
        centerContainer.add(recentOrdersCard, BorderLayout.CENTER);

        home.add(centerContainer, BorderLayout.CENTER);
        return home;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(UITheme.FONT_SMALL);
        lblTitle.setForeground(UITheme.COLOR_TEXT_MUTED);

        valueLabel.setFont(UITheme.FONT_STAT_NUMBER);
        valueLabel.setForeground(accentColor);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.COLOR_TEXT_MUTED);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(lblSub);

        return card;
    }

    public void refreshDashboardData() {
        SwingUtilities.invokeLater(() -> {
            try {
                int totalProducts = productDAO.getAllProducts().size();
                int totalClients = clientDAO.getAllClients().size();
                int totalOrders = orderDAO.getTotalOrderCount();
                double totalRevenue = orderDAO.getTotalRevenue();
                int lowStock = inventoryDAO.getLowStockCount();
                int activeDeliveries = deliveryDAO.getActiveDeliveryCount();

                lblStatProducts.setText(String.valueOf(totalProducts));
                lblStatClients.setText(String.valueOf(totalClients));
                lblStatOrders.setText(String.valueOf(totalOrders));
                lblStatRevenue.setText("LKR " + String.format("%,.0f", totalRevenue));
                lblStatLowStock.setText(String.valueOf(lowStock));
                lblStatDeliveries.setText(String.valueOf(activeDeliveries));

                // Refresh JFreeChart
                chartPanelContainer.removeAll();
                ChartPanel chartPanel = new ChartPanel(reportService.createMonthlySalesChart());
                chartPanel.setOpaque(false);
                chartPanel.setBackground(Color.WHITE);
                chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
                chartPanelContainer.revalidate();
                chartPanelContainer.repaint();

                // Refresh Low Stock Summary Card
                lowStockContainer.removeAll();
                JLabel lblLsTitle = new JLabel("Low-Stock Alerts (" + lowStock + ")");
                lblLsTitle.setFont(UITheme.FONT_CARD_HEADER);
                lblLsTitle.setForeground(UITheme.COLOR_DANGER);
                lowStockContainer.add(lblLsTitle);
                lowStockContainer.add(Box.createVerticalStrut(8));

                List<com.econest.model.Inventory> lowList = inventoryDAO.getLowStockItems();
                if (lowList.isEmpty()) {
                    JLabel lblOk = new JLabel("✔ All stocks are above reorder limits.");
                    lblOk.setFont(UITheme.FONT_BODY);
                    lblOk.setForeground(UITheme.COLOR_SUCCESS);
                    lowStockContainer.add(lblOk);
                } else {
                    int count = 0;
                    for (com.econest.model.Inventory inv : lowList) {
                        if (count++ >= 5) break; // Show top 5
                        JLabel itemLbl = new JLabel(String.format("• %s: %d left (Min %d)", inv.getProductName(), inv.getAvailableQuantity(), inv.getReorderLevel()));
                        itemLbl.setFont(UITheme.FONT_BODY);
                        lowStockContainer.add(itemLbl);
                        lowStockContainer.add(Box.createVerticalStrut(4));
                    }
                }
                lowStockContainer.revalidate();
                lowStockContainer.repaint();

                // Refresh Recent Orders Table
                recentOrdersModel.setRowCount(0);
                List<Order> recent = orderDAO.getRecentOrders(6);
                for (Order o : recent) {
                    recentOrdersModel.addRow(new Object[]{
                        o.getOrderId(),
                        o.getClientName(),
                        UITheme.formatDateTime(o.getOrderDate()),
                        UITheme.formatCurrency(o.getTotalAmount()),
                        o.getOrderStatus(),
                        o.getDeliveryAddress()
                    });
                }
            } catch (Exception ex) {
                System.err.println("Dashboard refresh error: " + ex.getMessage());
            }
        });
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy  HH:mm:ss");
            lblClock.setText(sdf.format(new Date()));
        });
        timer.start();
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out of EcoNest?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            com.econest.controller.LoginController.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
}
