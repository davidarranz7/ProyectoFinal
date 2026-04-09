document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPerfil();
});

let tarjetaIdPendienteEliminar = null;

async function obtenerSesionActual() {
    try {
        const response = await fetch("http://localhost:8080/auth/session", {
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

async function iniciarPaginaPerfil() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensaje("Debes iniciar sesión para acceder a tu perfil.");
        return;
    }

    configurarFormularioPerfil(sesion.id);
    configurarFormularioPassword(sesion.id);
    configurarTarjetas(sesion.id);
    configurarModalEliminarTarjeta(sesion.id);

    await cargarPerfil(sesion.id);
    await cargarTarjetas(sesion.id);
}

async function cargarPerfil(usuarioId) {
    try {
        const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/perfil`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el perfil");
        }

        const usuario = await response.json();

        document.getElementById("nombre").value = usuario.nombre || "";
        document.getElementById("email").value = usuario.email || "";
        document.getElementById("rol").value = usuario.rol || "";

        actualizarPanelLateral(usuario);
        actualizarNombreMenu(usuario.nombre);

    } catch (error) {
        console.error("Error al cargar perfil:", error);
        mostrarMensaje("No se pudo cargar tu perfil.");
    }
}

function actualizarPanelLateral(usuario) {
    const nombreLateral = document.getElementById("perfil-nombre-lateral");
    const miniEmail = document.getElementById("mini-email");
    const miniRol = document.getElementById("mini-rol");
    const avatarInicial = document.getElementById("avatar-inicial");

    if (nombreLateral) {
        nombreLateral.textContent = usuario.nombre || "Usuario";
    }

    if (miniEmail) {
        miniEmail.textContent = usuario.email || "";
    }

    if (miniRol) {
        miniRol.textContent = usuario.rol || "";
    }

    if (avatarInicial) {
        const inicial = usuario.nombre && usuario.nombre.trim().length > 0
            ? usuario.nombre.trim().charAt(0).toUpperCase()
            : "U";

        avatarInicial.textContent = inicial;
    }
}

function configurarFormularioPerfil(usuarioId) {
    const formPerfil = document.getElementById("formPerfil");
    const btnGuardarPerfil = document.getElementById("btn-guardar-perfil");

    if (!formPerfil) return;

    formPerfil.addEventListener("submit", async (e) => {
        e.preventDefault();

        const nombre = document.getElementById("nombre").value.trim();
        const email = document.getElementById("email").value.trim();

        const errorValidacion = validarDatosPerfil(nombre, email);
        if (errorValidacion) {
            mostrarMensaje(errorValidacion);
            return;
        }

        const datos = {
            nombre: nombre,
            email: email
        };

        try {
            if (btnGuardarPerfil) {
                btnGuardarPerfil.disabled = true;
                btnGuardarPerfil.textContent = "Guardando...";
            }

            const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/perfil`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                credentials: "include",
                body: JSON.stringify(datos)
            });

            const textoRespuesta = await response.text();

            if (!response.ok) {
                throw new Error(textoRespuesta || "No se pudo actualizar el perfil");
            }

            const usuarioActualizado = textoRespuesta ? JSON.parse(textoRespuesta) : null;

            if (usuarioActualizado) {
                document.getElementById("nombre").value = usuarioActualizado.nombre || "";
                document.getElementById("email").value = usuarioActualizado.email || "";
                document.getElementById("rol").value = usuarioActualizado.rol || "";

                actualizarPanelLateral(usuarioActualizado);
                actualizarNombreMenu(usuarioActualizado.nombre);
            }

            mostrarMensaje("Perfil actualizado correctamente.", "ok");

        } catch (error) {
            console.error("Error al actualizar perfil:", error);
            mostrarMensaje(error.message || "No se pudo actualizar el perfil.");
        } finally {
            if (btnGuardarPerfil) {
                btnGuardarPerfil.disabled = false;
                btnGuardarPerfil.textContent = "Guardar cambios";
            }
        }
    });
}

function validarDatosPerfil(nombre, email) {
    if (!nombre || !email) {
        return "Nombre y email son obligatorios.";
    }

    if (nombre.length < 3) {
        return "El nombre debe tener al menos 3 caracteres.";
    }

    if (nombre.length > 30) {
        return "El nombre no puede superar los 30 caracteres.";
    }

    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9._\-\s]+$/;
    if (!regexNombre.test(nombre)) {
        return "El nombre contiene caracteres no permitidos.";
    }

    if (!esEmailValido(email)) {
        return "Introduce un email válido.";
    }

    if (email.length > 100) {
        return "El email es demasiado largo.";
    }

    return null;
}

