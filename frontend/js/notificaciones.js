document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaNotificaciones();
});

let timeoutMensajeNotificaciones = null;

async function iniciarPaginaNotificaciones() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensajeNotificaciones("Debes iniciar sesion para ver tus notificaciones.", "error");
        renderizarEstadoVacio("Inicia sesion para consultar tus avisos.");
        return;
    }

    configurarAcciones();
    await cargarNotificaciones();
}

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
        console.error("Error al comprobar sesion:", error);
        return null;
    }
}

function configurarAcciones() {
    const btnMarcarTodas = document.getElementById("btn-marcar-todas");

    if (btnMarcarTodas) {
        btnMarcarTodas.addEventListener("click", marcarTodasComoLeidas);
    }
}

async function cargarNotificaciones() {
    const contenedor = document.getElementById("contenedor-notificaciones");
    const resumen = document.getElementById("resumen-no-leidas");

    if (!contenedor) return;

    try {
        const response = await fetch(`${BASE_URL}/notificaciones/mias?limit=50`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las notificaciones.");
        }

        const notificaciones = await response.json();
        const totalNoLeidas = Array.isArray(notificaciones)
            ? notificaciones.filter((item) => item && item.leida === false).length
            : 0;

        if (resumen) {
            resumen.textContent = `${totalNoLeidas} sin leer`;
        }

        renderizarNotificaciones(notificaciones);

        if (typeof window.actualizarContadorNotificaciones === "function") {
            await window.actualizarContadorNotificaciones();
        }
    } catch (error) {
        console.error("Error cargando notificaciones:", error);
        renderizarEstadoVacio("No se pudieron cargar tus notificaciones.");
        mostrarMensajeNotificaciones(error.message || "No se pudieron cargar las notificaciones.", "error");
    }
}

function renderizarNotificaciones(notificaciones) {
    const contenedor = document.getElementById("contenedor-notificaciones");
    if (!contenedor) return;

    limpiarContenedor(contenedor);

    if (!Array.isArray(notificaciones) || notificaciones.length === 0) {
        renderizarEstadoVacio("Todavia no tienes avisos de cambios de precio.");
        return;
    }

    notificaciones.forEach((notificacion) => {
        contenedor.appendChild(crearNotificacionCard(notificacion));
    });
}

function crearNotificacionCard(notificacion) {
    const card = document.createElement("article");
    card.className = `notificacion-item ${notificacion?.leida ? "" : "no-leida"}`.trim();

    const header = document.createElement("div");
    header.className = "notificacion-header";

    const info = document.createElement("div");
    const titulo = document.createElement("h3");
    titulo.textContent = notificacion?.titulo || "Notificacion";
    const fecha = document.createElement("p");
    fecha.textContent = formatearFecha(notificacion?.fechaCreacion);
    info.append(titulo, fecha);

    const badge = document.createElement("span");
    badge.className = `notificacion-badge ${obtenerClaseBadgeNotificacion(notificacion)}`;
    badge.textContent = formatearBadgeNotificacion(notificacion);

    header.append(info, badge);
    card.appendChild(header);

    const mensaje = document.createElement("p");
    mensaje.className = "notificacion-mensaje";
    mensaje.textContent = notificacion?.mensaje || "Tienes una actualizacion.";
    card.appendChild(mensaje);

    const meta = document.createElement("div");
    meta.className = "notificacion-meta";
    meta.appendChild(crearChipMeta(formatearCambioPrecio(notificacion)));

    if (Number.isFinite(Number(notificacion?.porcentajeDescuentoNuevo))) {
        meta.appendChild(crearChipMeta(`Descuento ${Number(notificacion.porcentajeDescuentoNuevo)}%`));
    }

    if (notificacion?.rebajaMayor) {
        meta.appendChild(crearChipMeta("Rebaja mayor"));
    }

    card.appendChild(meta);

    const acciones = document.createElement("div");
    acciones.className = "notificacion-acciones";

    if (notificacion?.urlDestino) {
        const linkProducto = document.createElement("a");
        linkProducto.className = "notificacion-link";
        linkProducto.href = notificacion.urlDestino;
        linkProducto.textContent = "Ver producto";
        acciones.appendChild(linkProducto);
    }

    if (notificacion?.urlProductoOriginal) {
        const linkOriginal = document.createElement("a");
        linkOriginal.className = "notificacion-link secundario";
        linkOriginal.href = notificacion.urlProductoOriginal;
        linkOriginal.target = "_blank";
        linkOriginal.rel = "noopener noreferrer";
        linkOriginal.textContent = "Tienda original";
        acciones.appendChild(linkOriginal);
    }

    if (notificacion?.leida === false) {
        const btnLeer = document.createElement("button");
        btnLeer.type = "button";
        btnLeer.className = "notificacion-btn";
        btnLeer.textContent = "Marcar como leida";
        btnLeer.addEventListener("click", async () => {
            await marcarComoLeida(notificacion.id);
        });
        acciones.appendChild(btnLeer);
    }

    card.appendChild(acciones);
    return card;
}

