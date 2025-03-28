package com.leanx.app.api.employee.self;

import java.io.IOException;
import java.sql.SQLException;

import com.leanx.app.model.dto.EmployeeProfile;
import com.leanx.app.service.modules.employee.self.EmployeeSelfService;
import com.leanx.app.utils.ApiUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet(name = "EmployeeSelfController", urlPatterns = "/api/employee/self/*")
public class EmployeeSelfController extends HttpServlet {

    private final EmployeeSelfService employeeSelfService = new EmployeeSelfService();

    // GET /employee/self - Get personal employee information
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        HttpSession session = request.getSession(false);
        Integer currentUserId = (Integer) session.getAttribute("userId");

        if (pathInfo == null || pathInfo.equals("/")) {
            handleGetEmployeeProfile(currentUserId, response);
        } else {
            ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Unknown endpoint!");
        }
    }

     private void handleGetEmployeeProfile(Integer userId, HttpServletResponse response) throws IOException {
        try {
            EmployeeProfile employeeProfile = employeeSelfService.getPersonalEmployeeProfile(userId);
            if (employeeProfile == null) {
                ApiUtils.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Error fetching personal employee profile!");
                return;
            }
            
            ApiUtils.sendJsonResponse(response, employeeProfile);
        } catch (SQLException e) {
            ApiUtils.sendExceptionResponse(response, null, e);
        }
    }
}
