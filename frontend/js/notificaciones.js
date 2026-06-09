document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaNotificaciones();
});

let timeoutMensajeNotificaciones = null;
let estadoPushServidor = null;

async function iniciarPaginaNotificaciones() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensajeNotificaciones("Debes iniciar sesion para ver tus notificaciones.", "error");
        renderizarEstadoVacio("Inicia sesion para consultar tus avisos.");
        renderizarEstadoPushInvitado();
        return;
    }

    configurarAcciones();

    await Promise.allSettled([
        cargarNotificaciones(),
        inicializarPush()
    ]);
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
    const btnTogglePush = document.getElementById("btn-toggle-push");

    if (btnMarcarTodas) {
        btnMarcarTodas.addEventListener("click", marcarTodasComoLeidas);
    }

    if (btnTogglePush) {
        btnTogglePush.addEventListener("click", gestionarTogglePush);
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

async function inicializarPush() {
    if (!soportaPushNativo()) {
        renderizarEstadoPush(
            "warn",
            "No disponible aqui",
            "Este navegador o este modo de apertura no soporta notificaciones push."
        );
        return;
    }

    try {
        await registrarServiceWorkerPush();
        await cargarEstadoPushServidor();
        await sincronizarSuscripcionExistente();
        actualizarPanelPush();
    } catch (error) {
        console.error("Error inicializando push:", error);
        renderizarEstadoPush(
            "error",
            "Error al preparar push",
            error.message || "No se pudieron preparar las notificaciones reales."
        );
    }
}

async function gestionarTogglePush() {
    const btn = document.getElementById("btn-toggle-push");

    if (btn) {
        btn.disabled = true;
    }

    try {
        if (!soportaPushNativo()) {
            throw new Error("Este dispositivo no soporta notificaciones push.");
        }

        if (!estadoPushServidor?.habilitado) {
            throw new Error("Las notificaciones push estan desactivadas en el servidor.");
        }

        if (!estadoPushServidor?.configurado) {
            throw new Error("Faltan las claves de push en el servidor.");
        }

        if (Notification.permission === "denied") {
            throw new Error("El permiso de notificaciones esta bloqueado. Reactivalo en los ajustes del navegador o del iPhone.");
        }

        if (estadoPushServidor?.suscrito) {
            await desactivarPush();
            mostrarMensajeNotificaciones("Has desactivado las notificaciones reales en este dispositivo.", "ok");
        } else {
            await activarPush();
            mostrarMensajeNotificaciones("Notificaciones reales activadas correctamente.", "ok");
        }

        await cargarEstadoPushServidor();
        actualizarPanelPush();
    } catch (error) {
        console.error("Error cambiando el estado del push:", error);
        mostrarMensajeNotificaciones(error.message || "No se pudo actualizar el estado del push.", "error");
        actualizarPanelPush();
    } finally {
        if (btn) {
            btn.disabled = false;
        }
    }
}

async function activarPush() {
    const registration = await registrarServiceWorkerPush();
    const permission = Notification.permission === "granted"
        ? "granted"
        : await Notification.requestPermission();

    if (permission !== "granted") {
        throw new Error("No has concedido permiso para recibir notificaciones.");
    }

    const publicKey = estadoPushServidor?.clavePublica;

    if (!publicKey) {
        throw new Error("No hay clave publica VAPID disponible.");
    }

    let subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(publicKey)
        });
    }

    await enviarSuscripcionAServidor(subscription);
}

async function desactivarPush() {
    const registration = await registrarServiceWorkerPush();
    const subscription = await registration.pushManager.getSubscription();

    if (subscription) {
        await eliminarSuscripcionDelServidor(subscription);
        await subscription.unsubscribe();
        return;
    }

    await fetch(`${BASE_URL}/push-notificaciones/suscripcion`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify({})
    });
}

async function cargarEstadoPushServidor() {
    const response = await fetch(`${BASE_URL}/push-notificaciones/estado`, {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error("No se pudo obtener el estado de las notificaciones push.");
    }

    estadoPushServidor = await response.json();
}

async function sincronizarSuscripcionExistente() {
    if (!estadoPushServidor?.configurado) {
        return;
    }

    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        return;
    }

    if (!estadoPushServidor?.suscrito) {
        await enviarSuscripcionAServidor(subscription);
        estadoPushServidor.suscrito = true;
    }
}

async function enviarSuscripcionAServidor(subscription) {
    const payload = serializarSuscripcionPush(subscription);

    const response = await fetch(`${BASE_URL}/push-notificaciones/suscripcion`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error("No se pudo guardar la suscripcion push en el servidor.");
    }

    estadoPushServidor = await response.json();
}

