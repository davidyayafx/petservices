<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.petservices.db.RespaldoService" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Copias de seguridad — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");

    List<RespaldoService.Copia> listaCopias =
            (List<RespaldoService.Copia>) request.getAttribute("listaCopias");
    String carpetaCopias  = (String) request.getAttribute("carpetaCopias");
    String rutaBaseDatos  = (String) request.getAttribute("rutaBaseDatos");
    Integer retencion     = (Integer) request.getAttribute("retencionCopias");
    int totalCopias       = (listaCopias != null) ? listaCopias.size() : 0;
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
                    <h1 class="page-title">Copias de seguridad</h1>
                    <p class="page-desc">Respalda y restaura la base de datos local de PetServices.</p>
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

        <!-- Crear copia + datos de ubicación -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Crear una copia ahora</h2>
                    <span class="card-sub">Se guarda un archivo SQLite consistente, aunque la aplicación esté en marcha.</span>
                </div>
            </div>
            <div class="card-body">
                <form action="<%= request.getContextPath() %>/respaldos" method="post">
                    <input type="hidden" name="accion" value="crear">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken(session) %>">
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Crear copia de seguridad</button>
                    </div>
                </form>

                <div class="table-responsive" style="margin-top: 1rem;">
                    <table class="table">
                        <tbody>
                            <tr>
                                <th>Base de datos</th>
                                <td><%= esc(rutaBaseDatos) %></td>
                            </tr>
                            <tr>
                                <th>Carpeta de copias</th>
                                <td><%= esc(carpetaCopias) %></td>
                            </tr>
                            <tr>
                                <th>Copias que se conservan</th>
                                <td><%= retencion == null ? RespaldoService.RETENCION_POR_DEFECTO : retencion %>
                                    <span class="card-sub">(las más recientes; el resto se elimina automáticamente)</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </section>

        <!-- Listado de copias -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title">Copias disponibles</h2>
                    <span class="card-sub"><%= totalCopias %> copia<%= totalCopias == 1 ? "" : "s" %> en la carpeta</span>
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>Archivo</th>
                            <th>Fecha</th>
                            <th class="num">Tamaño</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaCopias == null || listaCopias.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="4" class="empty-cell">Todavía no hay copias. Use el botón «Crear copia de seguridad».</td>
                        </tr>
                    <%
                        } else {
                            for (RespaldoService.Copia copia : listaCopias) {
                    %>
                        <tr>
                            <td class="row-name"><%= esc(copia.getNombre()) %></td>
                            <td><%= esc(copia.getFecha()) %></td>
                            <td class="num"><%= esc(copia.getTamanoLegible()) %></td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/respaldos?accion=descargar&amp;nombre=<%= esc(copia.getNombre()) %>"
                                       class="btn btn-secondary btn-sm">
                                        Descargar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-nombre="<%= esc(copia.getNombre()) %>"
                                            onclick="confirmarRestauracionDesdeBoton(this)">
                                        Restaurar
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

    <!-- Formulario y modal de confirmación de restauración (destructiva) -->
    <form id="formRestaurar" method="post" action="<%= request.getContextPath() %>/respaldos" hidden>
        <input type="hidden" name="accion" value="restaurar">
        <input type="hidden" name="nombre" id="restaurarNombre" value="">
        <input type="hidden" name="csrfToken" value="<%= csrfToken(session) %>">
    </form>

    <div id="modalRestaurar" class="app-modal-backdrop" role="dialog" aria-modal="true"
         aria-labelledby="modalRestaurarTitulo" aria-describedby="modalRestaurarTexto">
        <div class="app-modal-card">
            <div class="app-modal-header">
                <div class="app-modal-icon danger" aria-hidden="true">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                </div>
                <h3 id="modalRestaurarTitulo" class="app-modal-title">Restaurar copia de seguridad</h3>
            </div>
            <div id="modalRestaurarTexto" class="app-modal-body">
                Se reemplazarán <strong>todos</strong> los datos actuales por los de la copia
                <span id="nombreCopiaRestaurar"></span>. Antes de restaurar se guarda
                automáticamente una copia del estado actual.
            </div>
            <div class="app-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="cerrarModalRestaurar()">Cancelar</button>
                <button type="button" class="btn btn-danger" onclick="ejecutarRestauracion()">Restaurar</button>
            </div>
        </div>
    </div>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
    function confirmarRestauracionDesdeBoton(btn) {
        if (!btn) return;
        confirmarRestauracion(btn.getAttribute('data-nombre'));
    }

    function confirmarRestauracion(nombre) {
        var campo = document.getElementById('restaurarNombre');
        if (campo) campo.value = nombre || '';
        var etiqueta = document.getElementById('nombreCopiaRestaurar');
        if (etiqueta) etiqueta.textContent = nombre || '';
        var modal = document.getElementById('modalRestaurar');
        if (modal) {
            modal.classList.add('is-active');
            var cancelar = modal.querySelector('.btn-secondary');
            if (cancelar) cancelar.focus();
        }
    }

    function cerrarModalRestaurar() {
        var modal = document.getElementById('modalRestaurar');
        if (modal) modal.classList.remove('is-active');
    }

    function ejecutarRestauracion() {
        var formulario = document.getElementById('formRestaurar');
        if (formulario) formulario.submit();
    }

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            cerrarModalRestaurar();
        }
    });
</script>

</body>
</html>
