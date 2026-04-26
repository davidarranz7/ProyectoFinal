document.addEventListener("DOMContentLoaded", () => {
    const formRecuperarPassword = document.getElementById("form-recuperar-password");
    const identificadorInput = document.getElementById("identificador");
    const mensajeRecuperacion = document.getElementById("mensaje-recuperacion");
    const btnRecuperarPassword = document.getElementById("btn-recuperar-password");

    formRecuperarPassword.addEventListener("submit", async (event) => {
        event.preventDefault();

        const identificador = identificadorInput.value.trim();

        limpiarMensaje();

        if (!identificador) {
            mostrarMensaje("Debes introducir tu email o nombre de usuario.", "error");
            return;
        }

        btnRecuperarPassword.disabled = true;
        btnRecuperarPassword.textContent = "Enviando enlace...";

        try {
            const response = await fetch(`${BASE_URL}/auth/recuperacion/password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    identificador: identificador
                })
            });

            const texto = await response.text();

            if (!response.ok) {
                mostrarMensaje(texto || "No se pudo procesar la solicitud.", "error");
                return;
            }

            mostrarMensaje(texto || "Si existe una cuenta con esos datos, recibirás un correo con instrucciones.", "ok");
            formRecuperarPassword.reset();

            setTimeout(() => {
                cerrarPantallaRecuperacion();
            }, 3000);

        } catch (error) {
            mostrarMensaje("No se pudo conectar con el servidor.", "error");
        } finally {
            btnRecuperarPassword.disabled = false;
            btnRecuperarPassword.textContent = "Enviar enlace de recuperación";
        }
    });

    function mostrarMensaje(texto, tipo) {
        mensajeRecuperacion.textContent = texto;
        mensajeRecuperacion.classList.remove("ok", "error");
        mensajeRecuperacion.classList.add(tipo);
    }

    function limpiarMensaje() {
        mensajeRecuperacion.textContent = "";
        mensajeRecuperacion.classList.remove("ok", "error");
    }

    function cerrarPantallaRecuperacion() {
        if (window.parent && window.parent !== window && typeof window.parent.cerrarAuthOverlay === "function") {
            window.parent.cerrarAuthOverlay();
            return;
        }

        window.location.href = "index.html";
    }
});