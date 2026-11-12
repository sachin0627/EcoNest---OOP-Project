package com.econest.view;

import com.econest.controller.DeliveryAgentController;
import com.econest.controller.DeliveryController;
import com.econest.controller.OrderController;
import com.econest.model.Delivery;
import com.econest.model.DeliveryAgent;
import com.econest.model.Order;
import com.econest.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Modal dialog for Assigning a Delivery Agent to a Confirmed Order.
 * Automatically triggers Delivery Agent Email Notification upon assignment.
 */
public class AssignDeliveryDialog extends JDialog {
    private final DeliveryController deliveryController;
    private final OrderController orderController;
    private final DeliveryAgentController agentController;

    private boolean completed = false;

    private JTextField txtDeliveryId;
    private JComboBox<Order> cmbOrder;
    private JComboBox<DeliveryAgent> cmbAgent;
    private JTextField txtDeliveryDate;
    private JTextField txtTrackingNotes;

    public AssignDeliveryDialog(Frame parent, String preselectedOrderId) {
        super(parent, "Schedule Delivery & Assign Agent", true);
        this.deliveryController = new DeliveryController();
        this.orderController = new OrderController();
        this.agentController = new DeliveryAgentController();

        initUI(preselectedOrderId);
    }

    private void initUI(String preselectedOrderId) {
        setSize(560, 480);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("EcoNest Delivery Dispatch Scheduling");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.COLOR_BACKGROUND);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Delivery ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Delivery Ref ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtDeliveryId = new JTextField(deliveryController.generateNextId());
        txtDeliveryId.setEditable(false);
        form.add(txtDeliveryId, gbc);

        // Confirmed Order Selector
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Confirmed Order:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbOrder = new JComboBox<>();
        List<Order> orders = orderController.getAllOrders();
        Order toSelect = null;
        for (Order o : orders) {
            // Show Confirmed, Processing, or Pending orders
            if (!"Delivered".equalsIgnoreCase(o.getOrderStatus()) && !"Cancelled".equalsIgnoreCase(o.getOrderStatus())) {
                cmbOrder.addItem(o);
                if (preselectedOrderId != null && o.getOrderId().equalsIgnoreCase(preselectedOrderId)) {
                    toSelect = o;
                }
            }
        }
        if (toSelect != null) cmbOrder.setSelectedItem(toSelect);
        form.add(cmbOrder, gbc);

        // Available Delivery Agent Selector
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Available Agent:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbAgent = new JComboBox<>();
        List<DeliveryAgent> agents = agentController.getAvailableAgents();
        if (agents.isEmpty()) {
            // Fallback: list all active agents if none currently idle
            agents = agentController.getAllAgents();
        }
        for (DeliveryAgent a : agents) {
            cmbAgent.addItem(a);
        }
        form.add(cmbAgent, gbc);

        // Scheduled Delivery Date (Default tomorrow)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Scheduled Delivery Date:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        txtDeliveryDate = new JTextField(sdf.format(cal.getTime()));
        form.add(txtDeliveryDate, gbc);

        // Tracking Notes
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(new JLabel("Dispatch Instructions:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtTrackingNotes = new JTextField("Assigned for eco-delivery. Keep plants shaded.");
        form.add(txtTrackingNotes, gbc);

        // Email Alert Notice
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel lblNotice = new JLabel("✉ Note: Assigning will automatically dispatch an email notification to the agent.");
        lblNotice.setFont(UITheme.FONT_SMALL);
        lblNotice.setForeground(UITheme.COLOR_PRIMARY);
        form.add(lblNotice, gbc);

        add(form, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnAssign = UITheme.createPrimaryButton("Assign & Dispatch Notification");
        btnAssign.addActionListener(e -> handleAssign());

        actionPanel.add(btnCancel);
        actionPanel.add(btnAssign);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleAssign() {
        Order selectedOrder = (Order) cmbOrder.getSelectedItem();
        DeliveryAgent selectedAgent = (DeliveryAgent) cmbAgent.getSelectedItem();
        String dateStr = txtDeliveryDate.getText().trim();
        String notes = txtTrackingNotes.getText().trim();

        if (selectedOrder == null || selectedAgent == null) {
            JOptionPane.showMessageDialog(this, "Please select an order and an agent.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date deliveryDate;
        try {
            deliveryDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date in yyyy-MM-dd format.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Delivery delivery = new Delivery();
        delivery.setDeliveryId(txtDeliveryId.getText().trim());
        delivery.setOrderId(selectedOrder.getOrderId());
        delivery.setAgentId(selectedAgent.getAgentId());
        delivery.setDeliveryDate(deliveryDate);
        delivery.setDeliveryStatus("Assigned");
        delivery.setTrackingNotes(notes);

        boolean ok = deliveryController.assignDelivery(delivery);
        if (ok) {
            completed = true;
            JOptionPane.showMessageDialog(this,
                "Delivery #" + delivery.getDeliveryId() + " successfully scheduled!\n" +
                "Delivery agent " + selectedAgent.getFullName() + " has been assigned.\n" +
                "Email notification has been triggered.",
                "Assignment Complete",
                JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to schedule delivery assignment.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCompleted() {
        return completed;
    }
}
