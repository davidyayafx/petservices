<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.petservices.modelo.Pedido" %>
<%@ page import="com.petservices.modelo.DetallePedido" %>
<%@ page import="com.petservices.modelo.Cliente" %>
<%@ page import="com.petservices.modelo.Producto" %>
<%@ page import="java.time.LocalDate" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<%!
    private String fechaCorta(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "—";
        String f = iso.trim();
        if (f.length() >= 10) return f.substring(8, 10) + "/" + f.substring(5, 7) + "/" + f.substring(0, 4);
        return esc(f);
    }

    private String badgeEstado(String estado) {
        String val = (estado == null) ? "" : estado.trim().toLowerCase();
        if ("pagado".equals(val) || "entregado".equals(val)) return "badge-success";
        if ("cancelado".equals(val)) return "badge-danger";
        return "badge-warning";
    }

    private String resumenDetalles(Pedido p) {
        if (p.getDetalles() == null || p.getDetalles().isEmpty()) {
            return (p.getCantidad() > 0) ? p.getCantidad() + " artículo(s)" : "—";
        }
        StringBuilder sb = new StringBuilder();
        for (DetallePedido d : p.getDetalles()) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(d.getCantidad()).append(" × ").append(d.getNombreProducto());
        }
        return sb.toString();
    }

    private String moneda(double valor) {
        return java.text.NumberFormat.getIntegerInstance(new Locale("es", "CO")).format(valor);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Pedidos — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

    List<Pedido> listaPedidos = (List<Pedido>) request.getAttribute("listaPedidos");
    Pedido pedidoEditar = (Pedido) request.getAttribute("pedidoEditar");
    boolean editando = (pedidoEditar != null);
    List<Cliente> listaClientes = (List<Cliente>) request.getAttribute("listaClientes");
    List<Producto> listaProductos = (List<Producto>) request.getAttribute("listaProductos");
    int total = 0, pendientes = 0, pagados = 0;
    double totalVentas = 0;
    if (listaPedidos != null) {
        total = listaPedidos.size();
        for (Pedido ped : listaPedidos) {
            totalVentas += ped.getTotal();
            String est = (ped.getEstado() == null) ? "" : ped.getEstado().trim().toLowerCase();
            if ("pagado".equals(est) || "entregado".equals(est)) pagados++;
            else pendientes++;
        }
    }
    String filtroEstado = request.getParameter("estado");
    boolean filtrando = (filtroEstado != null && !filtroEstado.trim().isEmpty());
    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");
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
                    <h1 class="page-title">Pedidos</h1>
                    <p class="page-desc">Compras registradas, su estado de pago y la existencia de cada producto.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <!-- Banda de cifras: sin tarjetas, dividida por filetes -->
        <section class="kpi-band" aria-label="Resumen de pedidos">
            <div class="kpi">
                <span class="kpi-label">Pedidos</span>
                <span class="kpi-figure"><%= total %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Facturado</span>
                <span class="kpi-figure">$<%= moneda(totalVentas) %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Pendientes</span>
                <span class="kpi-figure"><%= pendientes %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Pagados</span>
                <span class="kpi-figure"><%= pagados %></span>
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

        <!-- Formulario de alta/edición: el servlet envía listas de clientes y productos -->
        <section class="card">
            <div class="card-header">
                <div>
                    <h2 class="card-title"><%= editando ? "Modificar pedido #" + pedidoEditar.getIdPedido() : "Registrar pedido" %></h2>
                    <span class="card-sub"><%= editando ? "Actualiza los datos del pedido en el libro." : "Cliente, producto y cantidad; el total se calcula con el precio vigente." %></span>
                </div>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/pedidos" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/pedidos" method="post" onsubmit="return validarPedidoForm(this)">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken(session) %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= pedidoEditar.getIdPedido() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="idCliente">Cliente comprador</label>
                            <select class="form-control" id="idCliente" name="idCliente" required>
                                <option value="">— Seleccionar cliente —</option>
                                <%
                                    if (listaClientes != null) {
                                        for (Cliente c : listaClientes) {
                                            boolean sel = editando && pedidoEditar.getIdCliente() == c.getIdCliente();
                                %>
                                    <option value="<%= c.getIdCliente() %>" <%= sel ? "selected" : "" %>><%= esc(c.getNombre()) %></option>
                                <%
                                        }
                                    }
                                %>
                            </select>
                        </div>

                        <%
                            int idProdEdit = 0;
                            int cantEdit = 1;
                            if (editando && pedidoEditar.getDetalles() != null && !pedidoEditar.getDetalles().isEmpty()) {
                                DetallePedido dp0 = pedidoEditar.getDetalles().get(0);
                                idProdEdit = dp0.getIdProducto();
                                cantEdit = dp0.getCantidad();
                            }
                        %>
                        <div class="form-group">
                            <label class="form-label" for="idProducto">Producto solicitado</label>
                            <select class="form-control" id="idProducto" name="idProducto" required onchange="calcularTotal()">
                                <option value="">— Seleccionar producto —</option>
                                <%
                                    if (listaProductos != null) {
                                        for (Producto pr : listaProductos) {
                                %>
                                    <option value="<%= pr.getIdProducto() %>"
                                            data-precio="<%= pr.getPrecio() %>"
                                            data-stock="<%= pr.getStock() %>"
                                            <%= pr.getIdProducto() == idProdEdit ? "selected" : "" %>>
                                        <%= esc(pr.getNombre()) %> — $<%= moneda(pr.getPrecio()) %> (stock: <%= pr.getStock() %>)
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
                            <label class="form-label" for="fecha">Fecha del pedido</label>
                            <input class="form-control" type="date" id="fecha" name="fecha" required
                                   value="<%= editando && pedidoEditar.getFecha() != null ? pedidoEditar.getFecha() : LocalDate.now().toString() %>">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="estado">Estado del pedido</label>
                            <% String estadoActual = editando && pedidoEditar.getEstado() != null ? pedidoEditar.getEstado() : "Pendiente"; %>
                            <select class="form-control" id="estado" name="estado" required>
                                <option value="Pendiente"<%= "Pendiente".equalsIgnoreCase(estadoActual) ? " selected" : "" %>>Pendiente</option>
                                <option value="Pagado"<%= "Pagado".equalsIgnoreCase(estadoActual) ? " selected" : "" %>>Pagado</option>
                                <option value="Entregado"<%= "Entregado".equalsIgnoreCase(estadoActual) ? " selected" : "" %>>Entregado</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <span class="form-label">Total estimado</span>
                            <strong id="totalEstimado"><%= editando ? "$" + moneda(pedidoEditar.getTotal()) : "$0" %></strong>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Registrar pedido" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/pedidos" class="btn btn-secondary">Cancelar</a>
                        <% } %>
                    </div>
                </form>
            </div>
        </section>

        <!-- Dos zonas: libro de pedidos + columna de anotaciones -->
        <div class="dash-grid">

            <section class="card">
                <div class="card-header">
                    <div>
                        <h2 class="card-title">Libro de pedidos</h2>
                        <span class="card-sub"><%= total %> en el libro</span>
                    </div>
                    <form class="table-tools" method="get" action="<%= request.getContextPath() %>/pedidos">
                        <div class="form-group">
                            <label class="form-label" for="estadoFiltro">Estado</label>
                            <select class="form-control compact" id="estadoFiltro" name="estado" onchange="this.form.submit()">
                                <option value=""<%= !filtrando ? " selected" : "" %>>Todos</option>
                                <option value="Pendiente"<%= "Pendiente".equals(filtroEstado) ? " selected" : "" %>>Pendiente</option>
                                <option value="Pagado"<%= "Pagado".equals(filtroEstado) ? " selected" : "" %>>Pagado</option>
                                <option value="Entregado"<%= "Entregado".equals(filtroEstado) ? " selected" : "" %>>Entregado</option>
                            </select>
                        </div>
                        <button class="btn" type="submit">Filtrar</button>
                    </form>
                </div>

                <div class="table-responsive">
                    <table class="table table-pedidos">
                        <thead>
                            <tr>
                                <th class="num">N.º</th>
                                <th>Fecha</th>
                                <th>Cliente</th>
                                <th>Productos</th>
                                <th class="num">Total</th>
                                <th>Estado</th>
                                <th class="num">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                        <%
                            if (listaPedidos == null || listaPedidos.isEmpty()) {
                        %>
                            <tr>
                                <td colspan="7" class="empty-cell">Sin pedidos registrados.</td>
                            </tr>
                        <%
                            } else {
                                boolean algunoVisible = false;
                                for (Pedido p : listaPedidos) {
                                    String estadoTexto = (p.getEstado() == null || p.getEstado().trim().isEmpty()) ? "Pendiente" : p.getEstado().trim();
                                    if (filtrando && !estadoTexto.equalsIgnoreCase(filtroEstado.trim())) {
                                        continue;
                                    }
                                    algunoVisible = true;
                        %>
                            <tr>
                                <td class="num"><span class="badge badge-id">#<%= p.getIdPedido() %></span></td>
                                <td><%= fechaCorta(p.getFecha()) %></td>
                                <td class="row-name"><%= esc(p.getNombreCliente()) %></td>
                                <td class="row-muted"><%= esc(resumenDetalles(p)) %></td>
                                <td class="num">$<%= moneda(p.getTotal()) %></td>
                                <td><span class="badge <%= badgeEstado(p.getEstado()) %>"><%= esc(estadoTexto) %></span></td>
                                <td class="cell-actions">
                                    <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/pedidos?accion=editar&id=<%= p.getIdPedido() %>" class="btn btn-sm">Editar</a>
                                    <form method="post" action="<%= request.getContextPath() %>/pedidos" onsubmit="return confirm('¿Marcar el pedido #<%= p.getIdPedido() %> como pagado?')">
                                        <input type="hidden" name="accion" value="actualizar">
        <input type="hidden" name="csrfToken" value="<%= csrfToken(session) %>">
                                        <input type="hidden" name="id" value="<%= p.getIdPedido() %>">
                                        <input type="hidden" name="idCliente" value="<%= p.getIdCliente() %>">
                                        <input type="hidden" name="idProducto" value="<%= p.getDetalles().isEmpty() ? 0 : p.getDetalles().get(0).getIdProducto() %>">
                                        <input type="hidden" name="cantidad" value="<%= p.getDetalles().isEmpty() ? p.getCantidad() : p.getDetalles().get(0).getCantidad() %>">
                                        <input type="hidden" name="fecha" value="<%= p.getFecha() %>">
                                        <input type="hidden" name="estado" value="Pagado">
                                        <button class="btn btn-sm" type="submit"<%= "pagado".equalsIgnoreCase(p.getEstado()) || "entregado".equalsIgnoreCase(p.getEstado()) ? " disabled" : "" %>>Marcar pagado</button>
                                    </form>
                                    <button type="button" class="btn btn-sm btn-danger"
                                            data-id="<%= p.getIdPedido() %>"
                                            onclick="confirmarEliminarDesdeBoton(this)">Eliminar</button>
                                    </span>
                                </td>
                            </tr>
                        <%
                                }
                                if (!algunoVisible) {
                        %>
                            <tr>
                                <td colspan="7" class="empty-cell">No hay pedidos con este estado.</td>
                            </tr>
                        <%
                                }
                            }
                        %>
                        </tbody>
                    </table>
                </div>
            </section>

            <aside class="notes note-rail" aria-label="Anotaciones del módulo">
                <p class="notes-head">Anotaciones</p>
                <p class="note"><b>Marcar pagado:</b> reenvía el pedido completo con estado Pagado (el libro guarda siempre pedido, detalle e importes juntos). Para revertir el estado, usa «Editar».</p>
                <p class="note"><b>Filtro por estado:</b> el selector aplica el filtro al instante al cambiar; el botón «Filtrar» sirve por si el navegador no envía el formulario solo.</p>
                <p class="note"><b>Origen de los pedidos:</b> cada pedido se crea desde este libro con cliente, producto y cantidad; el total se calcula con el precio vigente del producto.</p>
                <p class="note"><b>Importes:</b> todos los valores se muestran en pesos colombianos, sin decimales.</p>
            </aside>

        </div>

    </main>
    <%
        // ── Modal compartido (WEB-INF/jsp/modal-confirmacion.jspf) ──
        String  modalTitulo   = "Confirmar eliminación";
        String  modalTexto    = "¿Estás seguro de que deseas eliminar este pedido del libro?";
        String  modalBoton    = "Eliminar pedido";
        String  modalCancelar = "Cancelar";
        String  modalAccion   = request.getContextPath() + "/pedidos";
        boolean modalEnlace   = false;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
    function calcularTotal() {
        var prodSelect = document.getElementById('idProducto');
        var cantInput = document.getElementById('cantidad');
        var totalDiv = document.getElementById('totalEstimado');
        if (!prodSelect || !cantInput || !totalDiv) return;

        var opt = prodSelect.options[prodSelect.selectedIndex];
        var precio = parseFloat(opt.getAttribute('data-precio')) || 0;
        var cant = parseInt(cantInput.value) || 0;

        totalDiv.textContent = '$' + (precio * cant).toLocaleString('es-CO');
    }

    function validarPedidoForm(form) {
        if (!form.idCliente.value) {
            alert('Por favor selecciona el cliente que realiza el pedido.');
            form.idCliente.focus();
            return false;
        }

        if (!form.idProducto.value) {
            alert('Por favor selecciona un producto.');
            form.idProducto.focus();
            return false;
        }

        var opt = form.idProducto.options[form.idProducto.selectedIndex];
        var stockDisponible = parseInt(opt.getAttribute('data-stock')) || 0;
        var cantidad = parseInt(form.cantidad.value);

        if (isNaN(cantidad) || cantidad <= 0) {
            alert('La cantidad del pedido debe ser al menos 1 unidad.');
            form.cantidad.focus();
            return false;
        }

        if (cantidad > stockDisponible) {
            alert('La cantidad solicitada supera el stock disponible (' + stockDisponible + ' unidades).');
            form.cantidad.focus();
            return false;
        }

        if (!form.fecha.value) {
            alert('Por favor indica la fecha del pedido.');
            form.fecha.focus();
            return false;
        }

        return true;
    }

    function confirmarEliminar(id) {
        var campoId = document.getElementById('confirmarAccionId');
        if (campoId) campoId.value = id;
        abrirModalConfirmacion('¿Estás seguro de que deseas eliminar el pedido #' + id + ' del libro? Esta acción no se puede deshacer.');
    }
</script>

</body>
</html>
