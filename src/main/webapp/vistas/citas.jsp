<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Mascota" %>
<%@ page import="com.petservices.modelo.Cita" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Citas — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

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

    <%@ include file="/WEB-INF/jsp/sidebar.jspf" %>

    <main class="app-main">

        <header class="page-header">
            <div class="page-head-main">
                <button type="button" class="menu-btn" onclick="toggleSidebar()" aria-label="Abrir menú de navegación">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" aria-hidden="true"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
                    Menú
                </button>
                <div>
                    <h1 class="page-title">Agenda de citas</h1>
                    <p class="page-desc">Consultas, vacunación, cirugías y estética: programación y seguimiento.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <!-- Estados de la agenda -->
        <section class="kpi-band" aria-label="Estados de la agenda">
            <div class="kpi">
                <span class="kpi-label">Pendientes</span>
                <span class="kpi-figure"><%= pendientes %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Confirmadas</span>
                <span class="kpi-figure"><%= confirmadas %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Realizadas</span>
                <span class="kpi-figure"><%= realizadas %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Canceladas</span>
                <span class="kpi-figure"><%= canceladas %></span>
            </div>
        </section>

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
                    <%= editando ? "Modificar cita" : "Agendar nueva cita" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/citas" method="post" onsubmit="return validarCitaForm(this)">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= citaEditar.getIdCita() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="idCliente">Cliente / propietario</label>
                            <select class="form-control" id="idCliente" name="idCliente" required onchange="filtrarMascotasForm()">
                                <option value="">— Seleccionar cliente —</option>
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
                            <label class="form-label" for="idMascota">Mascota (paciente)</label>
                            <select class="form-control" id="idMascota" name="idMascota" required onchange="marcarDuenoMascota()">
                                <option value="">— Seleccionar mascota —</option>
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
                            <label class="form-label" for="fechaHora">Fecha y hora</label>
                            <input class="form-control" type="datetime-local" id="fechaHora" name="fechaHora"
                                   value="<%= editando && citaEditar.getFechaHora() != null ? citaEditar.getFechaHora() : "" %>"
                                   required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="servicio">Tipo de servicio</label>
                            <% String srv = editando ? citaEditar.getServicio() : ""; %>
                            <select class="form-control" id="servicio" name="servicio" required>
                                <option value="">— Seleccionar servicio —</option>
                                <option value="Consulta"         <%= "Consulta".equalsIgnoreCase(srv) ? "selected" : "" %>>Consulta</option>
                                <option value="Vacunación"       <%= "Vacunación".equalsIgnoreCase(srv) ? "selected" : "" %>>Vacunación</option>
                                <option value="Baño/Peluquería"  <%= "Baño/Peluquería".equalsIgnoreCase(srv) ? "selected" : "" %>>Baño/Peluquería</option>
                                <option value="Cirugía"          <%= "Cirugía".equalsIgnoreCase(srv) ? "selected" : "" %>>Cirugía</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="estado">Estado de la cita</label>
                            <% String est = editando ? citaEditar.getEstado() : "Pendiente"; %>
                            <select class="form-control" id="estado" name="estado" required>
                                <option value="Pendiente"  <%= "Pendiente".equalsIgnoreCase(est) ? "selected" : "" %>>Pendiente</option>
                                <option value="Confirmada" <%= "Confirmada".equalsIgnoreCase(est) ? "selected" : "" %>>Confirmada</option>
                                <option value="Cancelada"  <%= "Cancelada".equalsIgnoreCase(est) ? "selected" : "" %>>Cancelada</option>
                            </select>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Agendar cita" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary">
                                Cancelar
                            </a>
                        <% } %>
                    </div>
                </form>
            </div>
        </section>

        <!-- Listado de citas -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Citas registradas</h2>
                    <%
                        int totalCitas = (listaCitas != null) ? listaCitas.size() : 0;
                    %>
                    <span class="card-sub" id="badgeContadorCitas"><%= totalCitas %> citas</span>
                </div>

                <div class="table-tools">
                    <label class="sr-only" for="filtroEstadoCita">Filtrar por estado</label>
                    <select id="filtroEstadoCita" class="form-control compact" onchange="filtrarTablaCitas()">
                        <option value="">Todos los estados</option>
                        <option value="pendiente">Pendientes</option>
                        <option value="confirmada">Confirmadas</option>
                        <option value="realizada">Realizadas</option>
                        <option value="cancelada">Canceladas</option>
                    </select>

                    <label class="sr-only" for="buscarCitaInput">Buscar cliente, mascota o servicio</label>
                    <input type="text" id="buscarCitaInput" class="form-control"
                           placeholder="Buscar cliente, mascota…" oninput="filtrarTablaCitas()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th class="num">ID</th>
                            <th>Cliente</th>
                            <th>Mascota</th>
                            <th>Fecha y hora</th>
                            <th>Servicio</th>
                            <th>Estado</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaCitas == null || listaCitas.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" class="empty-cell">No hay citas registradas actualmente.</td>
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
                            <td class="num"><span class="badge badge-id">#<%= c.getIdCita() %></span></td>
                            <td class="row-name"><%= esc(c.getNombreCliente()) %></td>
                            <td><%= esc(c.getNombreMascota()) %></td>
                            <td><%= esc(c.getFechaHora() != null ? c.getFechaHora().replace("T", " ") : "—") %></td>
                            <td><span class="badge badge-plain"><%= esc(c.getServicio()) %></span></td>
                            <td>
                                <span class="badge <%= badgeEstadoClass(c.getEstado()) %>">
                                    <%= esc(c.getEstado()) %>
                                </span>
                            </td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/citas?accion=editar&id=<%= c.getIdCita() %>"
                                       class="btn btn-secondary btn-sm">
                                        Editar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-id="<%= c.getIdCita() %>"
                                            data-nombre="<%= esc(c.getNombreMascota()) %>"
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
        String  modalTitulo   = "Confirmar cancelación";
        String  modalTexto    = "¿Estás seguro de que deseas cancelar esta cita?";
        String  modalBoton    = "Cancelar cita";
        String  modalCancelar = "Volver";
        String  modalAccion   = request.getContextPath() + "/citas";
        boolean modalEnlace   = false;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
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
                f.classList.remove('is-hidden');
                visibles++;
            } else {
                f.classList.add('is-hidden');
            }
        });

        var badge = document.getElementById('badgeContadorCitas');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' citas';
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

    function confirmarEliminar(id, mascota) {
        var campoId = document.getElementById('confirmarAccionId');
        if (campoId) campoId.value = id;
        abrirModalConfirmacion('¿Deseas cancelar y eliminar definitivamente la cita para ' + mascota + '?');
    }
</script>

</body>
</html>
