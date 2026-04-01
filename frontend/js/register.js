const form = document.getElementById("register-form");
const passwordInput = document.getElementById("password");
const togglePassword = document.getElementById("togglePassword");

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

function finalizarRegistro(nombreUsuario) {
    if (estaEnIframe()) {
        if (window.parent && typeof window.parent.actualizarMenuUsuario === "function") {
            window.parent.actualizarMenuUsuario();
        }

        setTimeout(() => {
            if (window.parent && typeof window.parent.cerrarAuthOverlay === "function") {
                window.parent.cerrarAuthOverlay();
            }
        }, 1200);

        return;
    }

    setTimeout(() => {
        window.location.href = "index.html";
    }, 1200);
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const usuario = document.getElementById("usuario").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!usuario || !email || !password) {
        mostrarModal("CAMPOS INCOMPLETOS", "Faltan datos", "Debes completar todos los campos.");
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/usuarios", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                nombre: usuario,
                email: email,
                password: password,
                rol: "USER"
            })
        });

        if (!response.ok) {
            const errorTexto = await response.text();
            mostrarModal("ERROR", "Registro fallido", errorTexto);
            return;
        }

        const data = await response.json();

        sessionStorage.setItem("usuarioLogueado", "true");
        sessionStorage.setItem("nombreUsuario", data.nombre);
        sessionStorage.setItem("usuarioId", data.id);

        mostrarModal("CORRECTO", "Cuenta creada", "Tu cuenta se ha registrado correctamente.");
        finalizarRegistro(data.nombre);

    } catch (error) {
        console.error("Error en registro:", error);
        mostrarModal("ERROR", "Servidor", "No se pudo conectar con el backend.");
    }
});