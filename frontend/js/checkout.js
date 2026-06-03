document.addEventListener("DOMContentLoaded", () => {
    iniciarCheckout();
});

let itemsCarrito = [];
let subtotalGlobal = 0;
let tarjetasGuardadas = [];
let direccionesGuardadas = [];
let tiendaUnicaCarrito = null;

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
    await cargarDireccionesGuardadas(sesion.id);
    await cargarTarjetasGuardadas(sesion.id);

    configurarMetodoEnvio();
    configurarMetodoPago();
    configurarSelectsLogistica();
    configurarBotonConfirmar();
}

/* =========================
   RESUMEN CARRITO
========================= */

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
        tiendaUnicaCarrito = null;

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
            const tallaTexto = window.TallasProducto
                ? window.TallasProducto.formatearTalla(talla)
                : talla;
            const precio = Number(producto.precio);
            const subtotalItem = precio * cantidad;

            subtotalGlobal += subtotalItem;

            const div = document.createElement("div");
            div.classList.add("item-resumen-checkout");

            div.innerHTML = `
                <img src="${producto.urlImagen}" alt="${producto.nombre}">
                <div class="item-resumen-info">
                    <h3>${producto.nombre}</h3>
                    <p>Talla: ${tallaTexto}</p>
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

/* =========================
   MÉTODO DE ENVÍO
========================= */

function configurarMetodoEnvio() {
    const radiosEnvio = document.querySelectorAll('input[name="metodoEnvio"]');

    radiosEnvio.forEach(radio => {
        radio.addEventListener("change", async () => {
            actualizarBloquesEnvio();
            recalcularTotal();
            await cargarDatosInicialesSegunMetodoEnvio();
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

    const nombresTiendas = [...new Set(
        itemsCarrito
            .map(item => item.producto?.tienda?.nombre)
            .filter(Boolean)
    )];

    tiendaUnicaCarrito = nombresTiendas.length === 1 ? nombresTiendas[0] : null;

    if (nombresTiendas.length > 1) {
        radioTienda.disabled = true;
        opcionRecogidaTienda.style.opacity = "0.5";
        mensajeMultitienda.style.display = "block";

        const radioSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked');
        if (radioSeleccionado?.value === "tienda") {
            const radioDomicilio = document.querySelector('input[name="metodoEnvio"][value="domicilio"]');
            radioDomicilio.checked = true;
            actualizarBloquesEnvio();
        }

        limpiarSelectTiendasNoDisponible();
        return;
    }

    radioTienda.disabled = false;
    opcionRecogidaTienda.style.opacity = "1";
    mensajeMultitienda.style.display = "none";
}

function limpiarSelectTiendasNoDisponible() {
    const selectProvinciaTienda = document.getElementById("select-provincia-tienda");
    const selectCiudadTienda = document.getElementById("select-ciudad-tienda");
    const selectTienda = document.getElementById("select-tienda");

    if (selectProvinciaTienda) {
        selectProvinciaTienda.innerHTML = `<option value="">No disponible para pedidos de varias tiendas</option>`;
    }

    if (selectCiudadTienda) {
        selectCiudadTienda.innerHTML = `<option value="">No disponible</option>`;
        selectCiudadTienda.disabled = true;
    }

    if (selectTienda) {
        selectTienda.innerHTML = `<option value="">No disponible para pedidos de varias tiendas</option>`;
    }
}

async function cargarDatosInicialesSegunMetodoEnvio() {
    const metodoSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked')?.value;

    if (metodoSeleccionado === "tienda") {
        await cargarProvinciasEstablecimientos();
    }

    if (metodoSeleccionado === "punto") {
        await cargarProvinciasPuntosRecogida();
    }
}

/* =========================
   DIRECCIONES
========================= */

async function cargarDireccionesGuardadas(usuarioId) {
    const bloqueDirecciones = document.getElementById("bloque-direcciones-guardadas");
    const listaDirecciones = document.getElementById("lista-direcciones-guardadas");

    if (!bloqueDirecciones || !listaDirecciones) return;

    try {
        const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las direcciones");
        }

        direccionesGuardadas = await response.json();
        listaDirecciones.innerHTML = "";

        if (!direccionesGuardadas || direccionesGuardadas.length === 0) {
            bloqueDirecciones.style.display = "none";
            return;
        }

        bloqueDirecciones.style.display = "block";

        direccionesGuardadas.forEach(direccion => {
            const label = document.createElement("label");
            label.classList.add("opcion-radio");

            const descripcion = construirTextoDireccion(direccion);
            const principal = direccion.principal ? " (Principal)" : "";

            label.innerHTML = `
                <input type="radio" name="seleccionDireccion" value="${direccion.id}">
                <span>${direccion.alias || "Dirección"}${principal} - ${descripcion}</span>
            `;

            listaDirecciones.appendChild(label);
        });

        document.querySelectorAll('input[name="seleccionDireccion"]').forEach(radio => {
            radio.addEventListener("change", actualizarFormularioDireccion);
        });

        actualizarFormularioDireccion();

    } catch (error) {
        console.error("Error al cargar direcciones guardadas:", error);
        bloqueDirecciones.style.display = "none";
    }
}

function construirTextoDireccion(direccion) {
    const partes = [
        direccion.calle,
        direccion.numero,
        direccion.piso,
        direccion.puerta,
        direccion.municipio,
        direccion.provincia,
        direccion.codigoPostal
    ].filter(valor => valor && String(valor).trim() !== "");

    return partes.join(", ");
}

function actualizarFormularioDireccion() {
    const seleccionDireccion = document.querySelector('input[name="seleccionDireccion"]:checked')?.value;
    const usarNueva = !seleccionDireccion || seleccionDireccion === "nueva";

    const idsCampos = [
        "alias-direccion",
        "nombre-envio",
        "provincia-envio",
        "ciudad-envio",
        "calle-envio",
        "numero-envio",
        "piso-envio",
        "puerta-envio",
        "cp-envio"
    ];

    idsCampos.forEach(id => {
        const input = document.getElementById(id);
        if (input) input.disabled = !usarNueva;
    });

    const guardarDireccion = document.getElementById("guardar-direccion");
    const direccionPrincipal = document.getElementById("direccion-principal");
    const bloqueGuardarDireccion = document.getElementById("bloque-guardar-direccion");

    if (guardarDireccion) guardarDireccion.disabled = !usarNueva;
    if (direccionPrincipal) direccionPrincipal.disabled = !usarNueva;
    if (bloqueGuardarDireccion) bloqueGuardarDireccion.style.display = usarNueva ? "block" : "none";

    if (!usarNueva) {
        idsCampos.forEach(id => {
            const input = document.getElementById(id);
            if (input) input.value = "";
        });

        if (guardarDireccion) guardarDireccion.checked = false;
        if (direccionPrincipal) direccionPrincipal.checked = false;
    }
}

async function guardarDireccionSiProcede(usuarioId) {
    const seleccionDireccion = document.querySelector('input[name="seleccionDireccion"]:checked')?.value;
    const usarNueva = !seleccionDireccion || seleccionDireccion === "nueva";

    if (!usarNueva) {
        return Number(seleccionDireccion);
    }

    const guardarDireccion = document.getElementById("guardar-direccion")?.checked || false;

    if (!guardarDireccion) {
        return null;
    }

    const alias = document.getElementById("alias-direccion")?.value.trim();
    const provincia = document.getElementById("provincia-envio")?.value.trim();
    const ciudad = document.getElementById("ciudad-envio")?.value.trim();
    const calle = document.getElementById("calle-envio")?.value.trim();
    const numero = document.getElementById("numero-envio")?.value.trim();
    const piso = document.getElementById("piso-envio")?.value.trim();
    const puerta = document.getElementById("puerta-envio")?.value.trim();
    const codigoPostal = document.getElementById("cp-envio")?.value.trim();
    const principal = document.getElementById("direccion-principal")?.checked || false;

    if (!alias || !provincia || !ciudad || !calle || !numero || !codigoPostal) {
        throw new Error("Completa todos los campos obligatorios de la dirección si quieres guardarla");
    }

    const payload = {
        alias,
        provincia,
        municipio: ciudad,
        calle,
        numero,
        piso: piso || "",
        puerta: puerta || "",
        codigoPostal,
        principal
    };

    const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const texto = await response.text();
        throw new Error(texto || "No se pudo guardar la dirección");
    }

    const direccionGuardada = await response.json();
    return direccionGuardada.id;
}

/* =========================
   ESTABLECIMIENTOS
========================= */

function configurarSelectsLogistica() {
    const selectProvinciaTienda = document.getElementById("select-provincia-tienda");
    const selectCiudadTienda = document.getElementById("select-ciudad-tienda");

    const selectProvinciaPunto = document.getElementById("select-provincia-punto");
    const selectCiudadPunto = document.getElementById("select-ciudad-punto");

    selectProvinciaTienda?.addEventListener("change", async () => {
        await cargarCiudadesEstablecimientos();
    });

    selectCiudadTienda?.addEventListener("change", async () => {
        await cargarEstablecimientosFinales();
    });

    selectProvinciaPunto?.addEventListener("change", async () => {
        await cargarCiudadesPuntosRecogida();
    });

    selectCiudadPunto?.addEventListener("change", async () => {
        await cargarPuntosRecogidaFinales();
    });
}

async function cargarProvinciasEstablecimientos() {
    const selectProvincia = document.getElementById("select-provincia-tienda");
    const selectCiudad = document.getElementById("select-ciudad-tienda");
    const selectEstablecimiento = document.getElementById("select-tienda");

    if (!selectProvincia || !selectCiudad || !selectEstablecimiento) return;

    if (!tiendaUnicaCarrito) {
        limpiarSelectTiendasNoDisponible();
        return;
    }

    selectProvincia.innerHTML = `<option value="">Cargando provincias...</option>`;
    selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
    selectCiudad.disabled = true;
    selectEstablecimiento.innerHTML = `<option value="">Selecciona un establecimiento</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/establecimientos/tienda/${encodeURIComponent(tiendaUnicaCarrito)}/provincias`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar las provincias");
        }

        const provincias = await response.json();

        selectProvincia.innerHTML = `<option value="">Selecciona una provincia</option>`;

        if (!provincias || provincias.length === 0) {
            selectProvincia.innerHTML = `<option value="">No hay provincias disponibles</option>`;
            return;
        }

        provincias.forEach(provincia => {
            const option = document.createElement("option");
            option.value = provincia;
            option.textContent = provincia;
            selectProvincia.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar provincias de establecimientos:", error);
        selectProvincia.innerHTML = `<option value="">Error al cargar provincias</option>`;
    }
}

async function cargarCiudadesEstablecimientos() {
    const selectProvincia = document.getElementById("select-provincia-tienda");
    const selectCiudad = document.getElementById("select-ciudad-tienda");
    const selectEstablecimiento = document.getElementById("select-tienda");

    if (!selectProvincia || !selectCiudad || !selectEstablecimiento || !tiendaUnicaCarrito) return;

    const provincia = selectProvincia.value;

    selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
    selectCiudad.disabled = true;
    selectEstablecimiento.innerHTML = `<option value="">Selecciona un establecimiento</option>`;

    if (!provincia) return;

    selectCiudad.innerHTML = `<option value="">Cargando ciudades...</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/establecimientos/tienda/${encodeURIComponent(tiendaUnicaCarrito)}/provincia/${encodeURIComponent(provincia)}/ciudades`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar las ciudades");
        }

        const ciudades = await response.json();

        selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
        selectCiudad.disabled = false;

        if (!ciudades || ciudades.length === 0) {
            selectCiudad.innerHTML = `<option value="">No hay ciudades disponibles</option>`;
            selectCiudad.disabled = true;
            return;
        }

        ciudades.forEach(ciudad => {
            const option = document.createElement("option");
            option.value = ciudad;
            option.textContent = ciudad;
            selectCiudad.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar ciudades de establecimientos:", error);
        selectCiudad.innerHTML = `<option value="">Error al cargar ciudades</option>`;
        selectCiudad.disabled = true;
    }
}

async function cargarEstablecimientosFinales() {
    const selectProvincia = document.getElementById("select-provincia-tienda");
    const selectCiudad = document.getElementById("select-ciudad-tienda");
    const selectEstablecimiento = document.getElementById("select-tienda");

    if (!selectProvincia || !selectCiudad || !selectEstablecimiento || !tiendaUnicaCarrito) return;

    const provincia = selectProvincia.value;
    const ciudad = selectCiudad.value;

    selectEstablecimiento.innerHTML = `<option value="">Selecciona un establecimiento</option>`;

    if (!provincia || !ciudad) return;

    selectEstablecimiento.innerHTML = `<option value="">Cargando establecimientos...</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/establecimientos/tienda/${encodeURIComponent(tiendaUnicaCarrito)}/provincia/${encodeURIComponent(provincia)}/ciudad/${encodeURIComponent(ciudad)}`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar los establecimientos");
        }

        const establecimientos = await response.json();

        selectEstablecimiento.innerHTML = `<option value="">Selecciona un establecimiento</option>`;

        if (!establecimientos || establecimientos.length === 0) {
            selectEstablecimiento.innerHTML = `<option value="">No hay establecimientos disponibles</option>`;
            return;
        }

        establecimientos.forEach(est => {
            const option = document.createElement("option");
            option.value = est.id;
            option.textContent = `${est.nombre} - ${est.direccion}`;
            selectEstablecimiento.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar establecimientos finales:", error);
        selectEstablecimiento.innerHTML = `<option value="">Error al cargar establecimientos</option>`;
    }
}

/* =========================
   PUNTOS DE RECOGIDA
========================= */

async function cargarProvinciasPuntosRecogida() {
    const selectProvincia = document.getElementById("select-provincia-punto");
    const selectCiudad = document.getElementById("select-ciudad-punto");
    const selectPunto = document.getElementById("select-punto-recogida");

    if (!selectProvincia || !selectCiudad || !selectPunto) return;

    selectProvincia.innerHTML = `<option value="">Cargando provincias...</option>`;
    selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
    selectCiudad.disabled = true;
    selectPunto.innerHTML = `<option value="">Selecciona un punto de recogida</option>`;

    try {
        const response = await fetch(`${BASE_URL}/puntos-recogida/disponibles/provincias`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las provincias");
        }

        const provincias = await response.json();

        selectProvincia.innerHTML = `<option value="">Selecciona una provincia</option>`;

        if (!provincias || provincias.length === 0) {
            selectProvincia.innerHTML = `<option value="">No hay provincias disponibles</option>`;
            return;
        }

        provincias.forEach(provincia => {
            const option = document.createElement("option");
            option.value = provincia;
            option.textContent = provincia;
            selectProvincia.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar provincias de puntos de recogida:", error);
        selectProvincia.innerHTML = `<option value="">Error al cargar provincias</option>`;
    }
}

async function cargarCiudadesPuntosRecogida() {
    const selectProvincia = document.getElementById("select-provincia-punto");
    const selectCiudad = document.getElementById("select-ciudad-punto");
    const selectPunto = document.getElementById("select-punto-recogida");

    if (!selectProvincia || !selectCiudad || !selectPunto) return;

    const provincia = selectProvincia.value;

    selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
    selectCiudad.disabled = true;
    selectPunto.innerHTML = `<option value="">Selecciona un punto de recogida</option>`;

    if (!provincia) return;

    selectCiudad.innerHTML = `<option value="">Cargando ciudades...</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/puntos-recogida/disponibles/provincia/${encodeURIComponent(provincia)}/ciudades`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar las ciudades");
        }

        const ciudades = await response.json();

        selectCiudad.innerHTML = `<option value="">Selecciona una ciudad</option>`;
        selectCiudad.disabled = false;

        if (!ciudades || ciudades.length === 0) {
            selectCiudad.innerHTML = `<option value="">No hay ciudades disponibles</option>`;
            selectCiudad.disabled = true;
            return;
        }

        ciudades.forEach(ciudad => {
            const option = document.createElement("option");
            option.value = ciudad;
            option.textContent = ciudad;
            selectCiudad.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar ciudades de puntos de recogida:", error);
        selectCiudad.innerHTML = `<option value="">Error al cargar ciudades</option>`;
        selectCiudad.disabled = true;
    }
}

async function cargarPuntosRecogidaFinales() {
    const selectProvincia = document.getElementById("select-provincia-punto");
    const selectCiudad = document.getElementById("select-ciudad-punto");
    const selectPunto = document.getElementById("select-punto-recogida");

    if (!selectProvincia || !selectCiudad || !selectPunto) return;

    const provincia = selectProvincia.value;
    const ciudad = selectCiudad.value;

    selectPunto.innerHTML = `<option value="">Selecciona un punto de recogida</option>`;

    if (!provincia || !ciudad) return;

    selectPunto.innerHTML = `<option value="">Cargando puntos de recogida...</option>`;

    try {
        const response = await fetch(
            `${BASE_URL}/puntos-recogida/disponibles/provincia/${encodeURIComponent(provincia)}/ciudad/${encodeURIComponent(ciudad)}`,
            {
                method: "GET",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudieron cargar los puntos de recogida");
        }

        const puntos = await response.json();

        selectPunto.innerHTML = `<option value="">Selecciona un punto de recogida</option>`;

        if (!puntos || puntos.length === 0) {
            selectPunto.innerHTML = `<option value="">No hay puntos disponibles</option>`;
            return;
        }

        puntos.forEach(punto => {
            const option = document.createElement("option");
            option.value = punto.id;
            option.textContent = `${punto.nombre} - ${punto.direccion}`;
            selectPunto.appendChild(option);
        });

    } catch (error) {
        console.error("Error al cargar puntos de recogida finales:", error);
        selectPunto.innerHTML = `<option value="">Error al cargar puntos de recogida</option>`;
    }
}

/* =========================
   TARJETAS
========================= */

async function cargarTarjetasGuardadas(usuarioId) {
    const bloqueTarjetas = document.getElementById("bloque-tarjetas-guardadas");
    const listaTarjetas = document.getElementById("lista-tarjetas-guardadas");

    if (!bloqueTarjetas || !listaTarjetas) return;

    try {
        const response = await fetch(`${BASE_URL}/tarjetas/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las tarjetas");
        }

        tarjetasGuardadas = await response.json();
        listaTarjetas.innerHTML = "";

        if (!tarjetasGuardadas || tarjetasGuardadas.length === 0) {
            bloqueTarjetas.style.display = "none";
            return;
        }

        bloqueTarjetas.style.display = "block";

        tarjetasGuardadas.forEach(tarjeta => {
            const label = document.createElement("label");
            label.classList.add("opcion-radio");

            label.innerHTML = `
                <input type="radio" name="seleccionTarjeta" value="${tarjeta.id}">
                <span>${tarjeta.tipo} - ${tarjeta.numeroEnmascarado} - ${tarjeta.titular} - ${tarjeta.fechaExpiracion}</span>
            `;

            listaTarjetas.appendChild(label);
        });

        document.querySelectorAll('input[name="seleccionTarjeta"]').forEach(radio => {
            radio.addEventListener("change", actualizarFormularioTarjeta);
        });

        actualizarFormularioTarjeta();

    } catch (error) {
        console.error("Error al cargar tarjetas guardadas:", error);
        bloqueTarjetas.style.display = "none";
    }
}

