package com.leanx.app.api.user.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leanx.app.service.modules.system.configs.SecurityConfig;
import com.leanx.app.service.modules.user.admin.UserService;
import com.leanx.app.service.modules.user.auth.AuthenticationService;
import com.leanx.app.service.modules.user.auth.exceptions.AccountDeactivatedException;
import com.leanx.app.service.modules.user.auth.exceptions.AccountLockedException;
import com.leanx.app.service.modules.user.auth.exceptions.FirstLoginException;
import com.leanx.app.service.modules.user.auth.exceptions.PasswordExpiredException;
import com.leanx.app.utils.ApiUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet controller for handling authentication-related requests, including
 * login, logout, session verification, and password change operations.
 */
@WebServlet(name = "AuthController", value = "/api/auth/*")
@MultipartConfig
public class AuthenticationController extends HttpServlet {

    // private static final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Handles GET requests to the authentication endpoints.
     * Currently supports checking the status of a user session.
     *
     * @param request  The {@code HttpServletRequest} object containing the client's request.
     * @param response The {@code HttpServletResponse} object for sending the response to the client.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs while handling the request.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // logic for verifying session status
        String path = request.getRequestURI();

        if (path.endsWith("/session")) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Unauthorized Session!");
                return;
            }

            ApiUtils.sendJsonResponse(response, "Authorized Session!");
        } else {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint!");
        }
    }

    /**
     * Handles POST requests to the authentication endpoints, including login,
     * logout, and password change.
     *
     * @param request  The {@code HttpServletRequest} object containing the client's request.
     * @param response The {@code HttpServletResponse} object for sending the response to the client.
     * @throws ServletException If a servlet-specific error occurs.
     * @throws IOException      If an I/O error occurs while handling the request.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (path.endsWith("/login")) {
            handleLogin(request, response);
        } else if (path.endsWith("/logout")) {
            handleLogout(request, response);
        } else if (path.endsWith("/change-password")) {
            handleChangePassword(request, response);
        } else {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint!");
        }
    }

    /**
     * Handles the user login process. It authenticates the user based on the
     * provided username and password. Upon successful authentication, it creates
     * a new session and stores the user ID. It also handles specific authentication
     * exceptions like first login or password expiry by generating a temporary
     * token for password change.
     *
     * @param request  The {@code HttpServletRequest} object containing the login credentials.
     * @param response The {@code HttpServletResponse} object for sending the authentication result.
     * @throws IOException If an I/O error occurs during the process.
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = null;
        String password = null;

        if (request.getContentType() != null) {
            if (request.getContentType().startsWith("application/json")) {
                // Parse JSON Data
                try (BufferedReader reader = request.getReader()) {
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(json.toString());

                    username = jsonNode.get("username").asText();
                    password = jsonNode.get("password").asText();
                } catch (Exception e) {
                    ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data.");
                    return;
                }
            } else if (request.getContentType().startsWith("application/x-www-form-urlencoded")) {
                // Handle x-www-form-urlencoded
                username = request.getParameter("username");
                password = request.getParameter("password");
            } else if (request.getContentType().startsWith("multipart/form-data")) {
                // Handle multipart/form-data
                username = request.getParameter("username");
                password = request.getParameter("password");
            }
        }

        if (username == null || password == null) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Fields cannot be null.");
            return;
        }

        try {
            // Invalidate the current session and create a new session to prevent session fixation
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);

            int authUserId = authService.authenticate(username, password);
            if (authUserId == -1) {
                session.invalidate();
                ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Incorrect username or password. Please Try again!");
                return;
            }

            // TODO: Implement Role Concept
            // List<Roles> roles = userRoleViewRepository.loadUserRoles(authUserId);
            // session.setAttribute("roles", roles);

            session.setAttribute("userId", authUserId);
            session.setMaxInactiveInterval(SecurityConfig.SESSION_TIMEOUT);

            ApiUtils.sendJsonResponse(response, "Login successful");
        } catch (FirstLoginException | PasswordExpiredException e) {
            // Generate temporary token
            String tokenPayload = String.format("%d-%d-%s", System.currentTimeMillis(), e.hashCode(), username); // Include username for context
            String tempToken = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tokenPayload.getBytes("UTF-8"));

          // Stores the temporary token in the session along with the reason and username
            HttpSession session = request.getSession(true);
            session.setAttribute("tempToken", tempToken);
            session.setAttribute("passwordChangeReason", e.getMessage());
            session.setAttribute("username", username);
            session.setMaxInactiveInterval(3600); // Session valid for 1 hour

            // Send the temporary token in the JSON response
            ApiUtils.sendJsonResponse(response, Map.of("tempToken", tempToken, "reason", e.getMessage()));
        } catch (AccountLockedException | AccountDeactivatedException e) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    /**
     * Handles the user logout process. It invalidates the current session,
     * effectively logging the user out.
     *
     * @param request  The {@code HttpServletRequest} object.
     * @param response The {@code HttpServletResponse} object for sending the logout confirmation.
     * @throws IOException If an I/O error occurs during the process.
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        session.invalidate();
        ApiUtils.sendJsonResponse(response, "Logout successful!");
    }

    /**
     * Handles the process of changing a user's password. This endpoint is typically
     * used when a user needs to change their password due to it being their first
     * login or due to password expiry. It verifies a temporary token stored in the
     * session before allowing the password change.
     *
     * @param request  The {@code HttpServletRequest} object containing the new password and token.
     * @param response The {@code HttpServletResponse} object for sending the result of the password change.
     * @throws IOException If an I/O error occurs during the process.
     */
    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("tempToken") == null) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access to change password.");
            return;
        }

        String tempTokenFromSession = (String) session.getAttribute("tempToken");
        String usernameFromSession = (String) session.getAttribute("username");

        String newPassword = null;
        String confirmNewPassword = null;
        String tokenFromRequest = null;

        // handle JSON requests
        if (request.getContentType() != null && request.getContentType().startsWith("application/json")) {
            // Parse JSON Data
            try (BufferedReader reader = request.getReader()) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(json.toString());

                newPassword = jsonNode.get("newPassword").asText();
                confirmNewPassword = jsonNode.get("confirmNewPassword").asText();
                tokenFromRequest = jsonNode.get("token").asText();
            } catch (Exception e) {
                ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON data.");
                return;
            }
        } else if (request.getContentType().startsWith("application/x-www-form-urlencoded")) {
            // Handle x-www-form-urlencoded
            newPassword = request.getParameter("newPassword");
            confirmNewPassword = request.getParameter("confirmNewPassword");
            tokenFromRequest = request.getParameter("token");
        } else if (request.getContentType().startsWith("multipart/form-data")) {
            // Handle multipart/form-data
            newPassword = request.getParameter("newPassword");
            confirmNewPassword = request.getParameter("confirmNewPassword");
            tokenFromRequest = request.getParameter("token");
        }

        if (newPassword  == null || confirmNewPassword == null) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid input.");
            return;
        }

        if (tokenFromRequest == null || !tokenFromRequest.equals(tempTokenFromSession)) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token for password change.");
            return;
        }

        try {
            Integer userId = new UserService().getUserId(usernameFromSession);

            boolean success = authService.changePassword(userId, userId, newPassword, confirmNewPassword);
            if (!success) {
                ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to update password!");
                return;
            }

            session.invalidate();
            ApiUtils.sendJsonResponse(response, "Password changed successfully!");
        } catch (IllegalArgumentException e) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ApiUtils.sendExceptionResponse(response, null, e);
        } catch (SecurityException e) {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }
}