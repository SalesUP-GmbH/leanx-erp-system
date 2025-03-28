package com.leanx.app.security.filters;

import java.io.IOException;

import com.leanx.app.utils.ApiUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/api/*")
public class SessionValidationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Optional initialization
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        if (path.endsWith("/login") || path.endsWith("/logout") || path.endsWith("/change-password")) {
            chain.doFilter(request, response);
            return;
        }

        // Get the session if it exists
        HttpSession session = httpRequest.getSession(false); // Don't create a new session if none exists
        if (session == null) {
            ApiUtils.sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "invalid or inactive session!");
            return;
        }

        if (session.getAttribute("userId") == null) {
            ApiUtils.sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized session!");
            return;
        }
        
        // Continue the request chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Optional cleanup
    }
    
}