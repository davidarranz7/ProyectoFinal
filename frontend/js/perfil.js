document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPerfil();
});

async function iniciarPaginaPerfil() {
    const usuarioId = localStorage.getItem("usuarioId");

    if (!usuarioId) {
        mostrarMensaje("Debes iniciar sesión para acceder a tu perfil.");
        return;
    }

    configurarFormularioPerfil(usuarioId);
    configurarFormularioPassword(usuarioId);
    await cargarPerfil(usuarioId);
}

async function cargarPerfil(usuarioId) {
    try {
        const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/perfil`);

        if (!response.ok) {
            throw new Error("No se pudo cargar el perfil");
        }

        const usuario = await response.json();

        document.getElementById("nombre").value = usuario.nombre || "";
        document.getElementById("email").value = usuario.email || "";
        document.getElementById("rol").value = usuario.rol || "";

    } catch (error) {
        console.error("Error al cargar perfil:", error);
        mostrarMensaje("No se pudo cargar tu perfil.");
    }
}

function configurarFormularioPerfil(usuarioId) {
    const formPerfil = document.getElementById("formPerfil");

    formPerfil.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const email = document.getElementById("email").value.trim();

        if (!nombre || !email) {
            mostrarMensaje("Nombre y email son obligatorios.");
            return;
        }

        const datos = {
            nombre: nombre,
            email: email
        };

        try {
            const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/perfil`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            });

            if (!response.ok) {
                const textoError = await response.text();
                throw new Error(textoError || "No se pudo actualizar el perfil");
            }

            const usuarioActualizado = await response.json();

            actualizarDatosSesion(usuarioActualizado);
            actualizarNombreMenu(usuarioActualizado.nombre);

            mostrarMensaje("Perfil actualizado correctamente.", "ok");

        } catch (error) {
            console.error("Error al actualizar perfil:", error);
            mostrarMensaje(error.message || "No se pudo actualizar el perfil.");
        }
    });
}

function configurarFormularioPassword(usuarioId) {
    const formPassword = document.getElementById("formPassword");

    formPassword.addEventListener("submit", async (e) => {
        e.preventDefault();

        const passwordActual = document.getElementById("passwordActual").value.trim();
        const passwordNueva = document.getElementById("passwordNueva").value.trim();
        const confirmarPassword = document.getElementById("confirmarPassword").value.trim();

        if (!passwordActual || !passwordNueva || !confirmarPassword) {
            mostrarMensaje("Debes completar todos los campos de contraseña.");
            return;
        }

        const datos = {
            passwordActual: passwordActual,
            passwordNueva: passwordNueva,
            confirmarPassword: confirmarPassword
        };

        try {
            const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/password`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datos)
            });

            const texto = await response.text();

            if (!response.ok) {
                throw new Error(texto || "No se pudo actualizar la contraseña");
            }

            document.getElementById("formPassword").reset();
            mostrarMensaje("Contraseña actualizada correctamente.", "ok");

        } catch (error) {
            console.error("Error al cambiar contraseña:", error);
            mostrarMensaje(error.message || "No se pudo actualizar la contraseña.");
        }
    });
}

function actualizarDatosSesion(usuarioActualizado) {
    const usuarioLogueado = JSON.parse(localStorage.getItem("usuarioLogueado"));

    if (usuarioLogueado) {
        usuarioLogueado.nombre = usuarioActualizado.nombre;
        usuarioLogueado.email = usuarioActualizado.email;
        localStorage.setItem("usuarioLogueado", JSON.stringify(usuarioLogueado));
    }
}

function actualizarNombreMenu(nombreNuevo) {
    const profileName = document.getElementById("profile-name");

    if (profileName) {
        profileName.textContent = nombreNuevo;
    }
}

function mostrarMensaje(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-perfil");

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