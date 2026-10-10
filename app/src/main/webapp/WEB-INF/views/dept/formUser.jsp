<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Formulaire user / ecole</title></head>
<body>
    <h1>Envoyer des objets</h1>
    <form action="${pageContext.request.contextPath}/api/dept/saveUser" method="post">
        <h3>User</h3>
        <input type="text" name="user.nom" placeholder="nom"><br>
        <input type="number" name="user.age" placeholder="age"><br>

        <h3>Ecole</h3>
        <input type="text" name="ecole.nom" placeholder="nom"><br>
        <input type="text" name="ecole.recteur" placeholder="recteur"><br>

        <h3>Date</h3>
        <input type="date" name="dateInsertion"><br><br>

        <button type="submit">Envoyer</button>
    </form>
</body>
</html>
