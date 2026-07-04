package sharon.sprintmvc.utils;

import java.util.Objects;

public class UrlKey {

    private final String url;
    private final String httpMethod;

    public UrlKey(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() { return url; }
    public String getHttpMethod() { return httpMethod; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        UrlKey other = (UrlKey) obj;
        if (url == null) {
            if (other.url != null) return false;
        } else if (!url.equals(other.url)) return false;
        if (httpMethod == null) {
            if (other.httpMethod != null) return false;
        } else if (!httpMethod.equals(other.httpMethod)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, httpMethod);
    }

    @Override
    public String toString() {
        return "[url: " + url + ", method : " + httpMethod + "]";
    }
}