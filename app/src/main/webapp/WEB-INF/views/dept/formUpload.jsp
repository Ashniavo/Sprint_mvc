<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head><title>Upload</title></head>
<body>
    <h1>Un fichier</h1>
    <form action="${pageContext.request.contextPath}/api/dept/upload"
          method="post" enctype="multipart/form-data">
        <input type="text" name="nom" placeholder="nom"><br><br>
        <input type="file" name="fichier"><br><br>
        <button type="submit">Envoyer</button>
    </form>

    <h1>Plusieurs fichiers</h1>
    <form action="${pageContext.request.contextPath}/api/dept/uploadMulti"
          method="post" enctype="multipart/form-data">
        <input type="file" name="fichiers" multiple><br><br>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>
