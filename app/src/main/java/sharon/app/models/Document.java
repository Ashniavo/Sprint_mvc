package sharon.app.models;

import sharon.sprintmvc.utils.Mapfiles;

public class Document {

    private String titre;
    private Mapfiles fichier;

    public Document() {}

    public String getTitre() { return titre; }
    public Mapfiles getFichier() { return fichier; }

    @Override
    public String toString() {
        return "titre=" + titre + ", fichier="
                + (fichier == null ? "aucun"
                    : fichier.getNom() + " (" + fichier.getSize() + " octets)");
    }
}
