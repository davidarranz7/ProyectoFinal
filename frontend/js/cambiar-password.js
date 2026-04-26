document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get("token");

    const tokenError = document.getElementById("token-error");
    const formCambiarPassword = document.getElementById("form-cambiar-password");
    const nuevaPasswordInput = document.getElementById("nuevaPassword");
    const repetirPasswordInput = document.getElementById("repetirPassword");
    const mensajePassword = document.getElementById("mensaje-password");
    const btnCambiarPassword = document.getElementById("btn-cambiar-password");

    if (!token) {
        tokenError.classList.remove("hidden");
        formCambiarPassword.classList.add("hidden");
        return;
    }

    formCambiarPassword.addEventListener("submit", async (event) => {
        event.preventDefault();

        const nuevaPassword = nuevaPasswordInput.value.trim();
        const repetirPassword = repetirPasswordInput.value.trim();

        limpiarMensaje();

        if (!nuevaPassword || !repetirPassword) {
            mostrarMensaje("Todos los campos son obligatorios.", "error");
            return;
        }

        if (nuevaPassword !== repetirPassword) {
            mostrarMensaje("Las contraseñas no coinciden.", "error");
            return;
        }

        if (nuevaPassword.length < 4) {
            mostrarMensaje("La contraseña debe tener al menos 4 caracteres.", "error");
            return;
        }

        btnCambiarPassword.disabled = true;
        btnCambiarPassword.textContent = "Cambiando contraseña...";

        try {
            const response = await fetch(`${BASE_URL}/auth/recuperacion/cambiar-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    token: token,
                    nuevaPassword: nuevaPassword,
                    repetirPassword: repetirPassword
                })
            });

            const texto = await response.text();

            if (!response.ok) {
                mostrarMensaje(texto || "No se pudo cambiar la contraseña.", "error");
                return;
            }

            mostrarMensaje("Contraseña actualizada correctamente. Ya puedes iniciar sesión.", "ok");
            formCambiarPassword.reset();

            setTimeout(() => {
                window.location.href = "index.html";
            }, 2500);

        } catch (error) {
            mostrarMensaje("No se pudo conectar con el servidor.", "error");
        } finally {
            btnCambiarPassword.disabled = false;
            btnCambiarPassword.textContent = "Cambiar contraseña";
        }
    });

    function mostrarMensaje(texto, tipo) {
        mensajePassword.textContent = texto;
        mensajePassword.classList.remove("ok", "error");
        mensajePassword.classList.add(tipo);
    }

    function limpiarMensaje() {
        mensajePassword.textContent = "";
        mensajePassword.classList.remove("ok", "error");
    }
});