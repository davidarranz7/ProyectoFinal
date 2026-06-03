document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPedidos();
});

let sesionActual = null;
let pedidosCache = [];
let pedidoSeleccionadoId = null;

async function obtenerSesionActual() {
    try {
        const response = await fetch(`${BASE_URL}/auth/session`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            return null;
        }

        return await response.json();
    } catch (error) {
        console.error("Error al comprobar sesión:", error);
        return null;
    }
}

async function iniciarPaginaPedidos() {
    sesionActual = await obtenerSesionActual();

    if (!sesionActual || !sesionActual.id) {
        mostrarMensaje("Debes iniciar sesión para ver tus pedidos.");
        renderizarListaVaciaSesion();
        return;
    }

    configurarFiltroEstado();
    configurarCerrarDetalle();
    await cargarPedidosIniciales();
}

function configurarFiltroEstado() {
    const filtroEstado = document.getElementById("filtro-estado");

    filtroEstado.addEventListener("change", async () => {
        await cargarPedidosIniciales();
    });
}

function configurarCerrarDetalle() {
    const btnCerrar = document.getElementById("cerrar-detalle");

    if (!btnCerrar) return;

    btnCerrar.addEventListener("click", () => {
        cerrarDetallePedido();
        desmarcarPedidoActivo();
    });
}

async function cargarPedidosIniciales() {
    const filtroEstado = document.getElementById("filtro-estado").value;
    const listaPedidos = document.getElementById("lista-pedidos");

    listaPedidos.innerHTML = `<div class="loading-card">Cargando pedidos...</div>`;
    cerrarDetallePedido();
    desmarcarPedidoActivo();

    try {
        let pedidos = [];

        if (filtroEstado === "TODOS") {
            pedidos = await obtenerPedidos(sesionActual.id);
        } else {
            pedidos = await obtenerPedidosPorEstado(sesionActual.id, filtroEstado);
        }

        const pedidosEnriquecidos = await enriquecerPedidosConItems(pedidos);
        pedidosCache = pedidosEnriquecidos;

        renderizarPedidos(pedidosEnriquecidos);
        actualizarStatPill(pedidosEnriquecidos);
    } catch (error) {
        console.error("Error al cargar pedidos:", error);
        listaPedidos.innerHTML = "";
        actualizarStatPill([]);
        mostrarMensaje("No se pudieron cargar tus pedidos.");
    }
}

