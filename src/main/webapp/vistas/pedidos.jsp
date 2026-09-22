<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Producto" %>
<%@ page import="com.petservices.modelo.Pedido" %>
<%@ page import="com.petservices.modelo.DetallePedido" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String moneda(double valor) {
        return "$" + String.format(new Locale("es", "CO"), "%,.0f", valor);
    }

    private String badgeEstadoClass(String estado) {
        String val = (estado == null) ? "" : estado.trim().toLowerCase();
        if ("entregado".equals(val)) return "badge-success";
        if ("cancelado".equals(val)) return "badge-danger";
        if ("pagado".equals(val)) return "badge-success";
        return "badge-warning";
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Pedidos — PetServices</title>
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

    Pedido pedidoEditar = (Pedido) request.getAttribute("pedidoEditar");
    boolean editando = (pedidoEditar != null);

    List<Pedido>   listaPedidos   = (List<Pedido>)   request.getAttribute("listaPedidos");
    List<Cliente>  listaClientes  = (List<Cliente>)  request.getAttribute("listaClientes");
    List<Producto> listaProductos = (List<Producto>) request.getAttribute("listaProductos");

    double totalVentas = 0;
    int pedidosEntregados = 0;
    int pedidosPendientes = 0;
    if (listaPedidos != null) {
        for (Pedido ped : listaPedidos) {
            totalVentas += ped.getTotal();
            String est = (ped.getEstado() == null) ? "" : ped.getEstado().toLowerCase();
            if (est.contains("entreg") || est.contains("pag")) pedidosEntregados++;
            else if (est.contains("pend")) pedidosPendientes++;
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
                <a href="<%= request.getContextPath() %>/productos" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/><line x1="3" y1="6" x2="21" y2="6"/><path d="M16 10a4 4 0 0 1-8 0"/></svg>
                    <span>Productos</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/pedidos" class="nav-link active">
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
                <h1 class="page-title">Control de Pedidos</h1>
                <p class="page-desc">Registra ventas, compras de artículos y seguimiento de entregas.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <!-- Métricas Rápidas de Facturación -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; margin-bottom: 24px;">
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Monto Total Registrado</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-primary);"><%= moneda(totalVentas) %></div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Pedidos Entregados/Pagados</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-success);"><%= pedidosEntregados %></div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Pedidos Pendientes</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-warning);"><%= pedidosPendientes %></div>
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
                    <%= editando ? "Modificar Pedido #" + pedidoEditar.getIdPedido() : "Registrar Nuevo Pedido" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/pedidos" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <form action="<%= request.getContextPath() %>/pedidos" method="post" onsubmit="return validarPedidoForm(this)">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= pedidoEditar.getIdPedido() %>">
                <% } %>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="idCliente">Cliente Comprador</label>
                        <select class="form-control" id="idCliente" name="idCliente" required>
                            <option value="">— Seleccionar Cliente —</option>
                            <%
                                if (listaClientes != null) {
                                    for (Cliente c : listaClientes) {
                                        boolean seleccionado = editando && pedidoEditar.getIdCliente() == c.getIdCliente();
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
                        <label class="form-label" for="idProducto">Producto Solicitado</label>
                        <%
                            int idProdEdit = 0;
                            int cantEdit   = 1;
                            if (editando && pedidoEditar.getDetalles() != null && !pedidoEditar.getDetalles().isEmpty()) {
                                DetallePedido dp = pedidoEditar.getDetalles().get(0);
                                idProdEdit = dp.getIdProducto();
                                cantEdit   = dp.getCantidad();
                            }
                        %>
                        <select class="form-control" id="idProducto" name="idProducto" required onchange="calcularTotal()">
                            <option value="">— Seleccionar Producto —</option>
                            <%
                                if (listaProductos != null) {
                                    for (Producto p : listaProductos) {
                                        boolean seleccionado = (p.getIdProducto() == idProdEdit);
                            %>
                                <option value="<%= p.getIdProducto() %>"
                                        data-precio="<%= p.getPrecio() %>"
                                        data-stock="<%= p.getStock() %>"
                                        <%= seleccionado ? "selected" : "" %>>
                                    <%= esc(p.getNombre()) %> — <%= moneda(p.getPrecio()) %> (Stock: <%= p.getStock() %>)
                                </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="cantidad">Cantidad</label>
                        <input class="form-control" type="number" id="cantidad" name="cantidad"
                               min="1" max="999" value="<%= cantEdit %>" required oninput="calcularTotal()">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="fecha">Fecha del Pedido</label>
                        <input class="form-control" type="date" id="fecha" name="fecha" required
                               value="<%= editando && pedidoEditar.getFecha() != null ? pedidoEditar.getFecha() : LocalDate.now().toString() %>">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="estado">Estado del Pedido</label>
                        <% String estadoActual = editando && pedidoEditar.getEstado() != null ? pedidoEditar.getEstado() : "Pendiente"; %>
                        <select class="form-control" id="estado" name="estado" required>
                            <%
                                String[] estados = {"Pendiente", "Pagado", "Enviado", "Entregado", "Cancelado"};
                                for (String e : estados) {
                            %>
                                <option value="<%= e %>" <%= e.equalsIgnoreCase(estadoActual) ? "selected" : "" %>><%= e %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group" style="display: flex; flex-direction: column; justify-content: center;">
                        <span class="form-label">Total Estimado</span>
                        <div id="totalEstimado" style="font-size: 1.3rem; font-weight: 700; color: var(--color-primary);">
                            <%= editando ? moneda(pedidoEditar.getTotal()) : "$0" %>
                        </div>
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">
                        <%= editando ? "Guardar Cambios" : "Crear Pedido" %>
                    </button>
                    <% if (editando) { %>
                        <a href="<%= request.getContextPath() %>/pedidos" class="btn btn-secondary">
                            Cancelar
                        </a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Tabla de Pedidos -->
        <section class="card">
            <div class="card-header" style="flex-wrap: wrap; gap: 12px;">
                <div>
                    <h2 class="card-title">Historial de Pedidos</h2>
                    <%
                        int totalPedidos = (listaPedidos != null) ? listaPedidos.size() : 0;
                    %>
                    <span class="badge badge-success" id="badgeContadorPedidos"><%= totalPedidos %> Pedidos</span>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <select id="filtroEstadoPedido" class="form-control" style="width: auto;" onchange="filtrarTablaPedidos()">
                        <option value="">Todos los Estados</option>
                        <option value="pendiente">Pendientes</option>
                        <option value="pagado">Pagados</option>
                        <option value="enviado">Enviados</option>
                        <option value="entregado">Entregados</option>
                        <option value="cancelado">Cancelados</option>
                    </select>

                    <input type="text" id="buscarPedidoInput" class="form-control" style="width: 200px;"
                           placeholder="🔍 Buscar cliente, producto..." oninput="filtrarTablaPedidos()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Cliente</th>
                            <th>Productos / Detalle</th>
                            <th>Fecha</th>
                            <th>Total</th>
                            <th>Estado</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaPedidos == null || listaPedidos.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 32px;">
                                No hay pedidos registrados en el sistema.
                            </td>
                        </tr>
                    <%
                        } else {
                            for (Pedido ped : listaPedidos) {
                                StringBuilder prodsStr = new StringBuilder();
                                if (ped.getDetalles() != null) {
                                    for (DetallePedido dp : ped.getDetalles()) {
                                        prodsStr.append(dp.getNombreProducto()).append(" ");
                                    }
                                }
                    %>
                        <tr class="fila-pedido"
                            data-cliente="<%= esc(ped.getNombreCliente()).toLowerCase() %>"
                            data-productos="<%= esc(prodsStr.toString()).toLowerCase() %>"
                            data-estado="<%= esc(ped.getEstado()).toLowerCase() %>">
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= ped.getIdPedido() %></span></td>
                            <td><strong><%= esc(ped.getNombreCliente()) %></strong></td>
                            <td>
                                <%
                                    if (ped.getDetalles() != null && !ped.getDetalles().isEmpty()) {
                                        for (DetallePedido dp : ped.getDetalles()) {
                                %>
                                    <div>• <%= esc(dp.getNombreProducto()) %> × <strong><%= dp.getCantidad() %></strong></div>
                                <%
                                        }
                                    } else {
                                %>
                                    <span style="color: var(--color-text-muted);">—</span>
                                <% } %>
                            </td>
                            <td><%= ped.getFecha() != null ? ped.getFecha() : "—" %></td>
                            <td><strong style="color: var(--color-primary);"><%= moneda(ped.getTotal()) %></strong></td>
                            <td>
                                <span class="badge <%= badgeEstadoClass(ped.getEstado()) %>">
                                    <%= esc(ped.getEstado()) %>
                                </span>
                            </td>
                            <td style="text-align: right;">
                                <a href="<%= request.getContextPath() %>/pedidos?accion=editar&id=<%= ped.getIdPedido() %>"
                                   class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                    Editar
                                </a>
                                <button class="btn btn-danger btn-sm"
                                        onclick="confirmarEliminar(<%= ped.getIdPedido() %>)">
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
                ¿Estás seguro de que deseas eliminar este pedido?
            </div>
            <div class="app-modal-footer">
                <button type="button" class="btn btn-secondary" onclick="cerrarModalConfirmacion()">Cancelar</button>
                <a id="btnConfirmarAccion" href="#" class="btn btn-danger">Eliminar Pedido</a>
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

    function filtrarTablaPedidos() {
        var texto = document.getElementById('buscarPedidoInput').value.toLowerCase().trim();
        var estado = document.getElementById('filtroEstadoPedido').value.toLowerCase().trim();
        var filas = document.querySelectorAll('.fila-pedido');
        var visibles = 0;

        filas.forEach(function(f) {
            var c = f.getAttribute('data-cliente') || '';
            var p = f.getAttribute('data-productos') || '';
            var e = f.getAttribute('data-estado') || '';

            var matchTexto = (c.indexOf(texto) > -1 || p.indexOf(texto) > -1);
            var matchEstado = (!estado || e.indexOf(estado) > -1);

            if (matchTexto && matchEstado) {
                f.style.display = '';
                visibles++;
            } else {
                f.style.display = 'none';
            }
        });

        var badge = document.getElementById('badgeContadorPedidos');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' Pedidos';
        }
    }

    function calcularTotal() {
        var prodSelect = document.getElementById('idProducto');
        var cantInput  = document.getElementById('cantidad');
        var totalDiv   = document.getElementById('totalEstimado');

        var opt = prodSelect.options[prodSelect.selectedIndex];
        if (!opt || !opt.value) {
            totalDiv.textContent = '$0';
            return;
        }

        var precio = parseFloat(opt.getAttribute('data-precio')) || 0;
        var cant   = parseInt(cantInput.value) || 0;
        var total  = precio * cant;

        totalDiv.textContent = '$' + total.toLocaleString('es-CO');
    }

    function validarPedidoForm(form) {
        var cliente = form.idCliente.value;
        var prodSelect = form.idProducto;
        var producto = prodSelect.value;
        var cantidad = parseInt(form.cantidad.value);
        var fecha = form.fecha.value;

        if (!cliente) {
            alert('Por favor selecciona el cliente que realiza el pedido.');
            form.idCliente.focus();
            return false;
        }

        if (!producto) {
            alert('Por favor selecciona un producto.');
            form.idProducto.focus();
            return false;
        }

        var opt = prodSelect.options[prodSelect.selectedIndex];
        var stockDisponible = parseInt(opt.getAttribute('data-stock')) || 0;

        if (isNaN(cantidad) || cantidad <= 0) {
            alert('La cantidad del pedido debe ser al menos 1 unidad.');
            form.cantidad.focus();
            return false;
        }

        if (stockDisponible <= 0 && !form.id) {
            if (!confirm('El producto seleccionado actualmente no tiene stock disponible en inventario. ¿Deseas continuar?')) {
                return false;
            }
        }

        if (!fecha) {
            alert('Por favor indica la fecha del pedido.');
            form.fecha.focus();
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

    function confirmarEliminar(id) {
        var url = '<%= request.getContextPath() %>/pedidos?accion=eliminar&id=' + id;
        abrirModalConfirmacion(url, '¿Deseas eliminar permanentemente el registro del pedido #' + id + '?');
    }
</script>

</body>
</html>
