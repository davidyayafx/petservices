<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Servicio" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Servicios — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");

    Servicio servicioEditar = (Servicio) request.getAttribute("servicioEditar");
    boolean editando = (servicioEditar != null);

    List<Servicio> listaServicios = (List<Servicio>) request.getAttribute("listaServicios");
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
                    <h1 class="page-title">Servicios</h1>
                    <p class="page-desc">Tipos de servicio que ofrece la veterinaria.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <% if (mensaje != null && !mensaje.isEmpty()) {
            String claseAlerta = "alert-error";
            if ("exito".equalsIgnoreCase(tipoMensaje)) claseAlerta = "alert-success";
        %>
            <div class="alert <%= claseAlerta %>" role="alert">
                <span><%= esc(mensajePlano(mensaje)) %></span>
            </div>
        <% } %>

        <!-- Formulario crear / editar -->
        <section class="card">
            <div class="card-header">
                <h2 class="card-title">
                    <%= editando ? "Modificar servicio" : "Registrar nuevo servicio" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/servicios" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/servicios" method="post">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= servicioEditar.getIdServicio() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="tipo">Tipo de servicio</label>
                            <% String tipoActual = editando ? servicioEditar.getTipo() : ""; %>
                            <select class="form-control" id="tipo" name="tipo" required>
                                <option value="">— Seleccionar tipo —</option>
                                <%
                                    String[] tipos = {"Consulta", "Baño y Corte", "Cirugía", "Vacunación", "Otro"};
                                    for (String t : tipos) {
                                %>
                                    <option value="<%= t %>" <%= t.equalsIgnoreCase(tipoActual) ? "selected" : "" %>><%= t %></option>
                                <% } %>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre del servicio</label>
                            <input class="form-control" type="text" id="nombre" name="nombre"
                                   value="<%= editando ? esc(servicioEditar.getNombre()) : "" %>"
                                   placeholder="Ej: Consulta general, Baño antipulgas…" required maxlength="100">
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Registrar servicio" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/servicios" class="btn btn-secondary">
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
                    <h2 class="card-title">Catálogo de servicios</h2>
                    <%
                        int total = (listaServicios != null) ? listaServicios.size() : 0;
                    %>
                    <span class="card-sub"><%= total %> en catálogo</span>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th class="num">ID</th>
                            <th>Tipo</th>
                            <th>Nombre</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaServicios == null || listaServicios.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="4" class="empty-cell">No hay servicios registrados en el catálogo.</td>
                        </tr>
                    <%
                        } else {
                            for (Servicio s : listaServicios) {
                    %>
                        <tr>
                            <td class="num"><span class="badge badge-id">#<%= s.getIdServicio() %></span></td>
                            <td><span class="badge badge-plain"><%= esc(s.getTipo()) %></span></td>
                            <td class="row-name"><%= esc(s.getNombre()) %></td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/servicios?accion=editar&id=<%= s.getIdServicio() %>"
                                       class="btn btn-secondary btn-sm">
                                        Editar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-id="<%= s.getIdServicio() %>"
                                            data-nombre="<%= esc(s.getNombre()) %>"
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
        String  modalAccion   = request.getContextPath() + "/servicios";
        boolean modalEnlace   = true;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
    function confirmarEliminar(id, nombre) {
        var url = '<%= request.getContextPath() %>/servicios?accion=eliminar&id=' + id;
        abrirModalConfirmacion('¿Estás seguro de que deseas eliminar el servicio "' + nombre + '" del catálogo de la veterinaria?', url);
    }
</script>

</body>
</html>
