<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Clientes — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    Cliente usuarioSesion = (Cliente) session.getAttribute("clienteSesion");
    String  nombreSesion  = (String)  session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());
%>

<div class="app-layout">

    <%@ include file="/WEB-INF/jsp/sidebar.jspf" %>

    <main class="app-main">

        <header class="page-header">
            <div class="page-head-main">
                <button type="button" class="menu-btn" onclick="toggleSidebar()" aria-label="Abrir menú de navegación">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
                    Menú
                </button>
                <div>
                    <h1 class="page-title">Clientes</h1>
                    <p class="page-desc">Registro, consulta y modificación de propietarios de mascotas.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <%
            String mensaje     = (String) request.getAttribute("mensaje");
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
        <%  } %>

        <%
            Cliente clienteEditar = (Cliente) request.getAttribute("clienteEditar");
            boolean editando = (clienteEditar != null);
        %>

        <!-- Formulario crear / editar -->
        <section class="card">
            <div class="card-header">
                <h2 class="card-title">
                    <%= editando ? "Modificar información del cliente" : "Registrar nuevo cliente" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/clientes" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/clientes" method="post" onsubmit="return validarClienteForm(this)">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= clienteEditar.getIdCliente() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre completo</label>
                            <input class="form-control" type="text" id="nombre" name="nombre"
                                   value="<%= editando ? esc(clienteEditar.getNombre()) : "" %>"
                                   placeholder="Ej: Laura Martínez" required maxlength="100" minlength="3">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="correo">Correo electrónico</label>
                            <input class="form-control" type="email" id="correo" name="correo"
                                   value="<%= editando ? esc(clienteEditar.getCorreo()) : "" %>"
                                   placeholder="correo@ejemplo.com" required maxlength="100">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="contrasena">
                                <%= editando ? "Nueva contraseña (dejar vacío para no cambiar)" : "Contraseña de acceso" %>
                            </label>
                            <input class="form-control" type="password" id="contrasena" name="contrasena"
                                   placeholder="••••••••" <%= editando ? "" : "required" %> minlength="6">
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Registrar cliente" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/clientes" class="btn btn-secondary">
                                Cancelar
                            </a>
                        <% } %>
                    </div>
                </form>
            </div>
        </section>

        <!-- Listado -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Listado de propietarios</h2>
                    <%
                        List<Cliente> listaClientes = (List<Cliente>) request.getAttribute("listaClientes");
                        int total = (listaClientes != null) ? listaClientes.size() : 0;
                    %>
                    <span class="card-sub" id="badgeContador"><%= total %> registrados</span>
                </div>

                <div class="table-tools">
                    <label class="sr-only" for="buscarClienteInput">Buscar por nombre o correo</label>
                    <input type="text" id="buscarClienteInput" class="form-control"
                           placeholder="Buscar por nombre o correo…" oninput="filtrarClientes()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table" id="tablaClientes">
                    <thead>
                        <tr>
                            <th class="num">ID</th>
                            <th>Nombre completo</th>
                            <th>Correo electrónico</th>
                            <th>Fecha de registro</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaClientes == null || listaClientes.isEmpty()) {
                    %>
                        <tr id="filaVacia">
                            <td colspan="5" class="empty-cell">No hay clientes registrados actualmente.</td>
                        </tr>
                    <%
                        } else {
                            for (Cliente c : listaClientes) {
                    %>
                        <tr class="fila-cliente" data-nombre="<%= esc(c.getNombre()).toLowerCase() %>" data-correo="<%= esc(c.getCorreo()).toLowerCase() %>">
                            <td class="num"><span class="badge badge-id">#<%= c.getIdCliente() %></span></td>
                            <td class="row-name"><%= esc(c.getNombre()) %></td>
                            <td class="row-muted"><%= esc(c.getCorreo()) %></td>
                            <td><%= esc(c.getFechaRegistro() != null ? c.getFechaRegistro() : "—") %></td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/clientes?accion=editar&id=<%= c.getIdCliente() %>"
                                       class="btn btn-secondary btn-sm">
                                        Editar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-id="<%= c.getIdCliente() %>"
                                            data-nombre="<%= esc(c.getNombre()) %>"
                                            onclick="confirmarEliminarDesdeBoton(this)">
                                        Eliminar
                                    </button>
                                </span>
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

    <%
        // ── Modal compartido (WEB-INF/jsp/modal-confirmacion.jspf) ──
        // Este módulo elimina por enlace GET, por eso modalEnlace = true.
        String  modalTitulo   = "Confirmar eliminación";
        String  modalTexto    = "¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.";
        String  modalBoton    = "Eliminar";
        String  modalCancelar = "Cancelar";
        String  modalAccion   = request.getContextPath() + "/clientes";
        boolean modalEnlace   = true;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
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

        var contador = document.getElementById('badgeContador');
        if (contador) {
            contador.textContent = visibles + ' de ' + filas.length + ' encontrados';
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

    function confirmarEliminar(id, nombre) {
        var url = '<%= request.getContextPath() %>/clientes?accion=eliminar&id=' + id;
        abrirModalConfirmacion('¿Estás seguro de que deseas eliminar al cliente "' + nombre + '"? Esta acción cancelará sus registros asociados.', url);
    }
</script>

</body>
</html>
