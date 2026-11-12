package com.econest.controller;

import com.econest.dao.DeliveryAgentDAO;
import com.econest.model.DeliveryAgent;

import java.util.List;

/**
 * Controller for Module 04: Delivery Agent Management.
 */
public class DeliveryAgentController {
    private final DeliveryAgentDAO agentDAO;

    public DeliveryAgentController() {
        this.agentDAO = new DeliveryAgentDAO();
    }

    public List<DeliveryAgent> getAllAgents() {
        return agentDAO.getAllAgents();
    }

    public List<DeliveryAgent> getAvailableAgents() {
        return agentDAO.getAvailableAgents();
    }

    public DeliveryAgent getAgent(String id) {
        return agentDAO.getAgentById(id);
    }

    public List<DeliveryAgent> searchAgents(String keyword, String status) {
        return agentDAO.searchAgents(keyword, status);
    }

    public boolean saveAgent(DeliveryAgent agent, boolean isNew) {
        if (agent == null || !agent.isValid()) {
            return false;
        }
        if (isNew) {
            return agentDAO.addAgent(agent);
        } else {
            return agentDAO.updateAgent(agent);
        }
    }

    public boolean deleteAgent(String agentId) {
        return agentDAO.deleteAgent(agentId);
    }

    public String generateNextId() {
        return agentDAO.getNextAgentId();
    }
}
