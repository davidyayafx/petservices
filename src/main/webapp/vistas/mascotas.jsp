<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Mascota" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<%!
    private String getEtapaVida(int edad) {
        if (edad <= 1) return "Cachorro";
        if (edad <= 7) return "Adulto";
        return "Senior";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Mascotas — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");

    Mascota mascotaEditar = (Mascota) request.getAttribute("mascotaEditar");
    boolean editando = (mascotaEditar != null);

    List<Mascota> listaMascotas = (List<Mascota>) request.getAttribute("listaMascotas");
    List<Cliente> listaClientes = (List<Cliente>) request.getAttribute("listaClientes");
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
                    <h1 class="page-title">Mascotas</h1>
                    <p class="page-desc">Pacientes animales y sus respectivos dueños, con pasaporte clínico.</p>
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
                    <%= editando ? "Modificar mascota" : "Registrar nueva mascota" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/mascotas" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/mascotas" method="post">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= mascotaEditar.getIdMascota() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre de la mascota</label>
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
                            <label class="form-label" for="edad">Edad (años)</label>
                            <input class="form-control" type="number" id="edad" name="edad" min="0" max="40"
                                   value="<%= editando ? mascotaEditar.getEdad() : "" %>"
                                   placeholder="Ej: 3" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="idCliente">Propietario / dueño</label>
                            <select class="form-control" id="idCliente" name="idCliente" required>
                                <option value="">— Seleccionar propietario —</option>
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

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Registrar mascota" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/mascotas" class="btn btn-secondary">
                                Cancelar
                            </a>
                        <% } %>
                    </div>
                </form>
            </div>
        </section>

        <!-- Listado: pasaportes o tabla -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Listado de pacientes</h2>
                    <%
                        int totalMascotas = (listaMascotas != null) ? listaMascotas.size() : 0;
                    %>
                    <span class="card-sub" id="badgeContadorMascotas"><%= totalMascotas %> mascotas</span>
                </div>

                <div class="table-tools">
                    <label class="sr-only" for="filtroEspecie">Filtrar por especie</label>
                    <select id="filtroEspecie" class="form-control compact" onchange="filtrarMascotas()">
                        <option value="">Todas las especies</option>
                        <option value="Perro">Perros</option>
                        <option value="Gato">Gatos</option>
                        <option value="Ave">Aves</option>
                        <option value="Conejo">Conejos</option>
                        <option value="Otro">Otros</option>
                    </select>

                    <label class="sr-only" for="buscarMascotaInput">Buscar mascota o dueño</label>
                    <input type="text" id="buscarMascotaInput" class="form-control"
                           placeholder="Buscar mascota o dueño…" oninput="filtrarMascotas()">

                    <span class="seg" role="group" aria-label="Cambiar vista">
                        <button type="button" id="btnVistaTabla" class="btn btn-secondary btn-sm" onclick="cambiarVista('tabla')">Tabla</button>
                        <button type="button" id="btnVistaPasaporte" class="btn btn-primary btn-sm" onclick="cambiarVista('pasaportes')">Pasaportes</button>
                    </span>
                </div>
            </div>

            <!-- Vista pasaportes -->
            <div id="vistaPasaportes" class="passport-grid">
                <%
                    if (listaMascotas == null || listaMascotas.isEmpty()) {
                %>
                    <div class="passport-empty">No hay mascotas registradas actualmente.</div>
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
                    <article class="passport"
                             data-nombre="<%= esc(m.getNombre()).toLowerCase() %>"
                             data-dueno="<%= esc(nombreDueno).toLowerCase() %>"
                             data-especie="<%= esc(m.getEspecie()).toLowerCase() %>">
                        <div class="passport-head">
                            <div>
                                <h3 class="passport-name"><%= esc(m.getNombre()) %></h3>
                                <span class="passport-meta">#<%= m.getIdMascota() %> · <%= esc(m.getEspecie()) %></span>
                            </div>
                            <span class="badge badge-plain"><%= getEtapaVida(m.getEdad()) %></span>
                        </div>

                        <dl class="passport-fields">
                            <div class="pass-field">
                                <dt>Raza</dt>
                                <dd><%= esc(m.getRaza() != null && !m.getRaza().isEmpty() ? m.getRaza() : "Mestizo") %></dd>
                            </div>
                            <div class="pass-field">
                                <dt>Edad</dt>
                                <dd><%= m.getEdad() %> años</dd>
                            </div>
                            <div class="pass-field">
                                <dt>Propietario</dt>
                                <dd><%= esc(nombreDueno) %></dd>
                            </div>
                        </dl>

                        <div class="passport-actions">
                            <a href="<%= request.getContextPath() %>/mascotas?accion=editar&id=<%= m.getIdMascota() %>"
                               class="btn btn-secondary btn-sm">
                                Editar
                            </a>
                            <button class="btn btn-danger btn-sm"
                                    data-id="<%= m.getIdMascota() %>"
                                    data-nombre="<%= esc(m.getNombre()) %>"
                                    onclick="confirmarEliminarDesdeBoton(this)">
                                Eliminar
                            </button>
                        </div>
                    </article>
                <%
                        }
                    }
                %>
            </div>

            <!-- Vista tabla -->
            <div class="table-responsive is-hidden" id="vistaTabla">
                <table class="table">
                    <thead>
                        <tr>
                            <th class="num">ID</th>
                            <th>Nombre</th>
                            <th>Especie</th>
                            <th>Raza</th>
                            <th>Edad / etapa</th>
                            <th>Propietario</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaMascotas == null || listaMascotas.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" class="empty-cell">No hay mascotas registradas actualmente.</td>
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
                            <td class="num"><span class="badge badge-id">#<%= m.getIdMascota() %></span></td>
                            <td class="row-name"><%= esc(m.getNombre()) %></td>
                            <td><span class="badge badge-plain"><%= esc(m.getEspecie()) %></span></td>
                            <td class="row-muted"><%= esc(m.getRaza()) %></td>
                            <td><%= m.getEdad() %> años · <%= getEtapaVida(m.getEdad()) %></td>
                            <td class="row-muted"><%= esc(nombreDueno) %></td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/mascotas?accion=editar&id=<%= m.getIdMascota() %>"
                                       class="btn btn-secondary btn-sm">
                                        Editar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-id="<%= m.getIdMascota() %>"
                                            data-nombre="<%= esc(m.getNombre()) %>"
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
        String  modalTitulo   = "Confirmar eliminación";
        String  modalTexto    = "¿Estás seguro de que deseas eliminar este registro?";
        String  modalBoton    = "Eliminar";
        String  modalCancelar = "Cancelar";
        String  modalAccion   = request.getContextPath() + "/mascotas";
        boolean modalEnlace   = false;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
    function cambiarVista(tipo) {
        var vPasaportes = document.getElementById('vistaPasaportes');
        var vTabla = document.getElementById('vistaTabla');
        var bPas = document.getElementById('btnVistaPasaporte');
        var bTab = document.getElementById('btnVistaTabla');

        if (tipo === 'tabla') {
            vPasaportes.classList.add('is-hidden');
            vTabla.classList.remove('is-hidden');
            bTab.className = 'btn btn-primary btn-sm';
            bPas.className = 'btn btn-secondary btn-sm';
        } else {
            vPasaportes.classList.remove('is-hidden');
            vTabla.classList.add('is-hidden');
            bPas.className = 'btn btn-primary btn-sm';
            bTab.className = 'btn btn-secondary btn-sm';
        }
    }

    function filtrarMascotas() {
        var texto = document.getElementById('buscarMascotaInput').value.toLowerCase().trim();
        var especie = document.getElementById('filtroEspecie').value.toLowerCase().trim();

        var tarjetas = document.querySelectorAll('.passport');
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
                card.classList.remove('is-hidden');
                visibles++;
            } else {
                card.classList.add('is-hidden');
            }
        });

        filas.forEach(function(fila) {
            var n = fila.getAttribute('data-nombre') || '';
            var d = fila.getAttribute('data-dueno') || '';
            var e = fila.getAttribute('data-especie') || '';

            var matchTexto = (n.indexOf(texto) > -1 || d.indexOf(texto) > -1);
            var matchEspecie = (!especie || e.indexOf(especie) > -1);

            if (matchTexto && matchEspecie) {
                fila.classList.remove('is-hidden');
            } else {
                fila.classList.add('is-hidden');
            }
        });

        var badge = document.getElementById('badgeContadorMascotas');
        if (badge) {
            badge.textContent = visibles + ' de ' + total + ' mascotas';
        }
    }

    function confirmarEliminar(id, nombre) {
        var campoId = document.getElementById('confirmarAccionId');
        if (campoId) campoId.value = id;
        abrirModalConfirmacion('¿Estás seguro de que deseas eliminar el pasaporte de la mascota "' + nombre + '"?');
    }
</script>

</body>
</html>
