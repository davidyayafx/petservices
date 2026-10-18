<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  Vista: error500.jsp — Página personalizada de error 500.
  Referenciada desde WEB-INF/web.xml → <error-page> para el código 500.

  Proyecto: PetServices - SENA GA10-220501097-AA7-EV01
--%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error 500 — PetServices</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <script>
        (function() {
            var theme = localStorage.getItem('petservices_theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
            if (theme === 'dark') document.documentElement.setAttribute('data-theme', 'dark');
        })();
    </script>
</head>
<body class="auth-page">

<button type="button" class="theme-toggle-floating" onclick="toggleTheme()" id="themeToggleBtn">
    <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
    <span id="themeToggleText">Modo Oscuro</span>
</button>

<div class="auth-card" style="max-width: 480px;">
    <div class="auth-form-container" style="padding: 40px 36px; text-align: center;">

        <div style="font-size: 4rem; font-weight: 800; letter-spacing: -0.02em; line-height: 1; margin-bottom: 16px; color: #dc2626;">
            500
        </div>

        <div class="brand-header" style="border: none; padding: 0; justify-content: center; margin-bottom: 18px;">
            <div class="brand-logo">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10 5.172a2 2 0 0 0-3.414-1.414l-3.758 3.758a2 2 0 0 0 0 2.828l10.828 10.828a2 2 0 0 0 2.828 0l3.758-3.758a2 2 0 0 0 0-2.828L10.414 4.758"/>
                    <circle cx="12" cy="12" r="3"/>
                </svg>
            </div>
            <div class="brand-title">PetServices</div>
        </div>

        <h1 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 10px;">Error interno del servidor</h1>
        <p style="color: var(--color-text-muted); font-size: 0.92rem; line-height: 1.6; margin-bottom: 28px;">
            Ocurrió un error inesperado al procesar tu solicitud.<br>
            Inténtalo de nuevo más tarde o contacta al administrador.
        </p>

        <%
            // Si hay sesión activa → al dashboard; si no → al login
            com.petservices.modelo.Cliente usuarioSesion =
                    (com.petservices.modelo.Cliente) session.getAttribute("clienteSesion");
            String destinoInicio = (usuarioSesion != null)
                    ? request.getContextPath() + "/dashboard"
                    : request.getContextPath() + "/login";
        %>
        <a href="<%= destinoInicio %>" class="btn btn-primary" style="display: inline-block; padding: 11px 28px; text-decoration: none;">
            Volver al inicio
        </a>

    </div>
</div>

<script>
    function updateThemeUI() {
        var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        var textEl = document.getElementById('themeToggleText');
        if (textEl) textEl.textContent = isDark ? 'Modo Claro' : 'Modo Oscuro';
    }

    function toggleTheme() {
        var isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        if (isDark) {
            document.documentElement.removeAttribute('data-theme');
            localStorage.setItem('petservices_theme', 'light');
        } else {
            document.documentElement.setAttribute('data-theme', 'dark');
            localStorage.setItem('petservices_theme', 'dark');
        }
        updateThemeUI();
    }

    document.addEventListener('DOMContentLoaded', updateThemeUI);
</script>

</body>
</html>