function crearChipMeta(texto) {
    const chip = document.createElement("span");
    chip.textContent = texto;
    return chip;
}

function formatearCambioPrecio(notificacion) {
    const anterior = formatearImporte(notificacion?.precioAnterior);
    const nuevo = formatearImporte(notificacion?.precioNuevo);
    return `${anterior} -> ${nuevo}`;
}

function formatearImporte(valor) {
    if (valor === null || valor === undefined || valor === "") {
        return "-";
    }

    return `${valor} EUR`;
}

function formatearFecha(fecha) {
    if (!fecha) {
        return "Sin fecha";
    }

    const date = new Date(fecha);

    if (Number.isNaN(date.getTime())) {
        return fecha;
    }

    return date.toLocaleString("es-ES", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

function obtenerClaseBadgeNotificacion(notificacion) {
    if (!notificacion || notificacion.leida) {
        return "leida";
    }

    if (notificacion.rebajaMayor) {
        return "rebaja";
    }

    return "bajada";
}

function formatearBadgeNotificacion(notificacion) {
    if (!notificacion || notificacion.leida) {
        return "Leida";
    }

    if (notificacion.rebajaMayor) {
        return "Rebaja mayor";
    }

    return "Bajada de precio";
}

async function marcarComoLeida(notificacionId) {
    try {
        const response = await fetch(`${BASE_URL}/notificaciones/${notificacionId}/leer`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo marcar la notificacion como leida.");
        }

        mostrarMensajeNotificaciones("Notificacion actualizada.", "ok");
        await cargarNotificaciones();
    } catch (error) {
        console.error("Error al marcar notificacion como leida:", error);
        mostrarMensajeNotificaciones(error.message || "No se pudo actualizar la notificacion.", "error");
    }
}

async function marcarTodasComoLeidas() {
    try {
        const response = await fetch(`${BASE_URL}/notificaciones/mias/leer-todas`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron marcar todas las notificaciones como leidas.");
        }

        mostrarMensajeNotificaciones("Todas las notificaciones se han marcado como leidas.", "ok");
        await cargarNotificaciones();
    } catch (error) {
        console.error("Error al marcar todas como leidas:", error);
        mostrarMensajeNotificaciones(error.message || "No se pudieron actualizar las notificaciones.", "error");
    }
}

function renderizarEstadoVacio(texto) {
    const contenedor = document.getElementById("contenedor-notificaciones");
    if (!contenedor) return;

    limpiarContenedor(contenedor);

    const box = document.createElement("div");
    box.className = "notificaciones-vacio";
    box.textContent = texto;
    contenedor.appendChild(box);
}

function mostrarMensajeNotificaciones(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-notificaciones");
    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.classList.remove("hidden", "ok", "error");
    mensaje.classList.add(tipo === "ok" ? "ok" : "error");

    clearTimeout(timeoutMensajeNotificaciones);

    timeoutMensajeNotificaciones = setTimeout(() => {
        mensaje.classList.add("hidden");
    }, 3500);
}

function limpiarContenedor(contenedor) {
    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
}
