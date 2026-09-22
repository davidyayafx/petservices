<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Crear Cuenta — PetServices</title>
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

<div class="auth-card" style="max-width: 960px;">
    <div class="auth-banner">
        <div>
            <div class="brand-header" style="border: none; padding: 0; margin-bottom: 24px;">
                <div class="brand-logo" style="background: rgba(255, 255, 255, 0.15); border: 1px solid rgba(255, 255, 255, 0.2);">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #fde68a;">
                        <path d="M12 2a5 5 0 0 0-5 5c0 1.5.5 3 1.5 4L12 21l3.5-10c1-1 1.5-2.5 1.5-4a5 5 0 0 0-5-5z"></path>
                        <circle cx="12" cy="7" r="2"></circle>
                    </svg>
                </div>
                <div>
                    <div class="brand-title" style="letter-spacing: -0.01em;">PetServices</div>
                    <div class="brand-subtitle" style="color: #a7f3d0; font-size: 0.78rem;">Plataforma de Cuidado y Pasaporte Animal</div>
                </div>
            </div>
            <h2 style="font-size: 1.75rem; font-weight: 700; line-height: 1.25; margin-bottom: 14px; color: #ffffff;">
                Crea tu cuenta de cliente
            </h2>
            <p style="color: #d1fae5; font-size: 0.92rem; line-height: 1.6; margin-bottom: 22px;">
                Únete a la red PetConnect y gestiona el historial clínico de tus mascotas, citas veterinarias y pedidos en un entorno seguro y profesional.
            </p>
            <ul style="color: #ecfdf5; font-size: 0.88rem; list-style: none; display: flex; flex-direction: column; gap: 10px;">
                <li style="display: flex; align-items: center; gap: 8px;">
                    <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: rgba(217, 119, 6, 0.3); color: #fde68a; font-size: 11px; font-weight: bold;">✓</span>
                    Registro inmediato sin esperas
                </li>
                <li style="display: flex; align-items: center; gap: 8px;">
                    <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: rgba(217, 119, 6, 0.3); color: #fde68a; font-size: 11px; font-weight: bold;">✓</span>
                    Historial y pasaporte clínico veterinario
                </li>
                <li style="display: flex; align-items: center; gap: 8px;">
                    <span style="display: inline-flex; align-items: center; justify-content: center; width: 20px; height: 20px; border-radius: 50%; background: rgba(217, 119, 6, 0.3); color: #fde68a; font-size: 11px; font-weight: bold;">✓</span>
                    Acceso directo a catálogo y citas agendadas
                </li>
            </ul>
        </div>
        <div style="font-size: 0.78rem; color: #a7f3d0; margin-top: 32px; border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 16px;">
            PetConnect Passport Care
        </div>
    </div>

    <div class="auth-form-container">
        <h1 class="auth-title">Crear Cuenta</h1>
        <p class="auth-subtitle">Completa el formulario para registrarte en el sistema.</p>

        <%
            String mensaje = (String) request.getAttribute("mensaje");
            String tipoMensaje = (String) request.getAttribute("tipoMensaje");
            if (mensaje != null && !mensaje.isEmpty()) {
                String claseAlerta = "alert-error";
                if ("exito".equalsIgnoreCase(tipoMensaje)) {
                    claseAlerta = "alert-success";
                }
        %>
            <div class="alert <%= claseAlerta %>">
                <span><%= mensaje %></span>
            </div>
        <%
            }
        %>

        <form action="<%= request.getContextPath() %>/registro" method="post">
            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="nombre">Nombre Completo</label>
                <input class="form-control" type="text" id="nombre" name="nombre"
                       placeholder="Ej: Daniel Rodríguez" required autofocus maxlength="100">
            </div>

            <div class="form-group" style="margin-bottom: 16px;">
                <label class="form-label" for="correo">Correo Electrónico</label>
                <input class="form-control" type="email" id="correo" name="correo"
                       placeholder="correo@ejemplo.com" required maxlength="100">
            </div>

            <div class="form-group" style="margin-bottom: 22px;">
                <label class="form-label" for="contrasena">Contraseña</label>
                <input class="form-control" type="password" id="contrasena" name="contrasena"
                       placeholder="Mínimo 6 caracteres" required minlength="6">
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                Completar Registro
            </button>
        </form>

        <div style="margin-top: 24px; text-align: center; font-size: 0.9rem; color: var(--color-text-muted);">
            ¿Ya tienes una cuenta registrada?
            <a href="<%= request.getContextPath() %>/login" style="color: var(--color-primary); font-weight: 600; text-decoration: none;">
                Inicia sesión aquí
            </a>
        </div>
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
