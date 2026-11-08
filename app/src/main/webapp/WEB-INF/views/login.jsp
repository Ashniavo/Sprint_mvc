<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Connexion</title></head>
<body>
    <h1>Connexion</h1>

    <p style="color: red; font-weight: bold;">${error}</p>

    <form action="${pageContext.request.contextPath}/api/dept/connexion" method="post">
        <input type="text" name="nom" placeholder="nom"><br><br>
        <select name="role">
            <option value="user">user</option>
            <option value="admin">admin</option>
        </select><br><br>
        <button type="submit">Se connecter</button>
    </form>

    <ul>
        <li><a href="${pageContext.request.contextPath}/api/dept/protege">Zone protegee (connecte)</a></li>
        <li><a href="${pageContext.request.contextPath}/api/dept/admin">Zone admin (role admin)</a></li>
        <li><a href="${pageContext.request.contextPath}/api/dept/deconnexion">Se deconnecter</a></li>
    </ul>
</body>
</html>