async function eliminarSuscripcionDelServidor(subscription) {
    const payload = serializarSuscripcionPush(subscription);

    const response = await fetch(`${BASE_URL}/push-notificaciones/suscripcion`, {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json"
        },
        credentials: "include",
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        throw new Error("No se pudo eliminar la suscripcion push del servidor.");
    }

    estadoPushServidor = await response.json();
}

async function registrarServiceWorkerPush() {
    return navigator.serviceWorker.register("/service-worker.js");
}

function serializarSuscripcionPush(subscription) {
    if (subscription && typeof subscription.toJSON === "function") {
        const data = subscription.toJSON();

        if (data?.endpoint && data?.keys?.p256dh && data?.keys?.auth) {
            return data;
        }
    }

    return {
        endpoint: subscription?.endpoint || "",
        expirationTime: subscription?.expirationTime ?? null,
        keys: {
            p256dh: arrayBufferToBase64Url(subscription?.getKey?.("p256dh")),
            auth: arrayBufferToBase64Url(subscription?.getKey?.("auth"))
        }
    };
}

function actualizarPanelPush() {
    const btn = document.getElementById("btn-toggle-push");

    if (!btn) {
        return;
    }

    if (!soportaPushNativo()) {
        btn.disabled = true;
        btn.textContent = "No disponible";
        return;
    }

    if (Notification.permission === "denied") {
        btn.disabled = true;
        btn.textContent = "Permiso bloqueado";
        renderizarEstadoPush(
            "error",
            "Permiso bloqueado",
            "Debes reactivar las notificaciones desde los ajustes del navegador o del iPhone."
        );
        return;
    }

    if (!estadoPushServidor) {
        btn.disabled = true;
        btn.textContent = "Comprobando...";
        return;
    }

    if (!estadoPushServidor.habilitado) {
        btn.disabled = true;
        btn.textContent = "No disponible";
        renderizarEstadoPush(
            "warn",
            "Push desactivado",
            estadoPushServidor.mensaje || "Las notificaciones push estan desactivadas en el servidor."
        );
        return;
    }

    if (!estadoPushServidor.configurado) {
        btn.disabled = true;
        btn.textContent = "Falta configurar";
        renderizarEstadoPush(
            "warn",
            "Servidor sin claves",
            estadoPushServidor.mensaje || "Faltan las claves VAPID para enviar push."
        );
        return;
    }

    if (estadoPushServidor.suscrito) {
        btn.disabled = false;
        btn.textContent = "Desactivar notificaciones reales";
        renderizarEstadoPush(
            "ok",
            "Activadas",
            "Este dispositivo recibira avisos de bajadas de precio incluso fuera de la web."
        );
        return;
    }

    btn.disabled = false;
    btn.textContent = "Activar notificaciones reales";

    if (esIosSinPwaInstalada()) {
        renderizarEstadoPush(
            "warn",
            "Abrir como app",
            "En iPhone debes anadir MODA a pantalla de inicio y abrirla como app para que Apple permita el push."
        );
        return;
    }

    renderizarEstadoPush(
        "warn",
        "Listas para activar",
        estadoPushServidor.mensaje || "Puedes activar notificaciones reales en este dispositivo."
    );
}

function renderizarEstadoPushInvitado() {
    const btn = document.getElementById("btn-toggle-push");

    if (btn) {
        btn.disabled = true;
        btn.textContent = "Inicia sesion";
    }

    renderizarEstadoPush(
        "warn",
        "Inicia sesion",
        "Necesitas iniciar sesion para activar avisos reales de tus favoritos."
    );
}

function renderizarEstadoPush(estado, titulo, detalle) {
    const badge = document.getElementById("estado-push");
    const detalleElemento = document.getElementById("detalle-push");

    if (badge) {
        badge.dataset.estado = estado || "cargando";
        badge.textContent = titulo || "Comprobando...";
    }

    if (detalleElemento) {
        detalleElemento.textContent = detalle || "";
    }
}

function soportaPushNativo() {
    const contextoSeguro = window.isSecureContext || window.ModaRuntimeConfig?.esEntornoLocal === true;
    return contextoSeguro && "serviceWorker" in navigator && "PushManager" in window && "Notification" in window;
}

function esIosSinPwaInstalada() {
    const userAgent = window.navigator.userAgent || "";
    const esIos = /iPad|iPhone|iPod/.test(userAgent);
    const standalone = window.matchMedia?.("(display-mode: standalone)")?.matches || window.navigator.standalone === true;
    return esIos && !standalone;
}

function urlBase64ToUint8Array(base64String) {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding)
        .replace(/-/g, "+")
        .replace(/_/g, "/");

    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);

    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i);
    }

    return outputArray;
}

function arrayBufferToBase64Url(buffer) {
    if (!buffer) {
        return "";
    }

    const bytes = new Uint8Array(buffer);
    let binary = "";

    bytes.forEach((byte) => {
        binary += String.fromCharCode(byte);
    });

    return window.btoa(binary)
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/g, "");
}
