package sharon.sprintmvc.utils;

import sharon.sprintmvc.annotation.Controller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.List;
import java.util.Map;

public class UtilsTest {

    @Test
    public void testLoadClassesTrouveLesControllers() throws Exception {
        List<Class<?>> result = Utils.loadClasses(
            "sharon.sprintmvc.controllers",
            Controller.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty(),
            "Aucun @Controller trouve dans le package");
    }

    @Test
    public void testBuildUrlMappingContientLesURLs() throws Exception {
        List<Class<?>> controllers = Utils.loadClasses(
            "sharon.sprintmvc.controllers",
            Controller.class
        );

        Map<UrlKey, Mapping> urlMap = Utils.buildUrlMapping(controllers);

        UrlKey newKey = new UrlKey("/dept/new", "GET");
        UrlKey listKey = new UrlKey("/dept/list", "GET");

        Assertions.assertTrue(urlMap.containsKey(newKey));
        Assertions.assertTrue(urlMap.containsKey(listKey));
    }

    @Test
    public void testUrlInconnueNestPasDansLeMapping() throws Exception {
        List<Class<?>> controllers = Utils.loadClasses(
            "sharon.sprintmvc.controllers",
            Controller.class
        );

        Map<UrlKey, Mapping> urlMap = Utils.buildUrlMapping(controllers);

        UrlKey unknownKey = new UrlKey("/n_importe_quoi", "GET");
        Assertions.assertFalse(urlMap.containsKey(unknownKey));
    }
}