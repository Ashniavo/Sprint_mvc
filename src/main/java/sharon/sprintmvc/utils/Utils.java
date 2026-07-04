package sharon.sprintmvc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import sharon.sprintmvc.annotation.URLMapping;

public class Utils {

    private static Properties loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = Utils.class.getClassLoader()
                                            .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties introuvable !");
            }
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture config.properties", e);
        }
        return prop;
    }

    public static List<Class<?>> loadClasses(String packageName,
                                              Class<? extends Annotation> annotation)
            throws Exception {
        List<Class<?>> result = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(packageName)
                .scan()) {
            ClassInfoList classes = scanResult.getClassesWithAnnotation(annotation.getName());
            for (ClassInfo classInfo : classes) {
                result.add(Class.forName(classInfo.getName()));
            }
        }
        return result;
    }

    public static List<String> intoString(List<Class<?>> classes) {
        List<String> names = new ArrayList<>();
        for (Class<?> c : classes) {
            names.add(c.getName());
        }
        return names;
    }

    public static Map<UrlKey, Mapping> buildUrlMapping(List<Class<?>> controllers)
            throws IllegalStateException {
        Map<UrlKey, Mapping> urlMap = new HashMap<>();
        for (Class<?> controllerClass : controllers) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(URLMapping.class)) {
                    URLMapping urlMapping = method.getAnnotation(URLMapping.class);
                    UrlKey key = new UrlKey(urlMapping.value(), urlMapping.method());

                    // Detection de conflit
                    if (urlMap.containsKey(key)) {
                        Mapping existing = urlMap.get(key);
                        throw new IllegalStateException(
                            "[ERREUR] Conflit de routes detecte !\n" +
                            "La route " + key + " est deja associee a : " +
                            existing.getControllerClass().getName() + "." +
                            existing.getMethod().getName() + "()\n" +
                            "Impossible de la reassigner a : " +
                            controllerClass.getName() + "." + method.getName() + "()"
                        );
                    }

                    urlMap.put(key, new Mapping(controllerClass, method));
                }
            }
        }
        return urlMap;
    }

    public static Map<UrlKey, Mapping> buildUrlMappingFromConfig() throws Exception {
        Properties prop = loadConfig();
        String packageName = prop.getProperty("app.package");

        @SuppressWarnings("unchecked")
        Class<? extends Annotation> controllerAnnotation =
            (Class<? extends Annotation>) Class.forName(
                prop.getProperty("annotation.controller"));

        List<Class<?>> controllers = loadClasses(packageName, controllerAnnotation);
        return buildUrlMapping(controllers);
    }
}