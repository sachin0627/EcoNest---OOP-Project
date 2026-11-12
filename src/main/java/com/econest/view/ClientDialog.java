package com.econest.view;

import com.econest.controller.ClientController;
import com.econest.model.Client;
import com.econest.model.HouseholdClient;
import com.econest.model.OfficeClient;
import com.econest.util.UITheme;
import com.econest.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Date;

/**
 * Modal dialog for Registering / Editing a Client in EcoNest.
 */
public class ClientDialog extends JDialog {
    private final ClientController controller;
    private Client client;
    private final boolean isNew;
    private boolean saved = false;

    private JTextField txtId;
    private JTextField txtName;
    private JComboBox<String> cmbType;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JTextField txtCity;

    public ClientDialog(Frame parent, Client client, boolean isNew) {
        super(parent, isNew ? "Register New Client" : "Edit Client Details", true);
        this.controller = new ClientController();
        this.client = client;
        this.isNew = isNew;

        initUI();
    }

    private void initUI() {
        setSize(500, 520);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getContentPane().setBackground(UITheme.COLOR_BACKGROUND);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel(isNew ? "Register New Client (Household / Office)" : "Update Client Information");
        lblTitle.setFont(UITheme.FONT_SUBTITLE);
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.COLOR_BACKGROUND);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Client ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtId = new JTextField(isNew ? controller.generateNextId() : client.getClientId());
        txtId.setEnabled(isNew);
        form.add(txtId, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        form.add(new JLabel("Client Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtName = new JTextField(client != null ? client.getClientName() : "");
        form.add(txtName, gbc);

        // Client Type
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        form.add(new JLabel("Client Type:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] types = {"Household", "Office"};
        cmbType = new JComboBox<>(types);
        if (client != null && client.getClientType() != null) cmbType.setSelectedItem(client.getClientType());
        form.add(cmbType, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        form.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtEmail = new JTextField(client != null ? client.getEmail() : "");
        form.add(txtEmail, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        form.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtPhone = new JTextField(client != null ? client.getPhone() : "+94 ");
        form.add(txtPhone, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        form.add(new JLabel("Street Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtAddress = new JTextField(client != null ? client.getAddress() : "");
        form.add(txtAddress, gbc);

        // City
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        form.add(new JLabel("City:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtCity = new JTextField(client != null ? client.getCity() : "Colombo");
        form.add(txtCity, gbc);

        add(form, BorderLayout.CENTER);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        actionPanel.setBackground(UITheme.COLOR_BACKGROUND);

        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = UITheme.createPrimaryButton(isNew ? "Register Client" : "Save Changes");
        btnSave.addActionListener(e -> handleSave());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);
        add(actionPanel, BorderLayout.SOUTH);
    }

    private void handleSave() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        String type = (String) cmbType.getSelectedItem();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String city = txtCity.getText().trim();

        if (name.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter all required client details.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address (e.g. client@example.com).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid phone number (e.g. +94 77 123 4567).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (client == null) {
            if ("Office".equalsIgnoreCase(type)) {
                client = new OfficeClient();
            } else {
                client = new HouseholdClient();
            }
        }

        client.setClientId(id);
        client.setClientName(name);
        client.setClientType(type);
        client.setEmail(email);
        client.setPhone(phone);
        client.setAddress(address);
        client.setCity(city);
        if (client.getRegistrationDate() == null) client.setRegistrationDate(new Date());

        boolean ok = controller.saveClient(client, isNew);
        if (ok) {
            saved = true;
            JOptionPane.showMessageDialog(this, "Client registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save client to database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
