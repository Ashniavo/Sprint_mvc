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
        String uri = req.getRequestURI();
        PrintWriter out = res.getWriter();

        out.println("<html><body>");
        out.println(uri);
        printClasses(out);
        out.println("<body><html>");
    }

    private void printClasses(PrintWriter out) {
        for (String s : this.listClasses) {
            out.println("\n");
            out.println(s);
        }
    }
}
