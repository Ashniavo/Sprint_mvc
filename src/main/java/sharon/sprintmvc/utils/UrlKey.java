package sharon.sprintmvc.utils;

public class UrlKey {

    private String url;
    private String httpMethod;

    public UrlKey(String url, String httpMethod) {
        this.url = url;
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlKey)) return false;
        UrlKey other = (UrlKey) obj;
        return this.url.equals(other.url) && this.httpMethod.equals(other.httpMethod);
    }

    @Override
    public int hashCode() {
        return (url + httpMethod).hashCode();
    }

    @Override
    public String toString() {
        return url + " [" + httpMethod + "]";
    }
}