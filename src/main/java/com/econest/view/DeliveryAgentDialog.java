package com.econest.view;

import com.econest.controller.DeliveryAgentController;
import com.econest.model.DeliveryAgent;
import com.econest.util.UITheme;
import com.econest.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog for Adding / Editing a Delivery Agent.
 */
public class DeliveryAgentDialog extends JDialog {
    private final DeliveryAgentController controller;
    private final DeliveryAgent agent;
    private final boolean isNew;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JComboBox<String> cmbVehicleType;
    private JTextField txtVehicleReg;
    private JComboBox<String> cmbStatus;

    public DeliveryAgentDialog(Frame parent, DeliveryAgent agent, boolean isNew) {
        super(parent, isNew ? "Add Delivery Agent" : "Edit Agent: " + agent.getAgentId(), true);
        this.controller = new DeliveryAgentController();
        this.agent = agent != null ? agent : new DeliveryAgent();
        this.isNew = isNew;

        initUI();
    }

    private void initUI() {
        setSize(480, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(isNew ? "Register New Delivery Personnel" : "Update Delivery Agent");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.COLOR_BACKGROUND);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Agent ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = new JTextField(isNew ? controller.generateNextId() : agent.getAgentId());
        txtId.setEnabled(isNew);
        form.add(txtId, gbc);

        // Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtName = new JTextField(agent.getFullName() != null ? agent.getFullName() : "");
        form.add(txtName, gbc);

        // Phone Number
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPhone = new JTextField(agent.getPhone() != null ? agent.getPhone() : "+94 ");
        form.add(txtPhone, gbc);

        // Email Address
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtEmail = new JTextField(agent.getEmail() != null ? agent.getEmail() : "");
        form.add(txtEmail, gbc);

        // Vehicle Type
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(new JLabel("Vehicle Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] vehicles = {"Eco-Electric Van", "Delivery Motorbike (Coir Carrier)", "Light Covered Truck", "Electric Cargo Scooter"};
        cmbVehicleType = new JComboBox<>(vehicles);
        if (agent.getVehicleType() != null) cmbVehicleType.setSelectedItem(agent.getVehicleType());
        form.add(cmbVehicleType, gbc);

        // Vehicle Reg No
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(new JLabel("Vehicle Reg No:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtVehicleReg = new JTextField(agent.getVehicleRegNo() != null ? agent.getVehicleRegNo() : "WP ");
        form.add(txtVehicleReg, gbc);

        // Availability Status
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(new JLabel("Availability:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] statuses = {"Available", "Assigned", "Unavailable"};
        cmbStatus = new JComboBox<>(statuses);
        if (agent.getAvailabilityStatus() != null) cmbStatus.setSelectedItem(agent.getAvailabilityStatus());
        form.add(cmbStatus, gbc);

        add(form, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UITheme.createPrimaryButton(isNew ? "Register Agent" : "Save Changes");
        btnSave.addActionListener(e -> handleSave());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String vehicle = (String) cmbVehicleType.getSelectedItem();
        String reg = txtVehicleReg.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();

        if (name.isEmpty() || reg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all required agent details.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid telephone number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        agent.setAgentId(id);
        agent.setFullName(name);
        agent.setPhone(phone);
        agent.setEmail(email);
        agent.setVehicleType(vehicle);
        agent.setVehicleRegNo(reg);
        agent.setAvailabilityStatus(status);
        agent.setActive(true);

        boolean ok = controller.saveAgent(agent, isNew);
        if (ok) {
            saved = true;
            JOptionPane.showMessageDialog(this, "Delivery agent registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save delivery agent.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
