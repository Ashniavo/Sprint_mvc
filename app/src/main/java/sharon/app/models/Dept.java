package sharon.app.models;

import sharon.sprintmvc.annotation.Param;

public class Dept {

    @Param(value = "nom", defaultValue = "-")
    private String nom;

    @Param(value = "prenom", defaultValue = "-")
    private String prenom;

    @Param(value = "age", defaultValue = "0")
    private int age;

    public Dept() {}

    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public int getAge() { return age; }

    @Override
    public String toString() {
        return "nom=" + nom + ", prenom=" + prenom + ", age=" + age;
    }
}
