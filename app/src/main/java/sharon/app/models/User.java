package sharon.app.models;

public class User {
    private String nom;
    private int age;

    public User() {}

    public String getNom() { return nom; }
    public int getAge() { return age; }

    @Override
    public String toString() { return "nom=" + nom + ", age=" + age; }
}