async function obtenerPedidos(usuarioId) {
    const response = await fetch(`${BASE_URL}/pedidos/usuario/${usuarioId}`, {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error("No se pudieron cargar los pedidos");
    }

    return await response.json();
}

async function obtenerPedidosPorEstado(usuarioId, estado) {
    const response = await fetch(`${BASE_URL}/pedidos/usuario/${usuarioId}/estado/${estado}`, {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error("No se pudieron cargar los pedidos filtrados");
    }

    return await response.json();
}

async function enriquecerPedidosConItems(pedidos) {
    const pedidosEnriquecidos = await Promise.all(
        pedidos.map(async (pedido) => {
            try {
                const items = await obtenerItemsPedido(pedido.id);

                const totalArticulos = items.reduce((acc, item) => acc + (item.cantidad || 0), 0);

                return {
                    ...pedido,
                    items,
                    totalArticulos
                };
            } catch (error) {
                console.error(`Error al cargar items del pedido ${pedido.id}:`, error);
                return {
                    ...pedido,
                    items: [],
                    totalArticulos: 0
                };
            }
        })
    );

    return pedidosEnriquecidos;
}

async function obtenerItemsPedido(pedidoId) {
    const response = await fetch(`${BASE_URL}/pedidos/${pedidoId}/items`, {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error("No se pudo cargar el detalle del pedido");
    }

    return await response.json();
}

function renderizarPedidos(pedidos) {
    const listaPedidos = document.getElementById("lista-pedidos");
    listaPedidos.innerHTML = "";

    if (!pedidos || pedidos.length === 0) {
        listaPedidos.innerHTML = `
            <div class="loading-card text-center">
                No hay pedidos para mostrar.
            </div>
        `;
        return;
    }

    pedidos.forEach((pedido) => {
        const card = document.createElement("article");
        card.className = "order-card";
        card.dataset.pedidoId = pedido.id;

        const badgeClass = obtenerBadgeEstado(pedido.estado);
        const fechaFormateada = formatearFecha(pedido.fechaPedido);
        const progreso = obtenerProgresoEstado(pedido.estado);
        const textoProgreso = obtenerTextoProgreso(pedido.estado);
        const totalArticulosTexto = pedido.totalArticulos === 1
            ? "1 artículo"
            : `${pedido.totalArticulos} artículos`;

        card.innerHTML = `
            <div class="order-top">
                <div class="order-title">
                    <h3>Pedido #${pedido.id}</h3>
                    <div class="order-sub">Compra realizada el ${fechaFormateada}</div>
                </div>
                <span class="badge ${badgeClass}">${traducirEstado(pedido.estado)}</span>
            </div>

            <div class="order-grid">
                <div class="metric">
                    <span class="label">Productos</span>
                    <span class="value">${totalArticulosTexto}</span>
                </div>

                <div class="metric featured">
                    <span class="label">Total</span>
                    <span class="value">${formatearPrecio(pedido.total)}</span>
                </div>

                <div class="metric">
                    <span class="label">Pago</span>
                    <span class="value">${traducirMetodoPago(pedido.metodoPago)}</span>
                </div>

                <div class="metric">
                    <span class="label">Estado</span>
                    <span class="value">${traducirEstado(pedido.estado)}</span>
                </div>
            </div>

            <div class="progress-block">
                <div class="progress-top">
                    <span>Progreso del pedido</span>
                    <span>${textoProgreso}</span>
                </div>
                <div class="progress">
                    <div class="bar" style="width: ${progreso}%;"></div>
                </div>
            </div>

            <div class="order-actions">
                <button class="btn btn-dark btn-detalle">Ver detalle</button>
                ${pedido.estado === "CONFIRMADO" ? `<button class="btn btn-danger btn-cancelar">Cancelar pedido</button>` : ""}
                ${pedido.estado === "ENVIADO" ? `<button class="btn btn-success btn-entrega">Confirmar entrega</button>` : ""}
            </div>
        `;

        const btnDetalle = card.querySelector(".btn-detalle");
        btnDetalle.addEventListener("click", async () => {
            await seleccionarPedido(pedido.id);
        });

        const btnCancelar = card.querySelector(".btn-cancelar");
        if (btnCancelar) {
            btnCancelar.addEventListener("click", async () => {
                await cancelarPedido(pedido.id);
            });
        }

        const btnEntrega = card.querySelector(".btn-entrega");
        if (btnEntrega) {
            btnEntrega.addEventListener("click", async () => {
                await confirmarEntregaPedido(pedido.id);
            });
        }

        listaPedidos.appendChild(card);
    });
}

async function seleccionarPedido(pedidoId) {
    pedidoSeleccionadoId = pedidoId;
    marcarPedidoActivo(pedidoId);

    let pedido = pedidosCache.find(p => Number(p.id) === Number(pedidoId));

    if (!pedido) return;

    try {
        if (!pedido.items || pedido.items.length === 0) {
            const items = await obtenerItemsPedido(pedidoId);
            pedido.items = items;
            pedido.totalArticulos = items.reduce((acc, item) => acc + (item.cantidad || 0), 0);
        }

        renderizarDetallePedido(pedido);
    } catch (error) {
        console.error("Error al seleccionar pedido:", error);
        mostrarMensaje("No se pudo cargar el detalle del pedido.");
    }
}

function renderizarDetallePedido(pedido) {
    const detalleSection = document.getElementById("detalle-pedido-section");
    const detalleVacio = document.getElementById("detalle-vacio");
    const detallePedido = document.getElementById("detalle-pedido");

    detalleVacio.classList.add("oculto");
    detalleSection.classList.remove("oculto");

    document.getElementById("detalle-titulo").textContent = `Detalle #${pedido.id}`;
    document.getElementById("detalle-estado").textContent = traducirEstado(pedido.estado);
    document.getElementById("detalle-pago").textContent = traducirMetodoPago(pedido.metodoPago);
    document.getElementById("detalle-fecha").textContent = formatearFecha(pedido.fechaPedido);
    document.getElementById("detalle-referencia").textContent = `MODA-${new Date().getFullYear()}-${pedido.id}`;

    detallePedido.innerHTML = "";

    if (!pedido.items || pedido.items.length === 0) {
        detallePedido.innerHTML = `
            <div class="loading-card text-center">
                No hay productos en este pedido.
            </div>
        `;
    } else {
        pedido.items.forEach((item) => {
            const producto = item.producto || {};
            const cantidad = item.cantidad || 0;
            const precioUnitario = Number(item.precioUnitario || 0);
            const subtotalLinea = precioUnitario * cantidad;
            const tallaTexto = window.TallasProducto
                ? window.TallasProducto.formatearTalla(item.talla)
                : item.talla;
            const talla = item.talla ? `Talla ${tallaTexto}` : "Talla no indicada";
            const tienda = producto.tienda?.nombre || "Tienda";
            const descripcion = producto.descripcion || "Producto comprado en tu pedido.";

            const article = document.createElement("article");
            article.className = "item";

            article.innerHTML = `
                <img src="${producto.urlImagen || 'https://via.placeholder.com/300x400?text=Producto'}" alt="${producto.nombre || 'Producto'}" />

                <div class="item-info">
                    <h3>${producto.nombre || "Producto"}</h3>

                    <div class="item-meta">
                        <span class="chip">${talla}</span>
                        <span class="chip">Cantidad ${cantidad}</span>
                        <span class="chip">${tienda}</span>
                    </div>

                    <p class="item-desc">${descripcion}</p>
                </div>

                <div class="item-price">
                    <div class="unit">Precio unitario</div>
                    <div class="subtotal">${formatearPrecio(precioUnitario)}</div>
                    <div class="unit" style="margin-top: 10px;">Subtotal línea</div>
                    <div class="subtotal">${formatearPrecio(subtotalLinea)}</div>
                </div>
            `;

            detallePedido.appendChild(article);
        });
    }

    const subtotal = Number(pedido.total || 0);

    document.getElementById("resumen-subtotal").textContent = formatearPrecio(subtotal);
    document.getElementById("resumen-envio").textContent = "Gratis";
    document.getElementById("resumen-descuento").textContent = "0,00 €";
    document.getElementById("resumen-total").textContent = formatearPrecio(subtotal);
}

function cerrarDetallePedido() {
    const detalleSection = document.getElementById("detalle-pedido-section");
    const detalleVacio = document.getElementById("detalle-vacio");
    const detallePedido = document.getElementById("detalle-pedido");

    detallePedido.innerHTML = "";
    detalleSection.classList.add("oculto");
    detalleVacio.classList.remove("oculto");

    pedidoSeleccionadoId = null;
}

function marcarPedidoActivo(pedidoId) {
    document.querySelectorAll(".order-card").forEach(card => {
        card.classList.remove("active");
    });

    const card = document.querySelector(`.order-card[data-pedido-id="${pedidoId}"]`);
    if (card) {
        card.classList.add("active");
    }
}

function desmarcarPedidoActivo() {
    document.querySelectorAll(".order-card").forEach(card => {
        card.classList.remove("active");
    });
}

async function cancelarPedido(pedidoId) {
    const filtroEstado = document.getElementById("filtro-estado").value;

    try {
        const response = await fetch(`${BASE_URL}/pedidos/cancelar/${pedidoId}`, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cancelar el pedido");
        }

        mostrarMensaje("Pedido cancelado correctamente.", "ok");

        if (Number(pedidoSeleccionadoId) === Number(pedidoId)) {
            cerrarDetallePedido();
            desmarcarPedidoActivo();
        }

        await cargarPedidosIniciales();

        if (filtroEstado !== "TODOS") {
            document.getElementById("filtro-estado").value = filtroEstado;
        }

    } catch (error) {
        console.error("Error al cancelar pedido:", error);
        mostrarMensaje("No se pudo cancelar el pedido.");
    }
}

async function confirmarEntregaPedido(pedidoId) {
    try {
        const response = await fetch(`${BASE_URL}/pedidos/entregar/${pedidoId}`, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("El endpoint no existe o falló");
        }

        mostrarMensaje("Entrega confirmada correctamente.", "ok");

        if (Number(pedidoSeleccionadoId) === Number(pedidoId)) {
            cerrarDetallePedido();
            desmarcarPedidoActivo();
        }

        await cargarPedidosIniciales();
    } catch (error) {
        console.error("Error al confirmar entrega:", error);
        mostrarMensaje("Aún te falta crear el endpoint backend para confirmar la entrega.");
    }
}

function actualizarStatPill(pedidos) {
    const statPill = document.getElementById("stat-pill");
    const totalPedidos = pedidos.length;
    const totalArticulos = pedidos.reduce((acc, pedido) => acc + (pedido.totalArticulos || 0), 0);

    const textoPedidos = totalPedidos === 1 ? "1 pedido" : `${totalPedidos} pedidos`;
    const textoArticulos = totalArticulos === 1 ? "1 artículo" : `${totalArticulos} artículos`;

    statPill.textContent = `${textoPedidos} · ${textoArticulos}`;
}

function renderizarListaVaciaSesion() {
    const listaPedidos = document.getElementById("lista-pedidos");
    actualizarStatPill([]);
    listaPedidos.innerHTML = `
        <div class="loading-card text-center">
            Debes iniciar sesión para ver tus pedidos.
        </div>
    `;
}

function obtenerBadgeEstado(estado) {
    if (estado === "CONFIRMADO") return "badge-confirm";
    if (estado === "ENVIADO") return "badge-ship";
    if (estado === "ENTREGADO") return "badge-ok";
    if (estado === "CANCELADO") return "badge-cancel";
    return "badge-confirm";
}

function obtenerProgresoEstado(estado) {
    if (estado === "CONFIRMADO") return 35;
    if (estado === "ENVIADO") return 70;
    if (estado === "ENTREGADO") return 100;
    if (estado === "CANCELADO") return 18;
    return 0;
}

function obtenerTextoProgreso(estado) {
    if (estado === "CONFIRMADO") return "Preparando pedido";
    if (estado === "ENVIADO") return "En camino";
    if (estado === "ENTREGADO") return "Completado";
    if (estado === "CANCELADO") return "Proceso detenido";
    return "Sin información";
}

function traducirEstado(estado) {
    if (estado === "CONFIRMADO") return "Confirmado";
    if (estado === "ENVIADO") return "Enviado";
    if (estado === "ENTREGADO") return "Entregado";
    if (estado === "CANCELADO") return "Cancelado";
    return estado;
}

function traducirMetodoPago(metodoPago) {
    if (metodoPago === "TARJETA") return "Tarjeta";
    if (metodoPago === "PAYPAL") return "PayPal";
    if (metodoPago === "CONTRA_REEMBOLSO") return "Contra reembolso";
    return metodoPago || "No indicado";
}

function formatearFecha(fecha) {
    if (!fecha) return "No disponible";

    const fechaObj = new Date(fecha);

    return fechaObj.toLocaleDateString("es-ES", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    }) + " · " + fechaObj.toLocaleTimeString("es-ES", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatearPrecio(valor) {
    return `${Number(valor || 0).toFixed(2)} €`;
}

function mostrarMensaje(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-pedidos");

    mensaje.textContent = texto;
    mensaje.style.display = "block";

    if (tipo === "ok") {
        mensaje.style.backgroundColor = "#e6ffe6";
        mensaje.style.color = "#006600";
        mensaje.style.border = "1px solid #b9e2b9";
    } else {
        mensaje.style.backgroundColor = "#fff3f4";
        mensaje.style.color = "#b00020";
        mensaje.style.border = "1px solid #f1c7cd";
    }

    setTimeout(() => {
        mensaje.style.display = "none";
    }, 4000);
}
