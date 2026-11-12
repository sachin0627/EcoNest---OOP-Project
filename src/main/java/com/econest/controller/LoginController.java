package com.econest.controller;

import com.econest.dao.UserDAO;
import com.econest.model.User;

/**
 * Controller managing authentication, credential verification, and user sessions.
 */
public class LoginController {
    private final UserDAO userDAO;
    private static User currentUser;

    public LoginController() {
        this.userDAO = new UserDAO();
    }

    public User login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return null;
        }

        User user = userDAO.authenticate(usernameOrEmail.trim(), password);
        if (user != null) {
            currentUser = user;
        }
        return user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isAdmin() {
        return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }
}
