<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.petservices.modelo.Producto" %>
<%!
    private String esc(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String moneda(double valor) {
        return "$" + String.format(new Locale("es", "CO"), "%,.0f", valor);
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Productos — PetServices</title>
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
                <a href="<%= request.getContextPath() %>/dashboard" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/></svg>
                    <span>Dashboard</span>
                </a>
            </li>
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
                <a href="<%= request.getContextPath() %>/servicios" class="nav-link">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z"/></svg>
                    <span>Servicios</span>
                </a>
            </li>
            <li>
                <a href="<%= request.getContextPath() %>/productos" class="nav-link active">
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
                <h1 class="page-title">Inventario de Productos</h1>
                <p class="page-desc">Administra el stock, precios y categorías de los artículos en venta.</p>
            </div>
            <div class="user-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                <span><%= esc(nombreSesion) %></span>
            </div>
        </header>

        <!-- Métricas Rápidas de Inventario -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 14px; margin-bottom: 24px;">
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Unidades en Almacén</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-primary);"><%= totalStock %> uds.</div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Bajo Stock (&lt; 5)</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-warning);"><%= itemsBajoStock %> artículos</div>
            </div>
            <div style="background: var(--color-surface); border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px 18px;">
                <div style="font-size: 0.8rem; color: var(--color-text-muted);">Agotados</div>
                <div style="font-size: 1.4rem; font-weight: 700; color: var(--color-danger);"><%= itemsAgotados %> artículos</div>
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
                    <%= editando ? "Modificar Producto" : "Registrar Nuevo Producto" %>
                </h2>
                <% if (editando) { %>
                    <a href="<%= request.getContextPath() %>/productos" class="btn btn-secondary btn-sm">
                        Cancelar edición
                    </a>
                <% } %>
            </div>

            <form action="<%= request.getContextPath() %>/productos" method="post" onsubmit="return validarProductoForm(this)">
                <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">
                <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= productoEditar.getIdProducto() %>">
                <% } %>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label" for="nombre">Nombre del Producto</label>
                        <input class="form-control" type="text" id="nombre" name="nombre"
                               value="<%= editando ? esc(productoEditar.getNombre()) : "" %>"
                               placeholder="Ej: Alimento Premium 3kg" required maxlength="100">
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="categoria">Categoría</label>
                        <% String catActual = editando ? productoEditar.getCategoria() : ""; %>
                        <select class="form-control" id="categoria" name="categoria" required>
                            <option value="">— Seleccionar Categoría —</option>
                            <%
                                String[] cats = {"Alimentos", "Accesorios", "Higiene", "Juguetes", "Farmacia"};
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
                        <label class="form-label" for="stock">Stock Disponible</label>
                        <input class="form-control" type="number" id="stock" name="stock" min="0" max="9999"
                               value="<%= editando ? productoEditar.getStock() : "" %>"
                               placeholder="Ej: 20" required>
                    </div>
                </div>

                <div style="margin-top: 20px; display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-primary">
                        <%= editando ? "Guardar Cambios" : "Guardar Producto" %>
                    </button>
                    <% if (editando) { %>
                        <a href="<%= request.getContextPath() %>/productos" class="btn btn-secondary">
                            Cancelar
                        </a>
                    <% } %>
                </div>
            </form>
        </section>

        <!-- Tabla de Productos -->
        <section class="card">
            <div class="card-header" style="flex-wrap: wrap; gap: 12px;">
                <div>
                    <h2 class="card-title">Catálogo en Inventario</h2>
                    <%
                        int totalProds = (listaProductos != null) ? listaProductos.size() : 0;
                    %>
                    <span class="badge badge-success" id="badgeContadorProductos"><%= totalProds %> Artículos</span>
                </div>

                <div style="display: flex; gap: 10px; flex-wrap: wrap; align-items: center;">
                    <select id="filtroCategoria" class="form-control" style="width: auto;" onchange="filtrarTablaProductos()">
                        <option value="">Todas las Categorías</option>
                        <option value="alimentos">Alimentos</option>
                        <option value="accesorios">Accesorios</option>
                        <option value="higiene">Higiene</option>
                        <option value="juguetes">Juguetes</option>
                        <option value="farmacia">Farmacia</option>
                    </select>

                    <input type="text" id="buscarProductoInput" class="form-control" style="width: 200px;"
                           placeholder="🔍 Buscar producto..." oninput="filtrarTablaProductos()">
                </div>
            </div>

            <div class="table-responsive">
                <table class="table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Producto</th>
                            <th>Categoría</th>
                            <th>Precio Unitario</th>
                            <th>Stock</th>
                            <th>Estado</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                    <%
                        if (listaProductos == null || listaProductos.isEmpty()) {
                    %>
                        <tr>
                            <td colspan="7" style="text-align: center; color: var(--color-text-muted); padding: 32px;">
                                No hay productos registrados en el inventario.
                            </td>
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
                            <td><span class="badge" style="background: var(--color-primary-light); color: var(--color-primary);">#<%= p.getIdProducto() %></span></td>
                            <td><strong><%= esc(p.getNombre()) %></strong></td>
                            <td><span class="badge" style="background: var(--color-surface-tint); color: var(--color-text); border: 1px solid var(--color-border);"><%= esc(p.getCategoria()) %></span></td>
                            <td><strong><%= moneda(p.getPrecio()) %></strong></td>
                            <td>
                                <strong><%= p.getStock() %></strong> uds.
                                <% if (bajoStock) { %>
                                    <span style="color: var(--color-warning); font-size: 0.75rem; font-weight: 600;">(Bajo)</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (agotado) { %>
                                    <span class="badge badge-danger">Agotado</span>
                                <% } else if (bajoStock) { %>
                                    <span class="badge badge-warning">Por agotarse</span>
                                <% } else { %>
                                    <span class="badge badge-success">Disponible</span>
                                <% } %>
                            </td>
                            <td style="text-align: right;">
                                <a href="<%= request.getContextPath() %>/productos?accion=editar&id=<%= p.getIdProducto() %>"
                                   class="btn btn-secondary btn-sm" style="text-decoration: none;">
                                    Editar
                                </a>
                                <button class="btn btn-danger btn-sm"
                                        onclick="confirmarEliminar(<%= p.getIdProducto() %>, '<%= esc(p.getNombre()).replace("'", "\\'") %>')">
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
                ¿Estás seguro de que deseas eliminar este producto?
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
                f.style.display = '';
                visibles++;
            } else {
                f.style.display = 'none';
            }
        });

        var badge = document.getElementById('badgeContadorProductos');
        if (badge) {
            badge.textContent = visibles + ' de ' + filas.length + ' Artículos';
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
        var url = '<%= request.getContextPath() %>/productos?accion=eliminar&id=' + id;
        abrirModalConfirmacion(url, '¿Deseas eliminar el producto "' + nombre + '" del catálogo de la veterinaria?');
    }
</script>

</body>
</html>
