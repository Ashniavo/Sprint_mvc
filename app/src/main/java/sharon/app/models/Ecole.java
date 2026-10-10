package sharon.app.models;

public class Ecole {
    private String nom;
    private String recteur;

    public Ecole() {}

    public String getNom() { return nom; }
    public String getRecteur() { return recteur; }

    @Override
    public String toString() { return "nom=" + nom + ", recteur=" + recteur; }
}
