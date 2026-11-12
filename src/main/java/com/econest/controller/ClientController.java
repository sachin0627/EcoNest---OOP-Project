package com.econest.controller;

import com.econest.dao.ClientDAO;
import com.econest.model.Client;

import java.util.List;

/**
 * Controller for Module 02: Client Management.
 */
public class ClientController {
    private final ClientDAO clientDAO;

    public ClientController() {
        this.clientDAO = new ClientDAO();
    }

    public List<Client> getAllClients() {
        return clientDAO.getAllClients();
    }

    public Client getClient(String id) {
        return clientDAO.getClientById(id);
    }

    public List<Client> searchClients(String keyword, String type) {
        return clientDAO.searchClients(keyword, type);
    }

    public boolean saveClient(Client client, boolean isNew) {
        if (client == null || !client.isValid()) {
            return false;
        }
        if (isNew) {
            return clientDAO.addClient(client);
        } else {
            return clientDAO.updateClient(client);
        }
    }

    public boolean deleteClient(String clientId) {
        return clientDAO.deleteClient(clientId);
    }

    public String generateNextId() {
        return clientDAO.getNextClientId();
    }
}
