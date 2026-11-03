<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>404 · Ruta no encontrada — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body class="error-page">

<main class="error-sheet">
    <p class="error-code">404</p>
    <h1 class="error-title">Ruta no encontrada</h1>
    <p class="error-text">La ruta solicitada no existe en el registro de la aplicación.</p>

    <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

    <div class="error-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Volver al tablero</a>
        <a class="btn" href="${pageContext.request.contextPath}/clientes">Ir a clientes</a>
    </div>
</main>

</body>
</html>
