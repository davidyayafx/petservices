<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Iniciar sesión — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body class="auth-page">

<button type="button" class="theme-toggle-floating" onclick="toggleTheme()" id="themeToggleBtn">
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
    <span id="themeToggleText">Modo Oscuro</span>
</button>

<div class="auth-card">
    <div class="auth-banner">
        <div>
            <div class="auth-brand">
                <span class="brand-mark" aria-hidden="true">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M12 21c-4.5-3-8-6-8-10a4.5 4.5 0 0 1 8-2.8A4.5 4.5 0 0 1 20 11c0 4-3.5 7-8 10z"/>
                    </svg>
                </span>
                <span class="brand-text">
                    <span class="brand-name">PetServices</span>
                    <span class="brand-sub">Plataforma de cuidado animal</span>
                </span>
            </div>

            <h2 class="auth-headline">El sistema integral de la clínica, en una sola hoja</h2>
            <p class="auth-lead">
                Clientes, pasaporte de mascotas, agenda de citas veterinarias y control de
                pedidos: todo el trabajo del día, reglado y a la vista.
            </p>
        </div>

        <p class="auth-foot">PetConnect Passport Care</p>
    </div>

    <div class="auth-form-container">
        <h1 class="auth-title">Iniciar sesión</h1>
        <p class="auth-subtitle">Ingresa tus credenciales para acceder al panel.</p>

        <%
            String mensaje = (String) request.getAttribute("mensaje");
            String tipoMensaje = (String) request.getAttribute("tipoMensaje");
            if (mensaje != null && !mensaje.isEmpty()) {
                String claseAlerta = "alert-error";
                if ("exito".equalsIgnoreCase(tipoMensaje)) {
                    claseAlerta = "alert-success";
                }
        %>
            <div class="alert <%= claseAlerta %>" role="alert">
                <span><%= esc(mensajePlano(mensaje)) %></span>
            </div>
        <%
            }
        %>

        <form class="auth-form" action="<%= request.getContextPath() %>/login" method="post">
            <div class="form-group">
                <label class="form-label" for="correo">Correo electrónico</label>
                <input class="form-control" type="email" id="correo" name="correo"
                       placeholder="ejemplo@correo.com" required autofocus>
            </div>

            <div class="form-group">
                <label class="form-label" for="contrasena">Contraseña</label>
                <input class="form-control" type="password" id="contrasena" name="contrasena"
                       placeholder="Mínimo 6 caracteres" required>
            </div>

            <button type="submit" class="btn btn-primary btn-block">Ingresar al sistema</button>
        </form>

        <p class="auth-switch">
            ¿No tienes una cuenta aún?
            <a href="<%= request.getContextPath() %>/registro">Regístrate aquí</a>
        </p>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>

</body>
</html>