function esEmailValido(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

function configurarFormularioPassword(usuarioId) {
    const formPassword = document.getElementById("formPassword");

    if (!formPassword) return;

    formPassword.addEventListener("submit", async (e) => {
        e.preventDefault();

        const passwordActual = document.getElementById("passwordActual").value.trim();
        const passwordNueva = document.getElementById("passwordNueva").value.trim();
        const confirmarPassword = document.getElementById("confirmarPassword").value.trim();

        if (!passwordActual || !passwordNueva || !confirmarPassword) {
            mostrarMensaje("Debes completar todos los campos de contraseña.");
            return;
        }

        if (passwordNueva.length < 6) {
            mostrarMensaje("La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (passwordNueva !== confirmarPassword) {
            mostrarMensaje("La nueva contraseña y su confirmación no coinciden.");
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
                credentials: "include",
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

function configurarTarjetas(usuarioId) {
    const btnAnadirTarjeta = document.getElementById("btn-anadir-tarjeta");
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");
    const modalTarjeta = document.getElementById("modal-tarjeta");
    const cerrarModalTarjeta = document.getElementById("cerrar-modal-tarjeta");
    const cancelarModalTarjeta = document.getElementById("cancelar-modal-tarjeta");
    const formTarjeta = document.getElementById("formTarjeta");
    const btnCambiarFoto = document.getElementById("btn-cambiar-foto");

    function abrirModalTarjeta() {
        if (modalTarjeta) {
            modalTarjeta.style.display = "flex";
        }
    }

    function cerrarModal() {
        if (modalTarjeta) {
            modalTarjeta.style.display = "none";
        }
        if (formTarjeta) {
            formTarjeta.reset();
        }
    }

    if (btnAnadirTarjeta) {
        btnAnadirTarjeta.addEventListener("click", abrirModalTarjeta);
    }

    if (cerrarModalTarjeta) {
        cerrarModalTarjeta.addEventListener("click", cerrarModal);
    }

    if (cancelarModalTarjeta) {
        cancelarModalTarjeta.addEventListener("click", cerrarModal);
    }

    if (modalTarjeta) {
        modalTarjeta.addEventListener("click", (e) => {
            if (e.target === modalTarjeta) {
                cerrarModal();
            }
        });
    }

    if (btnCambiarFoto) {
        btnCambiarFoto.addEventListener("click", () => {
            mostrarMensaje("La funcionalidad de cambiar foto la hacemos después.", "ok");
        });
    }

    if (contenedorTarjetas) {
        contenedorTarjetas.addEventListener("click", (e) => {
            const bloqueAnadir = e.target.closest(".add-card");
            const btnEliminar = e.target.closest(".btn-eliminar-tarjeta");

            if (bloqueAnadir) {
                abrirModalTarjeta();
                return;
            }

            if (btnEliminar) {
                const tarjetaId = btnEliminar.dataset.id;

                if (!tarjetaId) return;

                abrirModalEliminarTarjeta(tarjetaId);
            }
        });
    }

    if (formTarjeta) {
        formTarjeta.addEventListener("submit", async (e) => {
            e.preventDefault();

            const titular = document.getElementById("titularTarjeta").value.trim();
            const numeroTarjeta = document.getElementById("numeroTarjeta").value.trim();
            const fechaExpiracion = document.getElementById("fechaExpiracionTarjeta").value.trim();
            const tipo = document.getElementById("tipoTarjeta").value;

            const errorTarjeta = validarDatosTarjeta(titular, numeroTarjeta, fechaExpiracion, tipo);
            if (errorTarjeta) {
                mostrarMensaje(errorTarjeta);
                return;
            }

            const datos = {
                titular: titular,
                numeroTarjeta: numeroTarjeta.replace(/\s+/g, ""),
                fechaExpiracion: fechaExpiracion,
                tipo: tipo
            };

            try {
                const response = await fetch(`http://localhost:8080/tarjetas/usuario/${usuarioId}`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    credentials: "include",
                    body: JSON.stringify(datos)
                });

                const textoError = await response.text();

                if (!response.ok) {
                    throw new Error(textoError || "No se pudo guardar la tarjeta");
                }

                cerrarModal();
                mostrarMensaje("Tarjeta guardada correctamente.", "ok");
                await cargarTarjetas(usuarioId);

            } catch (error) {
                console.error("Error al guardar tarjeta:", error);
                mostrarMensaje(error.message || "No se pudo guardar la tarjeta.");
            }
        });
    }

    const numeroTarjetaInput = document.getElementById("numeroTarjeta");
    const fechaExpiracionInput = document.getElementById("fechaExpiracionTarjeta");

    if (numeroTarjetaInput) {
        numeroTarjetaInput.addEventListener("input", () => {
            numeroTarjetaInput.value = formatearNumeroTarjetaInput(numeroTarjetaInput.value);
        });
    }

    if (fechaExpiracionInput) {
        fechaExpiracionInput.addEventListener("input", () => {
            fechaExpiracionInput.value = formatearFechaExpiracionInput(fechaExpiracionInput.value);
        });
    }
}

function validarDatosTarjeta(titular, numeroTarjeta, fechaExpiracion, tipo) {
    if (!titular || !numeroTarjeta || !fechaExpiracion || !tipo) {
        return "Debes completar todos los datos de la tarjeta.";
    }

    if (titular.length < 3) {
        return "El nombre del titular es demasiado corto.";
    }

    const numeroLimpio = numeroTarjeta.replace(/\s+/g, "");

    if (!/^\d{16}$/.test(numeroLimpio)) {
        return "El número de tarjeta debe tener 16 dígitos.";
    }

    if (!/^\d{2}\/\d{2}$/.test(fechaExpiracion)) {
        return "La fecha de expiración debe tener formato MM/AA.";
    }

    const [mesTexto] = fechaExpiracion.split("/");
    const mes = parseInt(mesTexto, 10);

    if (mes < 1 || mes > 12) {
        return "El mes de expiración no es válido.";
    }

    return null;
}

function formatearNumeroTarjetaInput(valor) {
    const soloNumeros = valor.replace(/\D/g, "").slice(0, 16);
    return soloNumeros.replace(/(\d{4})(?=\d)/g, "$1 ");
}

function formatearFechaExpiracionInput(valor) {
    const soloNumeros = valor.replace(/\D/g, "").slice(0, 4);

    if (soloNumeros.length <= 2) {
        return soloNumeros;
    }

    return `${soloNumeros.slice(0, 2)}/${soloNumeros.slice(2)}`;
}

function configurarModalEliminarTarjeta(usuarioId) {
    const modalEliminar = document.getElementById("modal-confirmar-eliminar");
    const cerrarModalEliminar = document.getElementById("cerrar-modal-eliminar");
    const cancelarEliminar = document.getElementById("cancelar-eliminar-tarjeta");
    const confirmarEliminar = document.getElementById("confirmar-eliminar-tarjeta");

    function cerrarModal() {
        if (modalEliminar) {
            modalEliminar.style.display = "none";
        }
        tarjetaIdPendienteEliminar = null;
    }

    if (cerrarModalEliminar) {
        cerrarModalEliminar.addEventListener("click", cerrarModal);
    }

    if (cancelarEliminar) {
        cancelarEliminar.addEventListener("click", cerrarModal);
    }

    if (modalEliminar) {
        modalEliminar.addEventListener("click", (e) => {
            if (e.target === modalEliminar) {
                cerrarModal();
            }
        });
    }

    if (confirmarEliminar) {
        confirmarEliminar.addEventListener("click", async () => {
            if (!tarjetaIdPendienteEliminar) return;

            try {
                confirmarEliminar.disabled = true;
                confirmarEliminar.textContent = "Eliminando...";

                const response = await fetch(`http://localhost:8080/tarjetas/${tarjetaIdPendienteEliminar}`, {
                    method: "DELETE",
                    credentials: "include"
                });

                const textoError = await response.text();

                if (!response.ok) {
                    throw new Error(textoError || "No se pudo eliminar la tarjeta");
                }

                cerrarModal();
                mostrarMensaje("Tarjeta eliminada correctamente.", "ok");
                await cargarTarjetas(usuarioId);

            } catch (error) {
                console.error("Error al eliminar tarjeta:", error);
                mostrarMensaje(error.message || "No se pudo eliminar la tarjeta.");
            } finally {
                confirmarEliminar.disabled = false;
                confirmarEliminar.textContent = "Eliminar";
            }
        });
    }
}

function abrirModalEliminarTarjeta(tarjetaId) {
    const modalEliminar = document.getElementById("modal-confirmar-eliminar");

    tarjetaIdPendienteEliminar = tarjetaId;

    if (modalEliminar) {
        modalEliminar.style.display = "flex";
    }
}

async function cargarTarjetas(usuarioId) {
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");

    if (!contenedorTarjetas) return;

    try {
        const response = await fetch(`http://localhost:8080/tarjetas/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las tarjetas");
        }

        const tarjetas = await response.json();
        renderizarTarjetas(tarjetas);

    } catch (error) {
        console.error("Error al cargar tarjetas:", error);
        contenedorTarjetas.innerHTML = `
            <div class="add-card">No se pudieron cargar las tarjetas</div>
        `;
    }
}

function renderizarTarjetas(tarjetas) {
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");

    if (!contenedorTarjetas) return;

    contenedorTarjetas.innerHTML = "";

    if (!Array.isArray(tarjetas) || tarjetas.length === 0) {
        contenedorTarjetas.innerHTML = `
            <div class="add-card">
                Añadir nueva tarjeta de pago
            </div>
        `;
        return;
    }

    tarjetas.forEach(tarjeta => {
        contenedorTarjetas.appendChild(crearTarjetaHTML(tarjeta));
    });

    const bloqueAnadir = document.createElement("div");
    bloqueAnadir.className = "add-card";
    bloqueAnadir.textContent = "Añadir nueva tarjeta de pago";

    contenedorTarjetas.appendChild(bloqueAnadir);
}

function crearTarjetaHTML(tarjeta) {
    const card = document.createElement("div");
    card.classList.add("tarjeta-banco");

    const tipo = (tarjeta.tipo || "").toUpperCase();

    if (tipo === "VISA") {
        card.style.background = "#111";
        card.style.color = "#fff";
    } else if (tipo === "MASTERCARD") {
        card.style.background = "linear-gradient(135deg, #f5aac2, #df7aa0)";
        card.style.color = "#fff";
    } else {
        card.style.background = "#444";
        card.style.color = "#fff";
    }

    card.style.borderRadius = "18px";
    card.style.padding = "22px";
    card.style.minHeight = "200px";
    card.style.display = "flex";
    card.style.flexDirection = "column";
    card.style.justifyContent = "space-between";
    card.style.boxShadow = "0 10px 25px rgba(0,0,0,0.12)";
    card.style.position = "relative";

    card.innerHTML = `
        <button class="btn-eliminar-tarjeta"
                data-id="${tarjeta.id}"
                type="button"
                style="position:absolute; top:12px; right:12px; border:none; background:rgba(255,255,255,0.18); color:#fff; padding:8px 10px; border-radius:10px; cursor:pointer;">
            Eliminar
        </button>

        <div class="tarjeta-top" style="display:flex; justify-content:space-between; align-items:center; margin-bottom:22px;">
            <span style="font-size:18px; font-weight:700;">${formatearTipoTarjeta(tarjeta.tipo)}</span>
            <div style="width:42px; height:30px; border-radius:8px; background:rgba(255,255,255,0.35);"></div>
        </div>

        <div class="numero-tarjeta" style="font-size:22px; letter-spacing:2px; font-weight:600; margin-bottom:24px;">
            ${tarjeta.numeroEnmascarado || "**** **** **** 0000"}
        </div>

        <div class="tarjeta-bottom" style="display:flex; justify-content:space-between; gap:20px;">
            <div>
                <small style="opacity:0.85;">Titular</small><br />
                <strong>${tarjeta.titular || "Usuario"}</strong>
            </div>
            <div>
                <small style="opacity:0.85;">Expira</small><br />
                <strong>${tarjeta.fechaExpiracion || "--/--"}</strong>
            </div>
        </div>
    `;

    return card;
}

function formatearTipoTarjeta(tipo) {
    if (!tipo) return "Tarjeta";
    if (tipo.toUpperCase() === "VISA") return "Visa";
    if (tipo.toUpperCase() === "MASTERCARD") return "Mastercard";
    return tipo;
}

function actualizarNombreMenu(nombreNuevo) {
    const profileNameMenu = document.querySelector("#menu-container #profile-name");

    if (profileNameMenu) {
        profileNameMenu.textContent = nombreNuevo;
    }
}

function mostrarMensaje(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-perfil");

    if (!mensaje) return;

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