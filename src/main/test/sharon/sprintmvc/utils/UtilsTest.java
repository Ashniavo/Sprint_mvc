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

        Map<String, Mapping> urlMap = Utils.buildUrlMapping(controllers);

        Assertions.assertTrue(urlMap.containsKey("/dept/new"));
        Assertions.assertTrue(urlMap.containsKey("/dept/list"));
    }
}