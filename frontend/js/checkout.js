document.addEventListener("DOMContentLoaded", () => {
    iniciarCheckout();
});

let itemsCarrito = [];
let subtotalGlobal = 0;

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

async function iniciarCheckout() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensaje("Debes iniciar sesión para continuar con la compra.");
        return;
    }

    await cargarResumenCheckout(sesion.id);
    configurarMetodoEnvio();
    configurarMetodoPago();
    configurarBotonConfirmar();
}

async function cargarResumenCheckout(usuarioId) {
    const listaResumen = document.getElementById("lista-resumen-checkout");
    const subtotalCheckout = document.getElementById("subtotal-checkout");
    const envioCheckout = document.getElementById("envio-checkout");
    const totalCheckout = document.getElementById("total-checkout");

    try {
        const response = await fetch(`${BASE_URL}/carrito/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el carrito");
        }

        itemsCarrito = await response.json();

        listaResumen.innerHTML = "";
        subtotalGlobal = 0;

        if (!itemsCarrito || itemsCarrito.length === 0) {
            listaResumen.innerHTML = `<p>Tu carrito está vacío.</p>`;
            subtotalCheckout.textContent = "0.00 €";
            envioCheckout.textContent = "0.00 €";
            totalCheckout.textContent = "0.00 €";
            return;
        }

        itemsCarrito.forEach(item => {
            const producto = item.producto;
            const cantidad = item.cantidad;
            const talla = item.talla;
            const precio = Number(producto.precio);
            const subtotalItem = precio * cantidad;

            subtotalGlobal += subtotalItem;

            const div = document.createElement("div");
            div.classList.add("item-resumen-checkout");

            div.innerHTML = `
                <img src="${producto.urlImagen}" alt="${producto.nombre}">
                <div class="item-resumen-info">
                    <h3>${producto.nombre}</h3>
                    <p>Talla: ${talla}</p>
                    <p>Cantidad: ${cantidad}</p>
                    <p>${precio.toFixed(2)} € x ${cantidad}</p>
                    <p><strong>${subtotalItem.toFixed(2)} €</strong></p>
                </div>
            `;

            listaResumen.appendChild(div);
        });

        subtotalCheckout.textContent = `${subtotalGlobal.toFixed(2)} €`;

        await aplicarReglaTiendas();
        recalcularTotal();

    } catch (error) {
        console.error("Error al cargar resumen checkout:", error);
        listaResumen.innerHTML = `<p>Error al cargar el resumen del pedido.</p>`;
        subtotalCheckout.textContent = "0.00 €";
        envioCheckout.textContent = "0.00 €";
        totalCheckout.textContent = "0.00 €";
        mostrarMensaje("No se pudo cargar el resumen del pedido.");
    }
}

function configurarMetodoEnvio() {
    const radiosEnvio = document.querySelectorAll('input[name="metodoEnvio"]');

    radiosEnvio.forEach(radio => {
        radio.addEventListener("change", () => {
            actualizarBloquesEnvio();
            recalcularTotal();
        });
    });

    actualizarBloquesEnvio();
}

function actualizarBloquesEnvio() {
    const metodoSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked')?.value;

    const bloqueDomicilio = document.getElementById("bloque-domicilio");
    const bloqueTienda = document.getElementById("bloque-tienda");
    const bloquePunto = document.getElementById("bloque-punto");

    bloqueDomicilio.style.display = "none";
    bloqueTienda.style.display = "none";
    bloquePunto.style.display = "none";

    if (metodoSeleccionado === "domicilio") {
        bloqueDomicilio.style.display = "block";
    } else if (metodoSeleccionado === "tienda") {
        bloqueTienda.style.display = "block";
    } else if (metodoSeleccionado === "punto") {
        bloquePunto.style.display = "block";
    }
}

function recalcularTotal() {
    const metodoSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked')?.value;
    const envioCheckout = document.getElementById("envio-checkout");
    const totalCheckout = document.getElementById("total-checkout");

    let costeEnvio = 0;

    if (metodoSeleccionado === "tienda") {
        costeEnvio = 0;
    } else if (metodoSeleccionado === "punto") {
        costeEnvio = 2;
    } else if (metodoSeleccionado === "domicilio") {
        costeEnvio = 4;
    }

    const totalFinal = subtotalGlobal + costeEnvio;

    envioCheckout.textContent = `${costeEnvio.toFixed(2)} €`;
    totalCheckout.textContent = `${totalFinal.toFixed(2)} €`;
}

async function aplicarReglaTiendas() {
    const opcionRecogidaTienda = document.getElementById("opcion-recogida-tienda");
    const radioTienda = opcionRecogidaTienda.querySelector('input[value="tienda"]');
    const mensajeMultitienda = document.getElementById("mensaje-multitienda");
    const selectTienda = document.getElementById("select-tienda");

    const nombresTiendas = [...new Set(
        itemsCarrito
            .map(item => item.producto?.tienda?.nombre)
            .filter(Boolean)
    )];

    if (nombresTiendas.length > 1) {
        radioTienda.disabled = true;
        opcionRecogidaTienda.style.opacity = "0.5";
        mensajeMultitienda.style.display = "block";

        const radioDomicilio = document.querySelector('input[name="metodoEnvio"][value="domicilio"]');
        radioDomicilio.checked = true;
        actualizarBloquesEnvio();

        selectTienda.innerHTML = `<option value="">No disponible para pedidos de varias tiendas</option>`;
        return;
    }

    radioTienda.disabled = false;
    opcionRecogidaTienda.style.opacity = "1";
    mensajeMultitienda.style.display = "none";

    if (nombresTiendas.length === 1) {
        await cargarTiendasRecogidaVigo(nombresTiendas[0], selectTienda);
    } else {
        selectTienda.innerHTML = `<option value="">Selecciona una tienda</option>`;
    }
}

async function cargarTiendasRecogidaVigo(nombreTienda, selectTienda) {
    selectTienda.innerHTML = `<option value="">Cargando establecimientos...</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/establecimientos/tienda/${encodeURIComponent(nombreTienda)}/ciudad/Vigo`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar los establecimientos");
        }

        const establecimientos = await response.json();

        selectTienda.innerHTML = `<option value="">Selecciona una tienda</option>`;

        if (!establecimientos || establecimientos.length === 0) {
            selectTienda.innerHTML = `<option value="">No hay establecimientos disponibles en Vigo</option>`;
            return;
        }

        establecimientos.forEach(est => {
            const option = document.createElement("option");
            option.value = est.id;
            option.textContent = `${est.nombre} - ${est.direccion}`;
            selectTienda.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar establecimientos:", error);
        selectTienda.innerHTML = `<option value="">Error al cargar establecimientos</option>`;
    }
}

function configurarMetodoPago() {
    const radiosPago = document.querySelectorAll('input[name="metodoPago"]');

    radiosPago.forEach(radio => {
        radio.addEventListener("change", actualizarBloquesPago);
    });

    actualizarBloquesPago();
}

function actualizarBloquesPago() {
    const metodoPagoSeleccionado = document.querySelector('input[name="metodoPago"]:checked')?.value;

    const bloqueTarjeta = document.getElementById("bloque-tarjeta");
    const bloquePaypal = document.getElementById("bloque-paypal");
    const bloqueContraReembolso = document.getElementById("bloque-contra-reembolso");

    bloqueTarjeta.style.display = "none";
    bloquePaypal.style.display = "none";
    bloqueContraReembolso.style.display = "none";

    if (metodoPagoSeleccionado === "tarjeta") {
        bloqueTarjeta.style.display = "block";
    } else if (metodoPagoSeleccionado === "paypal") {
        bloquePaypal.style.display = "block";
    } else if (metodoPagoSeleccionado === "contra_reembolso") {
        bloqueContraReembolso.style.display = "block";
    }
}

function configurarBotonConfirmar() {
    const btnConfirmar = document.getElementById("btn-confirmar-pedido");

    if (!btnConfirmar) return;

    btnConfirmar.addEventListener("click", async () => {
        await confirmarPedido();
    });
}

async function confirmarPedido() {
    const sesion = await obtenerSesionActual();
    const metodoPagoSeleccionado = document.querySelector('input[name="metodoPago"]:checked')?.value;

    if (!sesion || !sesion.id) {
        mostrarMensaje("No se ha encontrado el usuario");
        return;
    }

    if (!metodoPagoSeleccionado) {
        mostrarMensaje("Selecciona un método de pago");
        return;
    }

    const body = {
        usuarioId: Number(sesion.id),
        metodoPago: convertirMetodoPagoBackend(metodoPagoSeleccionado)
    };

    if (metodoPagoSeleccionado === "tarjeta") {
        const numeroTarjeta = document.getElementById("numero-tarjeta").value.trim();
        const nombreTitular = document.getElementById("nombre-titular").value.trim();
        const fechaExpiracion = document.getElementById("fecha-expiracion").value.trim();
        const cvv = document.getElementById("cvv").value.trim();

        if (!numeroTarjeta || !nombreTitular || !fechaExpiracion || !cvv) {
            mostrarMensaje("Completa todos los datos de la tarjeta");
            return;
        }

        body.numeroTarjeta = numeroTarjeta;
        body.nombreTitular = nombreTitular;
        body.fechaExpiracion = fechaExpiracion;
        body.cvv = cvv;
    }

    if (metodoPagoSeleccionado === "paypal") {
        const emailPaypal = document.getElementById("email-paypal").value.trim();

        if (!emailPaypal) {
            mostrarMensaje("Introduce el email de PayPal");
            return;
        }

        body.emailPaypal = emailPaypal;
    }

    if (metodoPagoSeleccionado === "contra_reembolso") {
        const importeEntrega = document.getElementById("importe-entrega").value.trim();

        if (!importeEntrega) {
            mostrarMensaje("Indica con cuánto vas a pagar");
            return;
        }

        body.importeEntrega = Number(importeEntrega);
    }

    try {
        const response = await fetch(`${BASE_URL}/pagos/procesar`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify(body)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.mensaje || "No se pudo procesar el pago");
        }

        if (data.estado === "RECHAZADO") {
            mostrarMensaje(data.mensaje || "Pago rechazado");
            return;
        }

        mostrarMensaje(`Pedido confirmado. Referencia: ${data.referencia}`, "ok");

        setTimeout(() => {
            window.location.href = "index.html";
        }, 1500);

    } catch (error) {
        console.error("Error al confirmar pedido:", error);
        mostrarMensaje(error.message || "No se pudo confirmar el pedido");
    }
}

function convertirMetodoPagoBackend(valorFrontend) {
    if (valorFrontend === "tarjeta") return "TARJETA";
    if (valorFrontend === "paypal") return "PAYPAL";
    if (valorFrontend === "contra_reembolso") return "CONTRA_REEMBOLSO";
    return null;
}

function mostrarMensaje(texto, tipo = "error") {
    const div = document.getElementById("mensaje-checkout");

    if (!div) return;

    div.textContent = texto;
    div.classList.remove("oculto", "mensaje-error", "mensaje-ok");

    if (tipo === "ok") {
        div.classList.add("mensaje-ok");
    } else {
        div.classList.add("mensaje-error");
    }

    setTimeout(() => {
        div.classList.add("oculto");
    }, 5000);
}