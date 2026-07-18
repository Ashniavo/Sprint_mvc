<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head><title>Liste des departements</title></head>
<body>
    <h2>Liste des departements</h2>
    <ul>
        <%
            List<String> depts = (List<String>) request.getAttribute("depts");
            if (depts != null) {
                for (String dept : depts) {
        %>
            <li><%= dept %></li>
        <%
                }
            }
        %>
    </ul>
</body>
</html>