<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cita" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String badgeEstadoClass(String estado) {
        String val = (estado == null) ? "" : estado.trim().toLowerCase();
        if ("confirmada".equals(val)) return "badge-success";
        if ("cancelada".equals(val))  return "badge-danger";
        if ("realizada".equals(val))  return "badge-success";
        return "badge-warning";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — PetServices</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/app.css">
    <script>
        (function() {
            var theme = localStorage.getItem('petservices_theme') || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
            if (theme === 'dark') document.documentElement.setAttribute('data-theme', 'dark');
        })();
    </script>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";

    int totalClientes  = (Integer) request.getAttribute("totalClientes");
    int totalMascotas  = (Integer) request.getAttribute("totalMascotas");
    int totalCitas     = (Integer) request.getAttribute("totalCitas");
    int totalProductos = (Integer) request.getAttribute("totalProductos");
    int totalServicios = (Integer) request.getAttribute("totalServicios");
    int totalPedidos   = (Integer) request.getAttribute("totalPedidos");

    List<Cita> ultimasCitas = (List<Cita>) request.getAttribute("ultimasCitas");
%>

<div class="app-layout">

    <!-- ── Sidebar de Navegación ── -->
    <aside class="app-sidebar">
        <div class="brand-header">
            <div class="brand-logo">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M10 5.172a2 2 0 0 0-3.414-1.414l-3.758 3.758a2 2 0 0 0 0 2.828l10.828 10.828a2 2 0 0 0 2.828 0l3.758-3.758a2 2 0 0 0 0-2.828L10.414 4.758"/>
                    <circle cx="12" cy="12" r="3"/>
                </svg>
            </div>
            <div>
                <div class="brand-title">PetServices</div>
                <div class="brand-subtitle">Panel de Control</div>
            </div>
        </div>

        <ul class="nav-menu">
            <li>
                <a href="<%= request.getContextPath() %>/dashboard" class="nav-link active">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
                    <span>Dashboard</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/clientes" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    <span>Clientes</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/mascotas" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="7.5" cy="9" r="1.6"/><circle cx="10.2" cy="5.8" r="1.6"/><circle cx="13.8" cy="5.8" r="1.6"/><circle cx="16.5" cy="9" r="1.6"/><path d="M12 11.2c-2.8 0-5.2 2.3-5.2 5 0 1.6 1.1 2.8 2.6 2.8.9 0 1.6-.5 2.6-.5s1.7.5 2.6.5c1.5 0 2.6-1.2 2.6-2.8 0-2.7-2.4-5-5.2-5z"/></svg>
                    <span>Mascotas</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/citas" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                    <span>Citas</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/servicios" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
                    <span>Servicios</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/productos" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                    <span>Productos</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/pedidos" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="16.5" y1="9.4" x2="7.5" y2="4.21"/><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/><polyline points="3.27 6.96 12 12.01 20.73 6.96"/><line x1="12" y1="22.08" x2="12" y2="12"/></svg>
                    <span>Pedidos</span>
                </a>
            </li>
        </ul>

        <div class="sidebar-footer">
            <button type="button" class="theme-toggle-btn" onclick="toggleTheme()" id="themeToggleBtn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/></svg>
                <span id="themeToggleText">Modo Oscuro</span>
            </button>
            <a href="<%= request.getContextPath() %>/logout" class="nav-link logout" style="margin-top: 8px;">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                <span>Cerrar Sesión</span>
            </a>
        </div>
    </aside>

    <!-- ── Contenido Principal ── -->
    <main class="app-main">

        <!-- Encabezado -->
        <header class="page-header">
            <div>
                <h1 class="page-title">Dashboard</h1>
                <p class="page-desc">Resumen general del sistema PetServices.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <!-- KPIs del sistema -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; margin-bottom: 24px;">
            <a href="<%= request.getContextPath() %>/clientes" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Clientes registrados</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-primary);"><%= totalClientes %></div>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/mascotas" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Mascotas</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-accent);"><%= totalMascotas %></div>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/citas" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Citas agendadas</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-terracotta);"><%= totalCitas %></div>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/servicios" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Servicios en catálogo</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-success);"><%= totalServicios %></div>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/productos" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Productos en inventario</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-primary);"><%= totalProductos %></div>
                </div>
            </a>
            <a href="<%= request.getContextPath() %>/pedidos" style="text-decoration: none; color: inherit;">
                <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                    <div style="font-size: 0.8rem; color: var(--color-text-muted);">Pedidos registrados</div>
                    <div style="font-size: 1.6rem; font-weight: 700; color: var(--color-danger);"><%= totalPedidos %></div>
                </div>
            </a>
        </div>

        <!-- Últimas citas agendadas -->
        <section class="card">
            <div class="card-header">
                <h2 class="card-title">Últimas citas agendadas</h2>
                <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary btn-sm">Ver todas →</a>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>#ID</th>
                            <th>Cliente</th>
                            <th>Mascota</th>
                            <th>Servicio</th>
                            <th>Fecha y hora</th>
                            <th>Estado</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (ultimasCitas == null || ultimasCitas.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="6" style="text-align: center; color: var(--color-text-muted); padding: 20px;">No hay citas agendadas aún.</td>
                        </tr>
                    <%
                        } else {
                            for (Cita c : ultimasCitas) {
                                String estadoCita = (c.getEstado() != null) ? c.getEstado() : "pendiente";
                                String textoEstado = estadoCita.substring(0, 1).toUpperCase() + estadoCita.substring(1);
                                String horaCita = (c.getHora() != null && c.getHora().length() >= 5)
                                        ? c.getHora().substring(0, 5) : "—";
                    %>
                        <tr>
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= c.getIdCita() %></span></td>
                            <td><strong><%= esc(c.getNombreCliente()) %></strong></td>
                            <td><%= esc(c.getNombreMascota()) %></td>
                            <td><%= esc(c.getServicio()) %></td>
                            <td><%= esc(c.getFecha()) %> · <%= horaCita %></td>
                            <td><span class="badge <%= badgeEstadoClass(c.getEstado()) %>"><%= esc(textoEstado) %></span></td>
                        </tr>
                    <%
                            }
                        }
                    %>
                    </tbody>
                </table>
            </div>
        </section>

    </main>
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
