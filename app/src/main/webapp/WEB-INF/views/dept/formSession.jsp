<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Session</title></head>
<body>
    <h1>Session</h1>

    <form action="${pageContext.request.contextPath}/api/dept/login" method="post">
        <input type="text" name="nom" placeholder="nom">
        <button type="submit">Se connecter</button>
    </form>

    <ul>
        <li><a href="${pageContext.request.contextPath}/api/dept/profil">Voir le profil</a></li>
        <li><a href="${pageContext.request.contextPath}/api/dept/compteur">Compteur de visites</a></li>
        <li><a href="${pageContext.request.contextPath}/api/dept/logout">Se deconnecter</a></li>
    </ul>
</body>
</html>
