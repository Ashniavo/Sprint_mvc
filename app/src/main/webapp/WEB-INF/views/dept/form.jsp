<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Nouveau departement</title></head>
<body>
    <h1>Sauvegarder un departement</h1>
    <form action="${pageContext.request.contextPath}/api/dept/save" method="post">
        <label>Nom :</label>
        <input type="text" name="nom"><br><br>

        <label>Prenom :</label>
        <input type="text" name="prenom"><br><br>

        <label>Age :</label>
        <input type="number" name="age"><br><br>

        <button type="submit">Sauvegarder</button>
    </form>
</body>
</html>