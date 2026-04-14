document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPedidos();
});

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
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensaje("Debes iniciar sesión para ver tus pedidos.");
        return;
    }

    configurarFiltroEstado(sesion.id);
    configurarCerrarDetalle();
    await cargarPedidos(sesion.id);
}

function configurarFiltroEstado(usuarioId) {
    const filtroEstado = document.getElementById("filtro-estado");

    filtroEstado.addEventListener("change", async () => {
        const estado = filtroEstado.value;

        if (estado === "TODOS") {
            await cargarPedidos(usuarioId);
        } else {
            await cargarPedidosPorEstado(usuarioId, estado);
        }
    });
}

function configurarCerrarDetalle() {
    const btnCerrar = document.getElementById("cerrar-detalle");

    if (!btnCerrar) return;

    btnCerrar.addEventListener("click", () => {
        cerrarDetallePedido();
    });
}

async function cargarPedidos(usuarioId) {
    const listaPedidos = document.getElementById("lista-pedidos");

    try {
        const response = await fetch(`${BASE_URL}/pedidos/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los pedidos");
        }

        const pedidos = await response.json();
        renderizarPedidos(pedidos);

    } catch (error) {
        console.error("Error al cargar pedidos:", error);
        listaPedidos.innerHTML = "";
        mostrarMensaje("No se pudieron cargar tus pedidos.");
    }
}

async function cargarPedidosPorEstado(usuarioId, estado) {
    const listaPedidos = document.getElementById("lista-pedidos");

    try {
        const response = await fetch(`${BASE_URL}/pedidos/usuario/${usuarioId}/estado/${estado}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los pedidos filtrados");
        }

        const pedidos = await response.json();
        renderizarPedidos(pedidos);

    } catch (error) {
        console.error("Error al filtrar pedidos:", error);
        listaPedidos.innerHTML = "";
        mostrarMensaje("No se pudieron cargar los pedidos filtrados.");
    }
}

function renderizarPedidos(pedidos) {
    const listaPedidos = document.getElementById("lista-pedidos");
    listaPedidos.innerHTML = "";

    if (!pedidos || pedidos.length === 0) {
        listaPedidos.innerHTML = `<p>No hay pedidos para mostrar.</p>`;
        cerrarDetallePedido();
        return;
    }

    pedidos.forEach(pedido => {
        const card = document.createElement("article");
        card.classList.add("pedido-card");

        const estadoClase = obtenerClaseEstado(pedido.estado);
        const fechaFormateada = formatearFecha(pedido.fechaPedido);

        card.innerHTML = `
            <div class="pedido-header">
                <h3>Pedido #${pedido.id}</h3>
                <span class="estado ${estadoClase}">${traducirEstado(pedido.estado)}</span>
            </div>

            <div class="pedido-info">
                <p><strong>Fecha:</strong> ${fechaFormateada}</p>
                <p><strong>Total:</strong> ${Number(pedido.total).toFixed(2)} €</p>
                <p><strong>Método de pago:</strong> ${traducirMetodoPago(pedido.metodoPago)}</p>
            </div>

            <div class="pedido-acciones">
                <button class="btn btn-detalle">Ver detalle</button>
                ${pedido.estado === "CONFIRMADO" ? `<button class="btn btn-cancelar">Cancelar pedido</button>` : ""}
                ${pedido.estado === "ENVIADO" ? `<button class="btn btn-entrega">Confirmar entrega</button>` : ""}
            </div>
        `;

        const btnDetalle = card.querySelector(".btn-detalle");
        btnDetalle.addEventListener("click", async () => {
            await cargarDetallePedido(pedido.id);
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

async function cargarDetallePedido(pedidoId) {
    const detalleSection = document.getElementById("detalle-pedido-section");
    const detallePedido = document.getElementById("detalle-pedido");

    try {
        const response = await fetch(`${BASE_URL}/pedidos/${pedidoId}/items`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el detalle del pedido");
        }

        const items = await response.json();
        detallePedido.innerHTML = "";

        if (!items || items.length === 0) {
            detallePedido.innerHTML = `<p>No hay productos en este pedido.</p>`;
        } else {
            items.forEach(item => {
                const producto = item.producto;
                const cantidad = item.cantidad;
                const precioUnitario = Number(item.precioUnitario);
                const subtotalLinea = precioUnitario * cantidad;

                const div = document.createElement("article");
                div.classList.add("detalle-item-card");

                div.innerHTML = `
                    <img src="${producto.urlImagen}" alt="${producto.nombre}" class="detalle-item-imagen">

                    <div class="detalle-item-info">
                        <h3>${producto.nombre}</h3>
                        <p><strong>Cantidad:</strong> ${cantidad}</p>
                        <p><strong>Precio unitario:</strong> ${precioUnitario.toFixed(2)} €</p>
                        <p class="detalle-subtotal"><strong>Subtotal:</strong> ${subtotalLinea.toFixed(2)} €</p>
                    </div>
                `;

                detallePedido.appendChild(div);
            });
        }

        detalleSection.classList.remove("oculto");

    } catch (error) {
        console.error("Error al cargar detalle:", error);
        mostrarMensaje("No se pudo cargar el detalle del pedido.");
    }
}

function cerrarDetallePedido() {
    const detalleSection = document.getElementById("detalle-pedido-section");
    const detallePedido = document.getElementById("detalle-pedido");

    detallePedido.innerHTML = "";
    detalleSection.classList.add("oculto");
}

async function cancelarPedido(pedidoId) {
    const sesion = await obtenerSesionActual();
    const filtroEstado = document.getElementById("filtro-estado").value;

    if (!sesion || !sesion.id) {
        mostrarMensaje("Debes iniciar sesión para gestionar tus pedidos.");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/pedidos/cancelar/${pedidoId}`, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cancelar el pedido");
        }

        mostrarMensaje("Pedido cancelado correctamente.", "ok");

        if (filtroEstado === "TODOS") {
            await cargarPedidos(sesion.id);
        } else {
            await cargarPedidosPorEstado(sesion.id, filtroEstado);
        }

        cerrarDetallePedido();

    } catch (error) {
        console.error("Error al cancelar pedido:", error);
        mostrarMensaje("No se pudo cancelar el pedido.");
    }
}

async function confirmarEntregaPedido(pedidoId) {
    mostrarMensaje("Todavía falta crear el endpoint backend para confirmar la entrega.");
}

function obtenerClaseEstado(estado) {
    if (estado === "CONFIRMADO") return "estado-confirmado";
    if (estado === "ENVIADO") return "estado-enviado";
    if (estado === "ENTREGADO") return "estado-entregado";
    if (estado === "CANCELADO") return "estado-cancelado";
    return "";
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
    return metodoPago;
}

function formatearFecha(fecha) {
    if (!fecha) return "";

    const fechaObj = new Date(fecha);

    return fechaObj.toLocaleDateString("es-ES", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric"
    }) + " " + fechaObj.toLocaleTimeString("es-ES", {
        hour: "2-digit",
        minute: "2-digit"
    });
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