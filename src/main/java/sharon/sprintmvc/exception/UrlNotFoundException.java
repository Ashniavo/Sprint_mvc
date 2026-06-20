package sharon.sprintmvc.exception;

public class UrlNotFoundException extends Exception {
    public UrlNotFoundException(String url) {
        super("URL non trouvee : " + url);
    }
}