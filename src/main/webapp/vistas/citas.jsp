<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Mascota" %>
<%@ page import="com.petservices.modelo.Cita" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String badgeEstadoClass(String estado) {
        String val = (estado == null) ? "" : estado.trim().toLowerCase();
        if ("confirmada".equals(val)) return "badge-success";
        if ("cancelada".equals(val)) return "badge-danger";
        if ("realizada".equals(val)) return "badge-success";
        return "badge-warning";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Citas — PetServices</title>
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

    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");

    Cita citaEditar = (Cita) request.getAttribute("citaEditar");
    boolean editando = (citaEditar != null);

    List<Cita> listaCitas        = (List<Cita>) request.getAttribute("listaCitas");
    List<Cliente> listaClientes  = (List<Cliente>) request.getAttribute("listaClientes");
    List<Mascota> listaMascotas  = (List<Mascota>) request.getAttribute("listaMascotas");

    int pendientes = 0, confirmadas = 0, realizadas = 0, canceladas = 0;
    if (listaCitas != null) {
        for (Cita c : listaCitas) {
            String est = (c.getEstado() == null) ? "" : c.getEstado().toLowerCase();
            if (est.contains("pend")) pendientes++;
            else if (est.contains("conf")) confirmadas++;
            else if (est.contains("real")) realizadas++;
            else if (est.contains("canc")) canceladas++;
        }
    }
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
                <a href="<%= request.getContextPath() %>/dashboard" class="nav-link">
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
                <a href="<%= request.getContextPath() %>/citas" class="nav-link active">
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
                <h1 class="page-title">Agenda de Citas</h1>
                <p class="page-desc">Programa y gestiona consultas veterinarias, vacunación, cirugías y estética.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <!-- Métricas Rápidas de Agenda -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 14px; margin-bottom: 24px;">
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Pendientes</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-warning);"><%= pendientes %></div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Confirmadas</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-primary);"><%= confirmadas %></div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Realizadas</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-success);"><%= realizadas %></div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Canceladas</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-danger);"><%= canceladas %></div>
            </div>
        </div>

        <% if (mensaje != null && !mensaje.isEmpty()) {
            String claseAlerta = "alert-error";
            if ("exito".equalsIgnoreCase(tipoMensaje)) claseAlerta = "alert-success";
        %>
            <div class="alert <%= claseAlerta %>">
                <span><%= mensaje %></span>
            </div>
        <% } %>

        <!-- Formulario Crear / Editar -->
        <section class="card">
            <div class="card-header">
                <h2 class="card-title">
                    <%= editando ? "Modificar Cita" : "Agendar Nueva Cita" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <form action="<%= request.getContextPath() %>/citas" method="post" onsubmit="return validarCitaForm(this)">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= citaEditar.getIdCita() %>">
                <% } %>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="idCliente">Cliente / Propietario</label>
                        <select class="form-control" id="idCliente" name="idCliente" required onchange="filtrarMascotasForm()">
                            <option value="">— Seleccionar Cliente —</option>
                            <%
                                if (listaClientes != null) {
                                    for (Cliente c : listaClientes) {
                                        boolean seleccionado = editando && citaEditar.getIdCliente() == c.getIdCliente();
                            %>
                                <option value="<%= c.getIdCliente() %>" <%= seleccionado ? "selected" : "" %>>
                                    <%= esc(c.getNombre()) %>
                                </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="idMascota">Mascota (Paciente)</label>
                        <select class="form-control" id="idMascota" name="idMascota" required onchange="marcarDuenoMascota()">
                            <option value="">— Seleccionar Mascota —</option>
                            <%
                                if (listaMascotas != null) {
                                    for (Mascota m : listaMascotas) {
                                        boolean seleccionado = editando && citaEditar.getIdMascota() == m.getIdMascota();
                            %>
                                <option value="<%= m.getIdMascota() %>"
                                        data-cliente="<%= m.getIdCliente() %>"
                                        <%= seleccionado ? "selected" : "" %>>
                                    <%= esc(m.getNombre()) %> (<%= esc(m.getEspecie()) %>)
                                </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="fechaHora">Fecha y Hora</label>
                        <input class="form-control" type="datetime-local" id="fechaHora" name="fechaHora"
                               value="<%= editando && citaEditar.getFechaHora() != null ? citaEditar.getFechaHora() : "" %>"
                               required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="servicio">Tipo de Servicio</label>
                        <% String srv = editando ? citaEditar.getServicio() : ""; %>
                        <select class="form-control" id="servicio" name="servicio" required>
                            <option value="">— Seleccionar Servicio —</option>
                            <option value="Consulta General"  <%= "Consulta General".equalsIgnoreCase(srv) ? "selected" : "" %>>Consulta General</option>
                            <option value="Vacunación"        <%= "Vacunación".equalsIgnoreCase(srv) ? "selected" : "" %>>Vacunación</option>
                            <option value="Desparasitación"   <%= "Desparasitación".equalsIgnoreCase(srv) ? "selected" : "" %>>Desparasitación</option>
                            <option value="Baño y Peluquería" <%= "Baño y Peluquería".equalsIgnoreCase(srv) ? "selected" : "" %>>Baño y Peluquería</option>
                            <option value="Cirugía Menor"     <%= "Cirugía Menor".equalsIgnoreCase(srv) ? "selected" : "" %>>Cirugía Menor</option>
                            <option value="Urgencia"          <%= "Urgencia".equalsIgnoreCase(srv) ? "selected" : "" %>>Urgencia</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="estado">Estado de la Cita</label>
                        <% String est = editando ? citaEditar.getEstado() : "Pendiente"; %>
                        <select class="form-control" id="estado" name="estado" required>
                            <option value="Pendiente"  <%= "Pendiente".equalsIgnoreCase(est) ? "selected" : "" %>>Pendiente</option>
                            <option value="Confirmada" <%= "Confirmada".equalsIgnoreCase(est) ? "selected" : "" %>>Confirmada</option>
                            <option value="Realizada"  <%= "Realizada".equalsIgnoreCase(est) ? "selected" : "" %>>Realizada</option>
                            <option value="Cancelada"  <%= "Cancelada".equalsIgnoreCase(est) ? "selected" : "" %>>Cancelada</option>
                        </select>
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">
                        <%= editando ? "Guardar Cambios" : "Agendar Cita" %>
                    </button>
                    <% if (editando) { %>
                        <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary">
                            Cancelar
                        </a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Tabla de Citas -->
        <section class="card">
            <div class="card-header" style="flex-wrap: wrap; gap: 12px;">
                <div>
                    <h2 class="card-title">Citas Registradas</h2>
                    <%
                        int totalCitas = (listaCitas != null) ? listaCitas.size() : 0;
                    %>
                    <span class="badge badge-success" id="badgeContadorCitas"><%= totalCitas %> Citas</span>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <select id="filtroEstadoCita" class="form-control" style="width: auto;" onchange="filtrarTablaCitas()">
                        <option value="">Todos los Estados</option>
                        <option value="pendiente">Pendientes</option>
                        <option value="confirmada">Confirmadas</option>
                        <option value="realizada">Realizadas</option>
                        <option value="cancelada">Canceladas</option>
                    </select>

                    <input type="text" id="buscarCitaInput" class="form-control" style="width: 200px;"
                           placeholder="🔍 Buscar cliente, mascota..." oninput="filtrarTablaCitas()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Cliente</th>
                            <th>Mascota</th>
                            <th>Fecha y Hora</th>
                            <th>Servicio</th>
                            <th>Estado</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaCitas == null || listaCitas.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 32px;">
                                No hay citas registradas actualmente.
                            </td>
                        </tr>
                    <%
                        } else {
                            for (Cita c : listaCitas) {
                    %>
                        <tr class="fila-cita"
                            data-cliente="<%= esc(c.getNombreCliente()).toLowerCase() %>"
                            data-mascota="<%= esc(c.getNombreMascota()).toLowerCase() %>"
                            data-servicio="<%= esc(c.getServicio()).toLowerCase() %>"
                            data-estado="<%= esc(c.getEstado()).toLowerCase() %>">
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= c.getIdCita() %></span></td>
                            <td><strong><%= esc(c.getNombreCliente()) %></strong></td>
                            <td>🐾 <%= esc(c.getNombreMascota()) %></td>
                            <td><%= esc(c.getFechaHora() != null ? c.getFechaHora().replace("T", " ") : "—") %></td>
                            <td><span class="badge" style="background: var(--color-surface-tint); color: var(--color-text); border: 1px solid var(--color-border);"><%= esc(c.getServicio()) %></span></td>
                            <td>
                                <span class="badge <%= badgeEstadoClass(c.getEstado()) %>">
                                    <%= esc(c.getEstado()) %>
                                </span>
                            </td>
                            <td style="text-align: right;">
                                <a href="<%= request.getContextPath() %>/citas?accion=editar&id=<%= c.getIdCita() %>"
                                   class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                    Editar
                                </a>
                                <button class="btn btn-danger btn-sm"
                                        onclick="confirmarEliminar(<%= c.getIdCita() %>, '<%= esc(c.getNombreMascota()).replace("'", "\\'") %>')">
                                    Eliminar
                                </button>
                            </td>
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

    <!-- Modal Accesible de Confirmación -->
    <div id="modalConfirmacion" class="app-modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="modalTitulo" aria-describedby="modalDescripcion">
        <div class="app-modal-card">
            <div class="app-modal-header">
                <div class="app-modal-icon danger" aria-hidden="true">
                    <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                </div>
                <h3 id="modalTitulo" class="app-modal-title">Confirmar Cancelación</h3>
            </div>
            <div id="modalDescripcion" class="app-modal-body">
                ¿Estás seguro de que deseas cancelar esta cita?
            </div>
            <div class="app-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="cerrarModalConfirmacion()">Volver</button>
                <a id="btnConfirmarAccion" href="#" class="btn btn-danger">Cancelar Cita</a>
            </div>
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

    function filtrarTablaCitas() {
        var texto = document.getElementById('buscarCitaInput').value.toLowerCase().trim();
        var estado = document.getElementById('filtroEstadoCita').value.toLowerCase().trim();
        var filas = document.querySelectorAll('.fila-cita');
        var visibles = 0;

        filas.forEach(function(f) {
            var c = f.getAttribute('data-cliente') || '';
            var m = f.getAttribute('data-mascota') || '';
            var s = f.getAttribute('data-servicio') || '';
            var e = f.getAttribute('data-estado') || '';

            var matchTexto = (c.indexOf(texto) > -1 || m.indexOf(texto) > -1 || s.indexOf(texto) > -1);
            var matchEstado = (!estado || e.indexOf(estado) > -1);

            if (matchTexto && matchEstado) {
                f.style.display = '';
                visibles++;
            } else {
                f.style.display = 'none';
            }
        });

        var badge = document.getElementById('badgeContadorCitas');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' Citas';
        }
    }

    function filtrarMascotasForm() {
        var clienteSelect = document.getElementById('idCliente');
        var mascotaSelect = document.getElementById('idMascota');
        var clienteId = clienteSelect.value;

        for (var i = 1; i < mascotaSelect.options.length; i++) {
            var opt = mascotaSelect.options[i];
            if (!clienteId || opt.getAttribute('data-cliente') === clienteId) {
                opt.style.display = '';
            } else {
                opt.style.display = 'none';
            }
        }
    }

    function marcarDuenoMascota() {
        var mascotaSelect = document.getElementById('idMascota');
        var clienteSelect = document.getElementById('idCliente');
        var opt = mascotaSelect.options[mascotaSelect.selectedIndex];
        if (opt && opt.getAttribute('data-cliente')) {
            clienteSelect.value = opt.getAttribute('data-cliente');
        }
    }

    function validarCitaForm(form) {
        var cliente = form.idCliente.value;
        var mascota = form.idMascota.value;
        var fechaHoraStr = form.fechaHora.value;
        var servicio = form.servicio.value;

        if (!cliente) {
            alert('Por favor selecciona un cliente propietario.');
            form.idCliente.focus();
            return false;
        }

        if (!mascota) {
            alert('Por favor selecciona la mascota que recibirá atención.');
            form.idMascota.focus();
            return false;
        }

        if (!fechaHoraStr) {
            alert('Por favor especifica la fecha y hora de la cita.');
            form.fechaHora.focus();
            return false;
        }

        var fechaSeleccionada = new Date(fechaHoraStr);
        var ahora = new Date();
        // Permitir un margen de 5 minutos
        if (fechaSeleccionada.getTime() < (ahora.getTime() - 5 * 60 * 1000) && !form.id) {
            if (!confirm('La fecha y hora elegida ya transcurrió en el pasado. ¿Deseas agendarla de todas formas?')) {
                form.fechaHora.focus();
                return false;
            }
        }

        if (!servicio) {
            alert('Por favor selecciona el tipo de servicio veterinario.');
            form.servicio.focus();
            return false;
        }

        return true;
    }

    function abrirModalConfirmacion(url, mensaje) {
        var modal = document.getElementById('modalConfirmacion');
        var desc = document.getElementById('modalDescripcion');
        var btn = document.getElementById('btnConfirmarAccion');
        if (desc) desc.textContent = mensaje;
        if (btn) btn.href = url;
        if (modal) {
            modal.classList.add('is-active');
            var cancelBtn = modal.querySelector('.btn-secondary');
            if (cancelBtn) cancelBtn.focus();
        }
    }

    function cerrarModalConfirmacion() {
        var modal = document.getElementById('modalConfirmacion');
        if (modal) modal.classList.remove('is-active');
    }

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            cerrarModalConfirmacion();
        }
    });

    function confirmarEliminar(id, mascota) {
        var url = '<%= request.getContextPath() %>/citas?accion=eliminar&id=' + id;
        abrirModalConfirmacion(url, '¿Deseas cancelar y eliminar definitivamente la cita para ' + mascota + '?');
    }
</script>

</body>
</html>
