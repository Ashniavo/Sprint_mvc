<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="sharon.app.entity.Departement" %>
<!DOCTYPE html>
<html>
<head><title>Liste des departements</title></head>
<body>
    <h2>Liste des departements</h2>
    <table border="1">
        <tr>
            <th>ID</th>
            <th>Nom</th>
        </tr>
        <%
            List<Departement> depts = (List<Departement>) request.getAttribute("depts");
            if (depts != null) {
                for (Departement dept : depts) {
        %>
        <tr>
            <td><%= dept.getId() %></td>
            <td><%= dept.getNom() %></td>
        </tr>
        <%
                }
            }
        %>
    </table>
</body>
</html>