<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.petservices.modelo.Cita" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Dashboard — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
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

    int citPend = 0, citConf = 0, citCan = 0, citOtra = 0;
    if (ultimasCitas != null) {
        for (Cita c : ultimasCitas) {
            String est = (c.getEstado() == null) ? "" : c.getEstado().trim().toLowerCase();
            if ("pendiente".equals(est))      citPend++;
            else if ("confirmada".equals(est)) citConf++;
            else if ("cancelada".equals(est))  citCan++;
            else                               citOtra++;
        }
    }

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
                    <h1 class="page-title">Dashboard</h1>
                    <p class="page-desc">Resumen general del sistema: agenda, inventario y registros del día.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <!-- Banda de KPIs: sin tarjetas, dividida por filetes -->
        <section class="kpi-band" aria-label="Indicadores del sistema">
            <a class="kpi" href="<%= request.getContextPath() %>/clientes">
                <span class="kpi-label">Clientes</span>
                <span class="kpi-figure"><%= totalClientes %></span>
            </a>
            <a class="kpi" href="<%= request.getContextPath() %>/mascotas">
                <span class="kpi-label">Mascotas</span>
                <span class="kpi-figure"><%= totalMascotas %></span>
            </a>
            <a class="kpi" href="<%= request.getContextPath() %>/citas">
                <span class="kpi-label">Citas</span>
                <span class="kpi-figure"><%= totalCitas %></span>
            </a>
            <a class="kpi" href="<%= request.getContextPath() %>/servicios">
                <span class="kpi-label">Servicios</span>
                <span class="kpi-figure"><%= totalServicios %></span>
            </a>
            <a class="kpi" href="<%= request.getContextPath() %>/productos">
                <span class="kpi-label">Productos</span>
                <span class="kpi-figure"><%= totalProductos %></span>
            </a>
            <a class="kpi" href="<%= request.getContextPath() %>/pedidos">
                <span class="kpi-label">Pedidos</span>
                <span class="kpi-figure"><%= totalPedidos %></span>
            </a>
        </section>

        <div class="rule" aria-hidden="true"><span class="rule-tick">+</span></div>

        <!-- Dos zonas: tabla de citas + columna de anotaciones -->
        <div class="dash-grid">

            <section class="card">
                <div class="card-header">
                    <h2 class="card-title">Últimas citas agendadas</h2>
                    <a href="<%= request.getContextPath() %>/citas" class="btn btn-secondary btn-sm">Ver todas</a>
                </div>

                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th class="num">Cita</th>
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
                                <td colspan="6" class="empty-cell">No hay citas agendadas aún.</td>
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
                                <td class="num"><span class="badge badge-id">#<%= c.getIdCita() %></span></td>
                                <td class="row-name"><%= esc(c.getNombreCliente()) %></td>
                                <td><%= esc(c.getNombreMascota()) %></td>
                                <td class="row-muted"><%= esc(c.getServicio()) %></td>
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

            <aside class="notes note-rail" aria-label="Anotaciones al margen">
                <p class="notes-head">Anotaciones</p>
                <p class="note">Agenda: <b><%= citPend %> pendientes</b> · <%= citConf %> confirmadas · <%= citCan %> canceladas<%= citOtra > 0 ? " · " + citOtra + " otras" : "" %> en las últimas 5 citas.</p>
                <p class="note">Inventario: <b><%= totalProductos %> productos</b> · <%= totalServicios %> servicios en catálogo.</p>
                <p class="note">Cartera: <b><%= totalPedidos %> pedidos</b> registrados · <%= totalClientes %> clientes · <%= totalMascotas %> mascotas.</p>
                <p class="note">Los registros se actualizan al crear, editar o eliminar en cada módulo.</p>
            </aside>

        </div>

    </main>
</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>

</body>
</html>
