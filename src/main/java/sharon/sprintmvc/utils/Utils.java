package sharon.sprintmvc.utils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sharon.sprintmvc.annotation.URLMapping;
import sharon.sprintmvc.utils.UrlKey;
public class Utils {

    public static List<Class<?>> loadClasses(String packageName, Class<? extends Annotation> annotation) throws Exception {
        List<Class<?>> result = new ArrayList<>();
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            if (directory.exists() && directory.isDirectory()) {
                findClasses(directory, packageName, annotation, result);
            }
        }
        return result;
    }

    private static void findClasses(File directory, String packageName, Class<? extends Annotation> annotation, List<Class<?>> result) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                findClasses(file, packageName + "." + file.getName(), annotation, result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotation)) {
                    result.add(clazz);
                }
            }
        }
    }

    public static List<String> intoString(List<Class<?>> classes) {
        List<String> names = new ArrayList<>();
        for (Class<?> c : classes) {
            names.add(c.getName());
        }
        return names;
    }

    public static Map<UrlKey, Mapping> buildUrlMapping(List<Class<?>> controllers) {
    Map<UrlKey, Mapping> urlMap = new HashMap<>();
    for (Class<?> controllerClass : controllers) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(URLMapping.class)) {
                URLMapping urlMapping = method.getAnnotation(URLMapping.class);
                UrlKey key = new UrlKey(urlMapping.value(), urlMapping.method());
                urlMap.put(key, new Mapping(controllerClass, method));
            }
        }
    }
    return urlMap;
}
}