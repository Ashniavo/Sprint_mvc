#!/bin/bash

TOMCAT_DIR="/opt/tomcat"

echo "=== Sprint MVC — Build & Deploy ==="

# 1. Compiler et generer sprint0.jar + sprint0.war
echo ">>> Compilation Maven..."
mvn package -q
if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation"
    exit 1
fi
echo "✅ sprint0.jar genere → target/sprint0.jar"
echo "✅ sprint0.war genere → target/sprint0.war"

# 2. Arreter Tomcat
echo ">>> Arret de Tomcat..."
"$TOMCAT_DIR/bin/shutdown.sh" 2>/dev/null
sleep 2

# 3. Copier le .war dans Tomcat
echo ">>> Deploiement..."
cp target/sprint0.war "$TOMCAT_DIR/webapps/"
echo "✅ sprint0.war deploye sur Tomcat"

# 4. Demarrer Tomcat
echo ">>> Demarrage de Tomcat..."
"$TOMCAT_DIR/bin/startup.sh"
sleep 3

echo ""
echo "=== ✅ Projet lance ! ==="
ls -lh target/sprint0.jar
ls -lh target/sprint0.war

