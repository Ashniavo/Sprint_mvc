package main.java.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Classe utilitaire pour scanner un package et recuperer les classes
 * annotees par une annotation donnee.
 */
public class Utils {

    /**
     * Scanne le package donne (et ses sous-packages) et retourne la liste
     * des classes annotees par l'annotation passee en parametre.
     *
     * @param packageName  le package a scanner (ex: "main.java.controllers")
     * @param annotation   l'annotation recherchee (ex: Controller.class)
     * @return la liste des classes annotees trouvees
     */
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
        if (files == null) {
            return;
        }
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

    /**
     * Transforme une liste de classes en une liste de noms (FQCN).
     */
    public static List<String> intoString(List<Class<?>> classes) {
        List<String> names = new ArrayList<>();
        for (Class<?> c : classes) {
            names.add(c.getName());
        }
        return names;
    }
}
