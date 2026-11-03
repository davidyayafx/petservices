<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>500 · Error del servidor — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body class="error-page">

<main class="error-sheet">
    <p class="error-code">500</p>
    <h1 class="error-title">Error del servidor</h1>
    <p class="error-text">La operación no pudo completarse. Vuelve a intentarlo desde el tablero.</p>

    <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

    <div class="error-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Volver al tablero</a>
        <a class="btn" href="${pageContext.request.contextPath}/servicios">Ir a servicios</a>
    </div>
</main>

</body>
</html>