function actualizarFormularioTarjeta() {
    const seleccionTarjeta = document.querySelector('input[name="seleccionTarjeta"]:checked')?.value;

    const numeroTarjeta = document.getElementById("numero-tarjeta");
    const nombreTitular = document.getElementById("nombre-titular");
    const fechaExpiracion = document.getElementById("fecha-expiracion");
    const cvv = document.getElementById("cvv");
    const tipoTarjeta = document.getElementById("tipo-tarjeta");
    const guardarTarjeta = document.getElementById("guardar-tarjeta");
    const bloqueGuardarTarjeta = document.getElementById("bloque-guardar-tarjeta");

    const usarNueva = !seleccionTarjeta || seleccionTarjeta === "nueva";

    numeroTarjeta.disabled = !usarNueva;
    nombreTitular.disabled = !usarNueva;
    fechaExpiracion.disabled = !usarNueva;
    cvv.disabled = !usarNueva;

    if (tipoTarjeta) tipoTarjeta.disabled = !usarNueva;
    if (guardarTarjeta) guardarTarjeta.disabled = !usarNueva;
    if (bloqueGuardarTarjeta) bloqueGuardarTarjeta.style.display = usarNueva ? "block" : "none";

    if (!usarNueva) {
        numeroTarjeta.value = "";
        nombreTitular.value = "";
        fechaExpiracion.value = "";
        cvv.value = "";
        if (tipoTarjeta) tipoTarjeta.value = "";
        if (guardarTarjeta) guardarTarjeta.checked = false;
    }
}

