package main.java.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import main.java.annotation.Controller;
import main.java.utils.Utils;

public class FrontControllerServlet extends HttpServlet {

    List<String> listClasses;

    public void init() throws ServletException {
        String initial = this.getInitParameter("Controller");
        try {
            listClasses = Utils.intoString(Utils.loadClasses(initial, Controller.class));
        } catch (Exception e) {
            e.printStackTrace();
            listClasses = null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        PrintWriter out = res.getWriter();

        StringBuffer fullUrl = req.getRequestURL();
        if (req.getQueryString() != null) {
            fullUrl.append("?").append(req.getQueryString());
        }

        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Sprint MVC</title></head><body>");
        out.println("<h2>URL demandee :</h2>");
        out.println("<p style='font-size:1.5em; color:#2a6;'>" + fullUrl + "</p>");

        out.println("<h2>Controleurs detectes (@Controller) :</h2>");
        printClasses(out);

        out.println("</body></html>");
    }

    private void printClasses(PrintWriter out) {
        if (this.listClasses == null || this.listClasses.isEmpty()) {
            out.println("<p>Aucun controleur trouve.</p>");
            return;
        }
        out.println("<ul>");
        for (String s : this.listClasses) {
            out.println("<li>" + s + "</li>");
        }
        out.println("</ul>");
    }
}