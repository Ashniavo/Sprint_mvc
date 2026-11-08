package sharon.sprintmvc.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import sharon.sprintmvc.annotation.NeedLogin;

public class SecurityService {

    private final String isConnectedSessionName;

    public SecurityService(String isConnectedSessionName) {
        this.isConnectedSessionName = isConnectedSessionName;
    }

    public boolean needsLogin(Method method) {
        return method.isAnnotationPresent(NeedLogin.class)
                || method.getDeclaringClass().isAnnotationPresent(NeedLogin.class);
    }

    public boolean isConnected(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute(isConnectedSessionName) != null;
    }

    public Optional<MySpringUser> getUserConnected(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object obj = session.getAttribute(isConnectedSessionName);
        if (obj instanceof MySpringUser) {
            return Optional.of((MySpringUser) obj);
        }
        return Optional.empty();
    }

    public boolean accesAuthorised(HttpServletRequest req, Method method) {
        NeedLogin needLogin = method.getAnnotation(NeedLogin.class);
        if (needLogin == null) {
            needLogin = method.getDeclaringClass().getAnnotation(NeedLogin.class);
        }
        if (needLogin == null) {
            return true;
        }

        Optional<MySpringUser> user = getUserConnected(req);
        if (user.isEmpty()) {
            return false;
        }

        String[] roles = needLogin.value();
        return roles.length == 0 || Arrays.asList(roles).contains(user.get().getRole());
    }
}
