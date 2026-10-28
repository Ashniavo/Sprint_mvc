package sharon.sprintmvc.utils;

import java.util.HashMap;
import java.util.Map;

/** Session simplifiee que le controller recoit en argument. */
public class MySession {

    Map<String, Object> sessionData;

    public MySession() {
        this.sessionData = new HashMap<>();
    }

    public MySession(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    public Map<String, Object> getSessionData() {
        return sessionData;
    }

    public void setSessionData(Map<String, Object> sessionData) {
        this.sessionData = sessionData;
    }

    public Object getAttribute(String key) {
        return sessionData.get(key);
    }

    public void setAttribute(String key, Object value) {
        sessionData.put(key, value);
    }

    public void removeAttribute(String key) {
        sessionData.remove(key);
    }

    public void destroy() {
        sessionData.clear();
    }
}
