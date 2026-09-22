<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Clientes — PetServices</title>
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
    Cliente usuarioSesion = (Cliente) session.getAttribute("clienteSesion");
    String  nombreSesion  = (String)  session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
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
                <a href="<%= request.getContextPath() %>/clientes" class="nav-link active">
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
                <h1 class="page-title">Gestión de Clientes</h1>
                <p class="page-desc">Administra el registro, consulta y modificación de propietarios de mascotas.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= nombreSesion %></span>
            </div>
        </header>

        <%
            String mensaje     = (String) request.getAttribute("mensaje");
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
        <%  } %>

        <%
            Cliente clienteEditar = (Cliente) request.getAttribute("clienteEditar");
            boolean editando = (clienteEditar != null);
        %>

        <!-- Formulario Crear / Editar -->
        <section class="card">
            <div class="card-header">
                <h2 class="card-title">
                    <%= editando ? "Modificar Información del Cliente" : "Registrar Nuevo Cliente" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/clientes" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <form action="<%= request.getContextPath() %>/clientes" method="post" onsubmit="return validarClienteForm(this)">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= clienteEditar.getIdCliente() %>">
                <% } %>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre Completo</label>
                        <input class="form-control" type="text" id="nombre" name="nombre"
                               value="<%= editando ? clienteEditar.getNombre() : "" %>"
                               placeholder="Ej: Laura Martínez" required maxlength="100" minlength="3">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="correo">Correo Electrónico</label>
                        <input class="form-control" type="email" id="correo" name="correo"
                               value="<%= editando ? clienteEditar.getCorreo() : "" %>"
                               placeholder="correo@ejemplo.com" required maxlength="100">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="contrasena">
                            <%= editando ? "Nueva Contraseña (dejar vacío para no cambiar)" : "Contraseña de Acceso" %>
                        </label>
                        <input class="form-control" type="password" id="contrasena" name="contrasena"
                               placeholder="••••••••" <%= editando ? "" : "required" %> minlength="6">
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">
                        <%= editando ? "Guardar Cambios" : "Registrar Cliente" %>
                    </button>
                    <% if (editando) { %>
                        <a href="<%= request.getContextPath() %>/clientes" class="btn btn-secondary">
                            Cancelar
                        </a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Tabla de Clientes -->
        <section class="card">
            <div class="card-header" style="flex-wrap: wrap; gap: 12px;">
                <div>
                    <h2 class="card-title">Listado de Propietarios</h2>
                    <%
                        List<Cliente> listaClientes = (List<Cliente>) request.getAttribute("listaClientes");
                        int total = (listaClientes != null) ? listaClientes.size() : 0;
                    %>
                    <span class="badge badge-success" id="badgeContador"><%= total %> Registrados</span>
                </div>

                <div style="min-width: 260px;">
                    <input type="text" id="buscarClienteInput" class="form-control"
                           placeholder="🔍 Buscar por nombre o correo..." oninput="filtrarClientes()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table" id="tablaClientes">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre Completo</th>
                            <th>Correo Electrónico</th>
                            <th>Fecha de Registro</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaClientes == null || listaClientes.isEmpty()) {
                    %>
                        <tr id="filaVacia">
                            <td colspan="5" style="text-align: center; color: var(--color-text-muted); padding: 32px;">
                                No hay clientes registrados actualmente.
                            </td>
                        </tr>
                    <%
                        } else {
                            for (Cliente c : listaClientes) {
                    %>
                        <tr class="fila-cliente" data-nombre="<%= c.getNombre().toLowerCase() %>" data-correo="<%= c.getCorreo().toLowerCase() %>">
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= c.getIdCliente() %></span></td>
                            <td><strong><%= c.getNombre() %></strong></td>
                            <td><%= c.getCorreo() %></td>
                            <td><%= c.getFechaRegistro() != null ? c.getFechaRegistro() : "—" %></td>
                            <td style="text-align: right;">
                                <a href="<%= request.getContextPath() %>/clientes?accion=editar&id=<%= c.getIdCliente() %>"
                                   class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                    Editar
                                </a>
                                <button class="btn btn-danger btn-sm"
                                        onclick="confirmarEliminar(<%= c.getIdCliente() %>, '<%= c.getNombre().replace("'", "\\'") %>')">
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
                ¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.
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

    function filtrarClientes() {
        var filtro = document.getElementById('buscarClienteInput').value.toLowerCase().trim();
        var filas = document.querySelectorAll('.fila-cliente');
        var visibles = 0;

        filas.forEach(function(fila) {
            var nombre = fila.getAttribute('data-nombre') || '';
            var correo = fila.getAttribute('data-correo') || '';
            if (nombre.indexOf(filtro) > -1 || correo.indexOf(filtro) > -1) {
                fila.style.display = '';
                visibles++;
            } else {
                fila.style.display = 'none';
            }
        });

        var badge = document.getElementById('badgeContador');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' encontrados';
        }
    }

    function validarClienteForm(form) {
        var nombre = form.nombre.value.trim();
        var correo = form.correo.value.trim();
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (nombre.length < 3) {
            alert('El nombre debe tener al menos 3 caracteres.');
            form.nombre.focus();
            return false;
        }

        if (!emailRegex.test(correo)) {
            alert('Por favor introduce un correo electrónico válido con formato nombre@dominio.com');
            form.correo.focus();
            return false;
        }

        if (form.contrasena && form.contrasena.value) {
            if (form.contrasena.value.length < 6) {
                alert('La contraseña debe tener al menos 6 caracteres.');
                form.contrasena.focus();
                return false;
            }
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

    function confirmarEliminar(id, nombre) {
        var url = '<%= request.getContextPath() %>/clientes?accion=eliminar&id=' + id;
        abrirModalConfirmacion(url, '¿Estás seguro de que deseas eliminar al cliente "' + nombre + '"? Esta acción cancelará sus registros asociados.');
    }
</script>

</body>
</html>
