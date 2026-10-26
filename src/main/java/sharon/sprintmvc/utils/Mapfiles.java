package sharon.sprintmvc.utils;

/** Fichier recu par upload : nom, extension et contenu. */
public class Mapfiles {

    private String nom;
    private String extension;
    private byte[] file;

    public Mapfiles() {}

    public Mapfiles(String nom, String extension, byte[] file) {
        this.nom = nom;
        this.extension = extension;
        this.file = file;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public byte[] getFile() { return file; }
    public void setFile(byte[] file) { this.file = file; }

    public long getSize() { return file == null ? 0 : file.length; }
}
