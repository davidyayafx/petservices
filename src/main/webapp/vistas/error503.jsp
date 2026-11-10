<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>503 · Base de datos no disponible — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body class="error-page">

<main class="error-sheet">
    <p class="error-code">503</p>
    <h1 class="error-title">Base de datos no disponible</h1>
    <p class="error-text">
        La aplicación no pudo acceder a su base de datos local. Verifica que la carpeta
        <code>datos</code> exista y tenga permisos de escritura, y vuelve a intentarlo.
    </p>
    <%
        String detalle = (String) request.getAttribute("mensajeError");
        if (detalle != null && !detalle.trim().isEmpty()) {
    %>
        <p class="error-text"><%= esc(detalle) %></p>
    <%
        }
    %>

    <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

    <div class="error-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Volver al tablero</a>
        <a class="btn" href="${pageContext.request.contextPath}/login">Ir al inicio de sesión</a>
    </div>
</main>

</body>
</html>
