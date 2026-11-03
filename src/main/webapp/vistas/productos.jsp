<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.petservices.modelo.Producto" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ include file="/WEB-INF/jsp/util.jspf" %>
<%!
    private String moneda(double valor) {
        return "$" + String.format(new Locale("es", "CO"), "%,.0f", valor);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>Productos — PetServices</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body>

<%
    String nombreSesion = (String) session.getAttribute("nombreUsuario");
    if (nombreSesion == null) nombreSesion = "Usuario";
    String fechaHoy = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("es", "ES")).format(new Date());

    String mensaje     = (String) request.getAttribute("mensaje");
    String tipoMensaje = (String) request.getAttribute("tipoMensaje");

    Producto productoEditar = (Producto) request.getAttribute("productoEditar");
    boolean editando = (productoEditar != null);

    List<Producto> listaProductos = (List<Producto>) request.getAttribute("listaProductos");

    int totalStock = 0;
    int itemsAgotados = 0;
    int itemsBajoStock = 0;
    if (listaProductos != null) {
        for (Producto p : listaProductos) {
            totalStock += p.getStock();
            if (p.getStock() <= 0) itemsAgotados++;
            else if (p.getStock() < 5) itemsBajoStock++;
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
                    <h1 class="page-title">Productos</h1>
                    <p class="page-desc">Stock, precios y categorías de los artículos en venta.</p>
                </div>
            </div>
            <div class="page-meta">
                <span class="meta-date"><%= fechaHoy %></span>
                <span class="meta-user"><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>

        <!-- Métricas de inventario -->
        <section class="kpi-band" aria-label="Estado del inventario">
            <div class="kpi">
                <span class="kpi-label">Unidades en almacén</span>
                <span class="kpi-figure"><%= totalStock %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Bajo stock (&lt; 5)</span>
                <span class="kpi-figure"><%= itemsBajoStock %></span>
            </div>
            <div class="kpi">
                <span class="kpi-label">Agotados</span>
                <span class="kpi-figure"><%= itemsAgotados %></span>
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
                    <%= editando ? "Modificar producto" : "Registrar nuevo producto" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/productos" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <div class="card-body">
                <form action="<%= request.getContextPath() %>/productos" method="post" onsubmit="return validarProductoForm(this)">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                    <input type="hidden" name="csrfToken" value="<%= csrfToken(session) %>">
                    <% if (editando) { %>
                        <input type="hidden" name="id" value="<%= productoEditar.getIdProducto() %>">
                    <% } %>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label" for="nombre">Nombre del producto</label>
                            <input class="form-control" type="text" id="nombre" name="nombre"
                                   value="<%= editando ? esc(productoEditar.getNombre()) : "" %>"
                                   placeholder="Ej: Alimento Premium 3 kg" required maxlength="100">
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="categoria">Categoría</label>
                            <% String catActual = editando ? productoEditar.getCategoria() : ""; %>
                            <select class="form-control" id="categoria" name="categoria" required>
                                <option value="">— Seleccionar categoría —</option>
                                <%
                                    String[] cats = {"Alimento", "Medicamento", "Accesorio", "Juguete"};
                                    for (String c : cats) {
                                %>
                                    <option value="<%= c %>" <%= c.equalsIgnoreCase(catActual) ? "selected" : "" %>><%= c %></option>
                                <% } %>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="precio">Precio (COP)</label>
                            <input class="form-control" type="number" id="precio" name="precio" min="100" step="100"
                                   value="<%= editando ? productoEditar.getPrecio() : "" %>"
                                   placeholder="Ej: 45000" required>
                        </div>

                        <div class="form-group">
                            <label class="form-label" for="stock">Stock disponible</label>
                            <input class="form-control" type="number" id="stock" name="stock" min="0" max="9999"
                                   value="<%= editando ? productoEditar.getStock() : "" %>"
                                   placeholder="Ej: 20" required>
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">
                            <%= editando ? "Guardar cambios" : "Registrar producto" %>
                        </button>
                        <% if (editando) { %>
                            <a href="<%= request.getContextPath() %>/productos" class="btn btn-secondary">
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
                    <h2 class="card-title">Catálogo en inventario</h2>
                    <%
                        int totalProds = (listaProductos != null) ? listaProductos.size() : 0;
                    %>
                    <span class="card-sub" id="badgeContadorProductos"><%= totalProds %> artículos</span>
                </div>

                <div class="table-tools">
                    <label class="sr-only" for="filtroCategoria">Filtrar por categoría</label>
                    <select id="filtroCategoria" class="form-control compact" onchange="filtrarTablaProductos()">
                        <option value="">Todas las categorías</option>
                        <option value="alimento">Alimento</option>
                        <option value="medicamento">Medicamento</option>
                        <option value="accesorio">Accesorio</option>
                        <option value="juguete">Juguete</option>
                    </select>

                    <label class="sr-only" for="buscarProductoInput">Buscar producto</label>
                    <input type="text" id="buscarProductoInput" class="form-control"
                           placeholder="Buscar producto…" oninput="filtrarTablaProductos()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th class="num">ID</th>
                            <th>Producto</th>
                            <th>Categoría</th>
                            <th class="num">Precio</th>
                            <th class="num">Stock</th>
                            <th>Estado</th>
                            <th class="num">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaProductos == null || listaProductos.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" class="empty-cell">No hay productos registrados en el inventario.</td>
                        </tr>
                    <%
                        } else {
                            for (Producto p : listaProductos) {
                                boolean agotado = p.getStock() <= 0;
                                boolean bajoStock = !agotado && p.getStock() < 5;
                    %>
                        <tr class="fila-producto"
                            data-nombre="<%= esc(p.getNombre()).toLowerCase() %>"
                            data-categoria="<%= esc(p.getCategoria()).toLowerCase() %>">
                            <td class="num"><span class="badge badge-id">#<%= p.getIdProducto() %></span></td>
                            <td class="row-name"><%= esc(p.getNombre()) %></td>
                            <td><span class="badge badge-plain"><%= esc(p.getCategoria()) %></span></td>
                            <td class="num"><strong><%= moneda(p.getPrecio()) %></strong></td>
                            <td class="num"><%= p.getStock() %> uds.</td>
                            <td>
                                <% if (agotado) { %>
                                    <span class="badge badge-danger">Agotado</span>
                                <% } else if (bajoStock) { %>
                                    <span class="badge badge-warning">Por agotarse</span>
                                <% } else { %>
                                    <span class="badge badge-success">Disponible</span>
                                <% } %>
                            </td>
                            <td class="cell-actions">
                                <span class="cell-actions">
                                    <a href="<%= request.getContextPath() %>/productos?accion=editar&id=<%= p.getIdProducto() %>"
                                       class="btn btn-secondary btn-sm">
                                        Editar
                                    </a>
                                    <button class="btn btn-danger btn-sm"
                                            data-id="<%= p.getIdProducto() %>"
                                            data-nombre="<%= esc(p.getNombre()) %>"
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
        String  modalTexto    = "¿Estás seguro de que deseas eliminar este producto?";
        String  modalBoton    = "Eliminar";
        String  modalCancelar = "Cancelar";
        String  modalAccion   = request.getContextPath() + "/productos";
        boolean modalEnlace   = false;
    %>
    <%@ include file="/WEB-INF/jsp/modal-confirmacion.jspf" %>

</div>

<%@ include file="/WEB-INF/jsp/scripts.jspf" %>
<script>
    function filtrarTablaProductos() {
        var texto = document.getElementById('buscarProductoInput').value.toLowerCase().trim();
        var cat = document.getElementById('filtroCategoria').value.toLowerCase().trim();
        var filas = document.querySelectorAll('.fila-producto');
        var visibles = 0;

        filas.forEach(function(f) {
            var n = f.getAttribute('data-nombre') || '';
            var c = f.getAttribute('data-categoria') || '';

            var matchTexto = (n.indexOf(texto) > -1);
            var matchCat = (!cat || c.indexOf(cat) > -1);

            if (matchTexto && matchCat) {
                f.classList.remove('is-hidden');
                visibles++;
            } else {
                f.classList.add('is-hidden');
            }
        });

        var badge = document.getElementById('badgeContadorProductos');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' artículos';
        }
    }

    function validarProductoForm(form) {
        var nombre = form.nombre.value.trim();
        var cat = form.categoria.value;
        var precio = parseFloat(form.precio.value);
        var stock = parseInt(form.stock.value);

        if (nombre.length < 2) {
            alert('El nombre del producto debe tener al menos 2 caracteres.');
            form.nombre.focus();
            return false;
        }

        if (!cat) {
            alert('Por favor selecciona una categoría.');
            form.categoria.focus();
            return false;
        }

        if (isNaN(precio) || precio < 100) {
            alert('El precio debe ser un valor válido mayor o igual a $100 COP.');
            form.precio.focus();
            return false;
        }

        if (isNaN(stock) || stock < 0) {
            alert('El stock disponible no puede ser negativo.');
            form.stock.focus();
            return false;
        }

        return true;
    }

    function confirmarEliminar(id, nombre) {
        var campoId = document.getElementById('confirmarAccionId');
        if (campoId) campoId.value = id;
        abrirModalConfirmacion('¿Estás seguro de que deseas eliminar el producto "' + nombre + '" del catálogo de la veterinaria?');
    }
</script>

</body>
</html>
