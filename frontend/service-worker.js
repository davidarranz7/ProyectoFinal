self.addEventListener("install", (event) => {
    event.waitUntil(self.skipWaiting());
});

self.addEventListener("activate", (event) => {
    event.waitUntil(self.clients.claim());
});

self.addEventListener("push", (event) => {
    const data = leerPayloadSeguro(event);
    const title = data.title || "MODA";
    const body = data.body || "Tienes una nueva notificacion.";
    const url = data.url || "notificaciones.html";

    event.waitUntil(
        self.registration.showNotification(title, {
            body,
            data: {
                url
            },
            icon: data.icon || "/icon.svg",
            badge: data.badge || "/icon.svg",
            tag: data.tag || "moda-push",
            renotify: false
        })
    );
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    const destino = new URL(
        event.notification?.data?.url || "notificaciones.html",
        self.location.origin
    ).href;

    event.waitUntil(
        self.clients.matchAll({ type: "window", includeUncontrolled: true }).then((clientList) => {
            for (const client of clientList) {
                if (client.url === destino && "focus" in client) {
                    return client.focus();
                }
            }

            if (self.clients.openWindow) {
                return self.clients.openWindow(destino);
            }

            return null;
        })
    );
});

function leerPayloadSeguro(event) {
    if (!event || !event.data) {
        return {};
    }

    try {
        return event.data.json();
    } catch (error) {
        return {
            body: event.data.text()
        };
    }
}
