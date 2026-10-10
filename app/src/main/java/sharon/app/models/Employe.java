package sharon.app.models;

import sharon.sprintmvc.annotation.Param;

public class Employe {

    @Param(value = "nom", defaultValue = "-")
    private String nom;

    @Param(value = "age", defaultValue = "0")
    private int age;

    public Employe() {}

    public String getNom() { return nom; }
    public int getAge() { return age; }

    @Override
    public String toString() {
        return "{nom=" + nom + ", age=" + age + "}";
    }
}
