document.addEventListener("DOMContentLoaded", () => {
    const formRecuperarUsuario = document.getElementById("form-recuperar-usuario");
    const emailInput = document.getElementById("email");
    const mensajeRecuperacionUsuario = document.getElementById("mensaje-recuperacion-usuario");
    const btnRecuperarUsuario = document.getElementById("btn-recuperar-usuario");

    formRecuperarUsuario.addEventListener("submit", async (event) => {
        event.preventDefault();

        const email = emailInput.value.trim();

        limpiarMensaje();

        if (!email) {
            mostrarMensaje("Debes introducir tu email.", "error");
            return;
        }

        if (!validarEmail(email)) {
            mostrarMensaje("El formato del email no es válido.", "error");
            return;
        }

        btnRecuperarUsuario.disabled = true;
        btnRecuperarUsuario.textContent = "Enviando usuario...";

        try {
            const response = await fetch(`${BASE_URL}/auth/recuperacion/usuario`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    email: email
                })
            });

            const texto = await response.text();

            if (!response.ok) {
                mostrarMensaje(texto || "No se pudo procesar la solicitud.", "error");
                return;
            }

            mostrarMensaje(texto || "Te hemos enviado un correo con tu nombre de usuario.", "ok");
            formRecuperarUsuario.reset();

            setTimeout(() => {
                cerrarPantallaRecuperacionUsuario();
            }, 5000);

        } catch (error) {
            mostrarMensaje("No se pudo conectar con el servidor.", "error");
        } finally {
            btnRecuperarUsuario.disabled = false;
            btnRecuperarUsuario.textContent = "Enviar mi usuario";
        }
    });

    function mostrarMensaje(texto, tipo) {
        mensajeRecuperacionUsuario.textContent = texto;
        mensajeRecuperacionUsuario.classList.remove("ok", "error");
        mensajeRecuperacionUsuario.classList.add(tipo);
    }

    function limpiarMensaje() {
        mensajeRecuperacionUsuario.textContent = "";
        mensajeRecuperacionUsuario.classList.remove("ok", "error");
    }

    function validarEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function cerrarPantallaRecuperacionUsuario() {
        if (window.parent && window.parent !== window && typeof window.parent.cerrarAuthOverlay === "function") {
            window.parent.cerrarAuthOverlay();
            return;
        }

        window.location.href = "login.html";
    }
});