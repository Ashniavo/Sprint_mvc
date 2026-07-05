package sharon.sprintmvc.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import sharon.sprintmvc.annotation.Controller;
import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;

public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    System.out.println("[INIT] Demarrage — scan des routes...");

    ServletContext appContext = sce.getServletContext();

    // Sprint 4 : lire plusieurs packages depuis context-param
    String blockPackage = appContext.getInitParameter("package_list");
    String separator = appContext.getInitParameter("list_separator");

    if (blockPackage == null || blockPackage.isEmpty()) {
        System.err.println("[ERREUR] Parametre 'package_list' manquant dans web.xml");
        return;
    }

    String[] packageList = blockPackage.split(separator != null ? separator : ";");

    Set<Class<?>> withAnnotation = new HashSet<>();

    for (String pkg : packageList) {
        try {
            List<Class<?>> classes = Utils.loadClasses(pkg.trim(), Controller.class);
            withAnnotation.addAll(classes);
            System.out.println("[INFO] Package scanne : " + pkg.trim() + " → " + classes.size() + " controller(s)");
        } catch (Exception e) {
            System.err.println("[ERREUR] Scan du package " + pkg + " : " + e.getMessage());
        }
    }

    List<Class<?>> classList = new ArrayList<>(withAnnotation);

    try {
        Map<UrlKey, Mapping> routesMapping = Utils.buildUrlMapping(classList);

        appContext.setAttribute("routesWithMethod", routesMapping);
        appContext.setAttribute("controllerList", classList);

        System.out.println("[SUCCESS] " + routesMapping.size() + " routes chargees.");

    } catch (IllegalStateException e) {
        System.err.println("[ERREUR CRITIQUE] " + e.getMessage());
        throw new RuntimeException("Conflit de routes detecte.", e);
    } catch (Exception e) {
        System.err.println("[ERREUR] " + e.getMessage());
        throw new RuntimeException("Echec du scan des routes.", e);
    }
}
}