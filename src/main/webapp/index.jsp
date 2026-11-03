<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--
  index.jsp — Puerta de entrada pública de PetServices
  ─────────────────────────────────────────────────────
  Se sirve como welcome-file (sin atributos de servlet).
  Si ya hay sesión activa, redirige al dashboard antes de emitir salida.
  Proyecto: PetServices - SENA GA7-220501096-AA2-EV02
--%>
<%
    Object clienteSesion = session.getAttribute("clienteSesion");
    if (clienteSesion != null) {
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <title>PetServices — Cuidado integral para mascotas</title>
    <%@ include file="/WEB-INF/jsp/head.jspf" %>
</head>
<body class="public-page">

<!-- ── Barra pública ── -->
<header class="public-topbar">
    <div class="public-bar-inner">
        <span class="auth-brand">
            <span class="brand-mark" aria-hidden="true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 21c-4.5-3-8-6-8-10a4.5 4.5 0 0 1 8-2.8A4.5 4.5 0 0 1 20 11c0 4-3.5 7-8 10z"/>
                </svg>
            </span>
            <span class="brand-text">
                <span class="brand-name">PetServices</span>
                <span class="brand-sub">Plataforma de cuidado animal</span>
            </span>
        </span>
        <nav class="public-nav" aria-label="Accesos">
            <a class="public-nav-link" href="<%= request.getContextPath() %>/login">Iniciar sesión</a>
            <a class="btn btn-primary btn-sm" href="<%= request.getContextPath() %>/registro">Crear cuenta</a>
        </nav>
    </div>
</header>

<main class="public-main">

    <!-- ── Portada ── -->
    <section class="public-hero">
        <h1 class="public-title">El sistema integral de la clínica, en una sola hoja</h1>
        <div class="rule rule-rubric" aria-hidden="true"><span class="rule-tick">+</span></div>
        <p class="public-lead">
            Clientes, pasaporte de mascotas, agenda de citas veterinarias y control de
            pedidos: todo el trabajo del día, reglado y a la vista.
        </p>
        <div class="public-actions">
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/registro">Crear cuenta</a>
            <a class="btn" href="<%= request.getContextPath() %>/login">Iniciar sesión</a>
        </div>
    </section>

    <!-- ── Módulos de la plataforma ── -->
    <section class="feat-band" aria-label="Módulos de la plataforma">
        <article class="feat">
            <h2 class="feat-title">Clientes y mascotas</h2>
            <p class="feat-text">Ficha de cada cliente y pasaporte clínico de sus mascotas, con historial a la vista.</p>
        </article>
        <article class="feat">
            <h2 class="feat-title">Agenda de citas</h2>
            <p class="feat-text">Citas con su sello de estado: pendiente, confirmada o cancelada, nunca escondida.</p>
        </article>
        <article class="feat">
            <h2 class="feat-title">Servicios y catálogo</h2>
            <p class="feat-text">Tipos de servicio de la veterinaria y productos en existencia, con precios en pesos.</p>
        </article>
        <article class="feat">
            <h2 class="feat-title">Pedidos y pagos</h2>
            <p class="feat-text">Cada compra registrada desde el catálogo, con su estado de pago y su importe.</p>
        </article>
    </section>

    <!-- ── Cierre ── -->
    <section class="public-closing">
        <h2 class="public-closing-title">¿Listo para abrir el panel?</h2>
        <p class="public-closing-text">Crea tu cuenta de cliente o entra con tus credenciales para ver la agenda del día.</p>
        <div class="public-actions">
            <a class="btn btn-primary" href="<%= request.getContextPath() %>/registro">Crear cuenta</a>
            <a class="btn" href="<%= request.getContextPath() %>/login">Iniciar sesión</a>
        </div>
    </section>

</main>

<footer class="public-foot">
    <p>PetServices · 2026 · Todos los derechos reservados</p>
</footer>

</body>
</html>
