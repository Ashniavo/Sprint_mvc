# Sprint MVC

Mini framework Java MVC — affiche l'URL tapée dans le navigateur.

## Structure

```
sprint_mvc/
├── pom.xml                                              ← config Maven, genere sprint0.jar
├── README.md
├── sprint_list.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── mg/conquerant/sprintmvc/
    │   │       └── controller/
    │   │           └── FrontControllerServlet.java      ← servlet principale
    │   └── resources/
    │       └── web.xml                                  ← config Tomcat
    └── test/
        └── java/
```

## Utilisation

```bash
# Compiler et generer sprint0.jar
mvn compile
mvn package

# Le jar est dans :
# target/sprint0.jar
```


