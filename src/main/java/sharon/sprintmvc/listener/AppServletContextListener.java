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

        // Recuperer les packages a scanner depuis web.xml
        String blockPackage = appContext.getInitParameter("package_list");
        String separator = appContext.getInitParameter("list_separator");
        String[] packageList = blockPackage.split(separator);

        // Scanner tous les packages et collecter les @Controller
        Set<Class<?>> withAnnotation = new HashSet<>();
        List<Class<?>> classList;

        for (String pkg : packageList) {
            try {
                List<Class<?>> classes = Utils.loadClasses(pkg.trim(), Controller.class);
                withAnnotation.addAll(classes);
            } catch (Exception e) {
                System.err.println("[ERREUR] Scan du package " + pkg + " : " + e.getMessage());
            }
        }

        System.out.println("[INFO] Controllers trouves : " + withAnnotation.size());

        classList = new ArrayList<>(withAnnotation);

        try {
            Map<UrlKey, Mapping> routesMapping = Utils.buildUrlMapping(classList);

            // Stocker dans le contexte
            appContext.setAttribute("routesWithMethod", routesMapping);
            appContext.setAttribute("controllerList", classList);

            System.out.println("[SUCCESS] " + routesMapping.size() + " routes chargees.");

        } catch (IllegalStateException e) {
            System.err.println("[ERREUR CRITIQUE] " + e.getMessage());
            throw new RuntimeException("Conflit de routes detecte.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SHUTDOWN] L'application s'arrete.");
    }
}