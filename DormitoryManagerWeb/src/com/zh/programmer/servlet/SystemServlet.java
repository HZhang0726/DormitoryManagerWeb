package com.zh.programmer.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/systemServlet")
public class SystemServlet extends HttpServlet {

    private static final long serialVersionUID = -7258264317769166483L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = request.getParameter("method");
        if ("toAdminView".equals(method)){
            request.getRequestDispatcher("view/system.jsp").forward(request,response);
        }
        if ("LoginOut".equals(method)){
            request.getSession().setAttribute("user",null);
            request.getSession().setAttribute("userType",null);
            response.sendRedirect("index.jsp");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request, response);
    }
}
