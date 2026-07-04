package sharon.sprintmvc.listener;

import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import sharon.sprintmvc.utils.Mapping;
import sharon.sprintmvc.utils.UrlKey;
import sharon.sprintmvc.utils.Utils;

public class AppServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[INIT] Tomcat demarre l'application. Lancement du scan des routes...");

        try {
            Map<UrlKey, Mapping> routes = Utils.buildUrlMappingFromConfig();
            sce.getServletContext().setAttribute("routesWithMethod", routes);
            System.out.println("[SUCCESS] Scan termine avec succes. " + routes.size() + " routes chargees.");

        } catch (IllegalStateException e) {
            System.err.println("[ERREUR CRITIQUE DEMARRAGE] " + e.getMessage());
            throw new RuntimeException("Echec du deploiement a cause d'un conflit de routes.", e);

        } catch (Exception e) {
            System.err.println("[ERREUR] " + e.getMessage());
            throw new RuntimeException("Echec du scan des routes.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[SHUTDOWN] L'application s'arrete.");
    }
}