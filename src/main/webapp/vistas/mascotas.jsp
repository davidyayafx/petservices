<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Mascota" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String getEtapaVida(int edad) {
        if (edad <= 1) return "Cachorro / Cría";
        if (edad <= 7) return "Adulto";
        return "Senior";
    }

    private String getIconoEspecie(String esp) {
        String e = (esp == null) ? "" : esp.toLowerCase();
        if (e.contains("perro")) return "🐕";
        if (e.contains("gato")) return "🐈";
        if (e.contains("ave")) return "🦜";
        if (e.contains("conejo")) return "🐇";
        return "🐾";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Mascotas — PetServices</title>
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

    Mascota mascotaEditar = (Mascota) request.getAttribute("mascotaEditar");
    boolean editando = (mascotaEditar != null);

    List<Mascota> listaMascotas = (List<Mascota>) request.getAttribute("listaMascotas");
    List<Cliente> listaClientes = (List<Cliente>) request.getAttribute("listaClientes");
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
                <a href="<%= request.getContextPath() %>/clientes" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                    <span>Clientes</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/mascotas" class="nav-link active">
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
                <h1 class="page-title">Gestión de Mascotas</h1>
                <p class="page-desc">Registra los pacientes animales y asócialos con sus respectivos dueños.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= esc(nombreSesion) %></span>
            </div>
        </header>

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
                    <%= editando ? "Modificar Mascota" : "Registrar Nueva Mascota" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/mascotas" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <form action="<%= request.getContextPath() %>/mascotas" method="post">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= mascotaEditar.getIdMascota() %>">
                <% } %>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre de la Mascota</label>
                        <input class="form-control" type="text" id="nombre" name="nombre"
                               value="<%= editando ? esc(mascotaEditar.getNombre()) : "" %>"
                               placeholder="Ej: Max, Luna, Toby" required maxlength="50">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="especie">Especie</label>
                        <% String esp = editando ? mascotaEditar.getEspecie() : ""; %>
                        <select class="form-control" id="especie" name="especie" required>
                            <option value="">— Seleccionar —</option>
                            <option value="Perro"  <%= "Perro".equalsIgnoreCase(esp) ? "selected" : "" %>>Perro</option>
                            <option value="Gato"   <%= "Gato".equalsIgnoreCase(esp) ? "selected" : "" %>>Gato</option>
                            <option value="Ave"    <%= "Ave".equalsIgnoreCase(esp) ? "selected" : "" %>>Ave</option>
                            <option value="Conejo" <%= "Conejo".equalsIgnoreCase(esp) ? "selected" : "" %>>Conejo</option>
                            <option value="Otro"   <%= "Otro".equalsIgnoreCase(esp) ? "selected" : "" %>>Otro</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="raza">Raza</label>
                        <input class="form-control" type="text" id="raza" name="raza"
                               value="<%= editando ? esc(mascotaEditar.getRaza()) : "" %>"
                               placeholder="Ej: Golden Retriever, Criollo" maxlength="50">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="edad">Edad (Años)</label>
                        <input class="form-control" type="number" id="edad" name="edad" min="0" max="40"
                               value="<%= editando ? mascotaEditar.getEdad() : "" %>"
                               placeholder="Ej: 3" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="idCliente">Propietario / Dueño</label>
                        <select class="form-control" id="idCliente" name="idCliente" required>
                            <option value="">— Seleccionar Propietario —</option>
                            <%
                                if (listaClientes != null) {
                                    for (Cliente c : listaClientes) {
                                        boolean seleccionado = editando && mascotaEditar.getIdCliente() == c.getIdCliente();
                            %>
                                <option value="<%= c.getIdCliente() %>" <%= seleccionado ? "selected" : "" %>>
                                    <%= esc(c.getNombre()) %> (<%= esc(c.getCorreo()) %>)
                                </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">
                        <%= editando ? "Guardar Cambios" : "Registrar Mascota" %>
                    </button>
                    <% if (editando) { %>
                        <a href="<%= request.getContextPath() %>/mascotas" class="btn btn-secondary">
                            Cancelar
                        </a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Tabla y Pasaportes de Mascotas -->
        <section class="card">
            <div class="card-header" style="flex-wrap: wrap; gap: 14px;">
                <div>
                    <h2 class="card-title">Listado de Pacientes</h2>
                    <%
                        int totalMascotas = (listaMascotas != null) ? listaMascotas.size() : 0;
                    %>
                    <span class="badge badge-success" id="badgeContadorMascotas"><%= totalMascotas %> Mascotas</span>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <select id="filtroEspecie" class="form-control" style="width: auto;" onchange="filtrarMascotas()">
                        <option value="">Todas las Especies</option>
                        <option value="Perro">Perros</option>
                        <option value="Gato">Gatos</option>
                        <option value="Ave">Aves</option>
                        <option value="Conejo">Conejos</option>
                        <option value="Otro">Otros</option>
                    </select>

                    <input type="text" id="buscarMascotaInput" class="form-control" style="width: 200px;"
                           placeholder="🔍 Buscar mascota o dueño..." oninput="filtrarMascotas()">

                    <div style="display: flex; border: 1px solid var(--color-border); border-radius: var(--radius-sm); overflow: hidden;">
                        <button type="button" id="btnVistaTabla" class="btn btn-secondary btn-sm" style="border-radius: 0;" onclick="cambiarVista('tabla')">
                            Tabla
                        </button>
                        <button type="button" id="btnVistaPasaporte" class="btn btn-primary btn-sm" style="border-radius: 0;" onclick="cambiarVista('pasaportes')">
                            Pasaportes
                        </button>
                    </div>
                </div>
            </div>

            <!-- Vista Pasaportes (Cards) -->
            <div id="vistaPasaportes" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px; margin-top: 10px;">
                <%
                    if (listaMascotas == null || listaMascotas.isEmpty()) {
                %>
                    <div style="grid-column: 1 / -1; text-align: center; color: var(--color-text-muted); padding: 32px;">
                        No hay mascotas registradas actualmente.
                    </div>
                <%
                    } else {
                        for (Mascota m : listaMascotas) {
                            String nombreDueno = "ID " + m.getIdCliente();
                            if (listaClientes != null) {
                                for (Cliente c : listaClientes) {
                                    if (c.getIdCliente() == m.getIdCliente()) {
                                        nombreDueno = c.getNombre();
                                        break;
                                    }
                                }
                            }
                %>
                    <div class="tarjeta-mascota"
                         data-nombre="<%= esc(m.getNombre()).toLowerCase() %>"
                         data-dueno="<%= esc(nombreDueno).toLowerCase() %>"
                         data-especie="<%= esc(m.getEspecie()).toLowerCase() %>"
                         style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 18px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: var(--shadow-sm);">
                        <div>
                            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                                <div style="display: flex; align-items: center; gap: 8px;">
                                    <span style="font-size: 1.5rem;"><%= getIconoEspecie(m.getEspecie()) %></span>
                                    <div>
                                        <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--color-text); margin: 0;"><%= esc(m.getNombre()) %></h3>
                                        <span style="font-size: 0.8rem; color: var(--color-text-muted);">#<%= m.getIdMascota() %> • <%= esc(m.getEspecie()) %></span>
                                    </div>
                                </div>
                                <span class="badge" style="background: var(--color-primary-light); color: var(--color-primary); font-size: 0.72rem;">
                                    <%= getEtapaVida(m.getEdad()) %>
                                </span>
                            </div>

                            <div style="font-size: 0.85rem; color: var(--color-text-muted); display: flex; flex-direction: column; gap: 6px; margin-bottom: 14px;">
                                <div><strong>Raza:</strong> <%= esc(m.getRaza() != null && !m.getRaza().isEmpty() ? m.getRaza() : "Mestizo / Criollo") %></div>
                                <div><strong>Edad:</strong> <%= m.getEdad() %> años</div>
                                <div><strong>Propietario:</strong> <%= esc(nombreDueno) %></div>
                            </div>
                        </div>

                        <div style="display: flex; gap: 8px; border-top: 1px solid var(--color-border); padding-top: 12px;">
                            <a href="<%= request.getContextPath() %>/mascotas?accion=editar&id=<%= m.getIdMascota() %>"
                               class="btn btn-secondary btn-sm" style="flex: 1; text-align: center; text-decoration: none;">
                                Editar
                            </a>
                            <button class="btn btn-danger btn-sm" style="flex: 1;"
                                    onclick="confirmarEliminar(<%= m.getIdMascota() %>, '<%= esc(m.getNombre()).replace("'", "\\'") %>')">
                                Eliminar
                            </button>
                        </div>
                    </div>
                <%
                        }
                    }
                %>
            </div>

            <!-- Vista Tabla Tradicional -->
            <div class="table-responsive" id="vistaTabla" style="display: none;">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Especie</th>
                            <th>Raza</th>
                            <th>Edad / Etapa</th>
                            <th>Propietario</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaMascotas == null || listaMascotas.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 32px;">
                                No hay mascotas registradas actualmente.
                            </td>
                        </tr>
                    <%
                        } else {
                            for (Mascota m : listaMascotas) {
                                String nombreDueno = "ID " + m.getIdCliente();
                                if (listaClientes != null) {
                                    for (Cliente c : listaClientes) {
                                        if (c.getIdCliente() == m.getIdCliente()) {
                                            nombreDueno = c.getNombre();
                                            break;
                                        }
                                    }
                                }
                    %>
                        <tr class="fila-mascota"
                            data-nombre="<%= esc(m.getNombre()).toLowerCase() %>"
                            data-dueno="<%= esc(nombreDueno).toLowerCase() %>"
                            data-especie="<%= esc(m.getEspecie()).toLowerCase() %>">
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= m.getIdMascota() %></span></td>
                            <td><strong><%= esc(m.getNombre()) %></strong></td>
                            <td><span class="badge" style="background: var(--color-surface-tint); color: var(--color-text); border: 1px solid var(--color-border);"><%= getIconoEspecie(m.getEspecie()) %> <%= esc(m.getEspecie()) %></span></td>
                            <td><%= esc(m.getRaza()) %></td>
                            <td><%= m.getEdad() %> años (<%= getEtapaVida(m.getEdad()) %>)</td>
                            <td><span class="badge" style="background: var(--color-surface-tint); color: var(--color-text-muted); border: 1px solid var(--color-border);"><%= esc(nombreDueno) %></span></td>
                            <td style="text-align: right;">
                                <a href="<%= request.getContextPath() %>/mascotas?accion=editar&id=<%= m.getIdMascota() %>"
                                   class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                    Editar
                                </a>
                                <button class="btn btn-danger btn-sm"
                                        onclick="confirmarEliminar(<%= m.getIdMascota() %>, '<%= esc(m.getNombre()).replace("'", "\\'") %>')">
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
                <h3 id="modalTitulo" class="app-modal-title">Confirmar Eliminación</h3>
            </div>
            <div id="modalDescripcion" class="app-modal-body">
                ¿Estás seguro de que deseas eliminar este registro?
            </div>
            <div class="app-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="cerrarModalConfirmacion()">Cancelar</button>
                <a id="btnConfirmarAccion" href="#" class="btn btn-danger">Eliminar</a>
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

    function cambiarVista(tipo) {
        var vPasaportes = document.getElementById('vistaPasaportes');
        var vTabla = document.getElementById('vistaTabla');
        var bPas = document.getElementById('btnVistaPasaporte');
        var bTab = document.getElementById('btnVistaTabla');

        if (tipo === 'tabla') {
            vPasaportes.style.display = 'none';
            vTabla.style.display = 'block';
            bTab.className = 'btn btn-primary btn-sm';
            bPas.className = 'btn btn-secondary btn-sm';
        } else {
            vPasaportes.style.display = 'grid';
            vTabla.style.display = 'none';
            bPas.className = 'btn btn-primary btn-sm';
            bTab.className = 'btn btn-secondary btn-sm';
        }
    }

    function filtrarMascotas() {
        var texto = document.getElementById('buscarMascotaInput').value.toLowerCase().trim();
        var especie = document.getElementById('filtroEspecie').value.toLowerCase().trim();

        var tarjetas = document.querySelectorAll('.tarjeta-mascota');
        var filas = document.querySelectorAll('.fila-mascota');
        var total = tarjetas.length;
        var visibles = 0;

        tarjetas.forEach(function(card) {
            var n = card.getAttribute('data-nombre') || '';
            var d = card.getAttribute('data-dueno') || '';
            var e = card.getAttribute('data-especie') || '';

            var matchTexto = (n.indexOf(texto) > -1 || d.indexOf(texto) > -1);
            var matchEspecie = (!especie || e.indexOf(especie) > -1);

            if (matchTexto && matchEspecie) {
                card.style.display = 'flex';
                visibles++;
            } else {
                card.style.display = 'none';
            }
        });

        filas.forEach(function(fila) {
            var n = fila.getAttribute('data-nombre') || '';
            var d = fila.getAttribute('data-dueno') || '';
            var e = fila.getAttribute('data-especie') || '';

            var matchTexto = (n.indexOf(texto) > -1 || d.indexOf(texto) > -1);
            var matchEspecie = (!especie || e.indexOf(especie) > -1);

            if (matchTexto && matchEspecie) {
                fila.style.display = '';
            } else {
                fila.style.display = 'none';
            }
        });

        var badge = document.getElementById('badgeContadorMascotas');
        if (badge) {
            badge.textContent = visibles + ' de ' + total + ' Mascotas';
        }
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

    function confirmarEliminar(id, nombre) {
        var url = '<%= request.getContextPath() %>/mascotas?accion=eliminar&id=' + id;
        abrirModalConfirmacion(url, '¿Estás seguro de que deseas eliminar el pasaporte de la mascota "' + nombre + '"?');
    }
</script>

</body>
</html>
