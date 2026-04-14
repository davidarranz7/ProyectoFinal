const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");
const form = document.getElementById("login-form");

const modal = document.getElementById("modal-login");
const cerrarModal = document.getElementById("cerrar-modal");
const cerrarModalSecundario = document.getElementById("cerrar-modal-secundario");
const modalTag = document.getElementById("modal-tag");
const modalTitulo = document.getElementById("modal-titulo");
const modalMensaje = document.getElementById("modal-mensaje");

togglePassword.addEventListener("click", () => {
    const isPassword = passwordInput.type === "password";
    passwordInput.type = isPassword ? "text" : "password";
    togglePassword.textContent = isPassword ? "👁" : "●";
});

cerrarModal.addEventListener("click", () => {
    modal.style.display = "none";
});

cerrarModalSecundario.addEventListener("click", () => {
    modal.style.display = "none";
});

modal.addEventListener("click", (event) => {
    if (event.target.classList.contains("modal-overlay")) {
        modal.style.display = "none";
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && modal.style.display === "flex") {
        modal.style.display = "none";
    }
});

function mostrarModal(tag, titulo, mensaje) {
    modalTag.textContent = tag;
    modalTitulo.textContent = titulo;
    modalMensaje.textContent = mensaje;
    modal.style.display = "flex";
}

function estaEnIframe() {
    return window.self !== window.top;
}

function redirigirDespuesLogin(rol) {
    const esAdmin = rol === "ADMIN";

    if (estaEnIframe()) {
        if (esAdmin) {
            setTimeout(() => {
                window.top.location.href = "/admin.html";
            }, 1200);
            return;
        }

        if (window.parent && typeof window.parent.actualizarMenuUsuario === "function") {
            window.parent.actualizarMenuUsuario();
        }

        setTimeout(() => {
            if (window.parent && typeof window.parent.cerrarAuthOverlay === "function") {
                window.parent.cerrarAuthOverlay();
            } else {
                window.top.location.href = "/index.html";
            }
        }, 1200);

        return;
    }

    setTimeout(() => {
        window.location.href = esAdmin ? "/admin.html" : "/index.html";
    }, 1200);
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usuario = document.getElementById("usuario").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!usuario || !password) {
        mostrarModal("CAMPOS INCOMPLETOS", "Faltan datos", "Debes rellenar usuario y contraseña.");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                nombre: usuario,
                password: password
            })
        });

        if (response.status === 401) {
            mostrarModal("ERROR", "Login fallido", "Usuario o contraseña incorrectos.");
            return;
        }

        if (!response.ok) {
            mostrarModal("ERROR", "Servidor", "Ha ocurrido un error inesperado.");
            return;
        }

        const data = await response.json();

        console.log("Respuesta login:", data);

        mostrarModal("CORRECTO", "Bienvenido", `Hola ${data.nombre}`);
        redirigirDespuesLogin(data.rol);

    } catch (error) {
        console.error("Error real:", error);
        mostrarModal("ERROR", "Servidor", "Ha ocurrido un error en el frontend o en la respuesta.");
    }
});