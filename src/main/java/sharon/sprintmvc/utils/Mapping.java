package sharon.sprintmvc.utils;

import java.lang.reflect.Method;

public class Mapping {

    private final Class<?> controllerClass;
    private final Method method;

    public Mapping(Class<?> controllerClass, Method method) {
        this.controllerClass = controllerClass;
        this.method = method;
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public Method getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return controllerClass.getSimpleName() + "#" + method.getName() + "()";
    }
}