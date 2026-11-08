package sharon.app.models;

import java.io.Serializable;

import sharon.sprintmvc.utils.MySpringUser;

public class Utilisateur implements MySpringUser, Serializable {

    private String nom;
    private String role;

    public Utilisateur(String nom, String role) {
        this.nom = nom;
        this.role = role;
    }

    public String getNom() { return nom; }

    @Override
    public String getRole() { return role; }

    @Override
    public String toString() { return nom + " (" + role + ")"; }
}