/* =========================
   MÉTODO DE PAGO
========================= */

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
        actualizarFormularioTarjeta();
    } else if (metodoPagoSeleccionado === "paypal") {
        bloquePaypal.style.display = "block";
    } else if (metodoPagoSeleccionado === "contra_reembolso") {
        bloqueContraReembolso.style.display = "block";
    }
}

/* =========================
   CONFIRMAR PEDIDO
========================= */

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
    const metodoEnvioSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked')?.value;

    if (!sesion || !sesion.id) {
        mostrarMensaje("No se ha encontrado el usuario");
        return;
    }

    if (!metodoPagoSeleccionado) {
        mostrarMensaje("Selecciona un método de pago");
        return;
    }

    if (!metodoEnvioSeleccionado) {
        mostrarMensaje("Selecciona un método de envío");
        return;
    }

    try {
        const datosEntrega = await validarYObtenerDatosEntrega(sesion.id);

        const body = {
            usuarioId: Number(sesion.id),
            metodoPago: convertirMetodoPagoBackend(metodoPagoSeleccionado),
            metodoEntrega: datosEntrega.metodoEntrega,
            direccionId: datosEntrega.direccionId,
            establecimientoId: datosEntrega.establecimientoId,
            puntoRecogidaId: datosEntrega.puntoRecogidaId
        };

        if (metodoPagoSeleccionado === "tarjeta") {
            const seleccionTarjeta = document.querySelector('input[name="seleccionTarjeta"]:checked')?.value;

            if (seleccionTarjeta && seleccionTarjeta !== "nueva") {
                body.tarjetaId = Number(seleccionTarjeta);
            } else {
                const numeroTarjeta = document.getElementById("numero-tarjeta").value.trim();
                const nombreTitular = document.getElementById("nombre-titular").value.trim();
                const fechaExpiracion = document.getElementById("fecha-expiracion").value.trim();
                const cvv = document.getElementById("cvv").value.trim();
                const guardarTarjeta = document.getElementById("guardar-tarjeta")?.checked || false;
                const tipoTarjeta = document.getElementById("tipo-tarjeta")?.value || "";

                if (!numeroTarjeta || !nombreTitular || !fechaExpiracion || !cvv) {
                    mostrarMensaje("Completa todos los datos de la tarjeta");
                    return;
                }

                if (guardarTarjeta && !tipoTarjeta) {
                    mostrarMensaje("Selecciona el tipo de tarjeta si quieres guardarla");
                    return;
                }

                body.numeroTarjeta = numeroTarjeta;
                body.nombreTitular = nombreTitular;
                body.fechaExpiracion = fechaExpiracion;
                body.cvv = cvv;
                body.guardarTarjeta = guardarTarjeta;

                if (guardarTarjeta) {
                    body.tipoTarjeta = tipoTarjeta;
                }
            }
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

async function validarYObtenerDatosEntrega(usuarioId) {
    const metodoEnvioSeleccionado = document.querySelector('input[name="metodoEnvio"]:checked')?.value;

    if (metodoEnvioSeleccionado === "domicilio") {
        const seleccionDireccion = document.querySelector('input[name="seleccionDireccion"]:checked')?.value;
        const usarNueva = !seleccionDireccion || seleccionDireccion === "nueva";

        if (!usarNueva) {
            return {
                metodoEntrega: "DOMICILIO",
                direccionId: Number(seleccionDireccion),
                establecimientoId: null,
                puntoRecogidaId: null
            };
        }

        const nombre = document.getElementById("nombre-envio")?.value.trim();
        const alias = document.getElementById("alias-direccion")?.value.trim();
        const provincia = document.getElementById("provincia-envio")?.value.trim();
        const ciudad = document.getElementById("ciudad-envio")?.value.trim();
        const calle = document.getElementById("calle-envio")?.value.trim();
        const numero = document.getElementById("numero-envio")?.value.trim();
        const cp = document.getElementById("cp-envio")?.value.trim();

        if (!nombre || !alias || !provincia || !ciudad || !calle || !numero || !cp) {
            throw new Error("Completa todos los campos obligatorios del envío a domicilio");
        }

        const direccionId = await guardarDireccionSiProcede(usuarioId);

        if (!direccionId) {
            throw new Error("Para envío a domicilio, de momento debes guardar la dirección para poder usarla en el pedido");
        }

        return {
            metodoEntrega: "DOMICILIO",
            direccionId,
            establecimientoId: null,
            puntoRecogidaId: null
        };
    }

    if (metodoEnvioSeleccionado === "tienda") {
        const establecimientoId = document.getElementById("select-tienda")?.value;

        if (!establecimientoId) {
            throw new Error("Selecciona un establecimiento para la recogida en tienda");
        }

        return {
            metodoEntrega: "RECOGIDA_TIENDA",
            direccionId: null,
            establecimientoId: Number(establecimientoId),
            puntoRecogidaId: null
        };
    }

    if (metodoEnvioSeleccionado === "punto") {
        const puntoRecogidaId = document.getElementById("select-punto-recogida")?.value;

        if (!puntoRecogidaId) {
            throw new Error("Selecciona un punto de recogida");
        }

        return {
            metodoEntrega: "PUNTO_RECOGIDA",
            direccionId: null,
            establecimientoId: null,
            puntoRecogidaId: Number(puntoRecogidaId)
        };
    }

    throw new Error("Selecciona un método de envío válido");
}

/* =========================
   HELPERS
========================= */

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
