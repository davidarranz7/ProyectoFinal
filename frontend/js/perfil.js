document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPerfil();
});

let tarjetaIdPendienteEliminar = null;
let timeoutValidacionNombre = null;
let timeoutValidacionEmail = null;
let timeoutMensajePerfil = null;
let modoEdicionPerfil = false;

let estadoValidacionPerfil = {
    nombreValido: true,
    emailValido: true
};

let valoresOriginalesPerfil = {
    nombre: "",
    email: "",
    rol: ""
};

async function iniciarPaginaPerfil() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarMensaje("Debes iniciar sesión para acceder a tu perfil.");
        return;
    }

    configurarNavegacionSecciones();
    configurarFormularioPerfil(sesion.id);
    configurarFormularioPassword(sesion.id);
    configurarTarjetas(sesion.id);
    configurarModalEliminarTarjeta(sesion.id);
    configurarValidacionEnVivoPerfil(sesion.id);

    await cargarPerfil(sesion.id);
    await cargarTarjetas(sesion.id);

    activarSeccion("perfil");
    desactivarModoEdicionPerfil();
}

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

function configurarNavegacionSecciones() {
    const btnPerfil = document.getElementById("btn-menu-perfil");
    const btnPassword = document.getElementById("btn-menu-password");
    const btnTarjetas = document.getElementById("btn-menu-tarjetas");

    if (btnPerfil) {
        btnPerfil.addEventListener("click", () => activarSeccion("perfil"));
    }

    if (btnPassword) {
        btnPassword.addEventListener("click", () => activarSeccion("password"));
    }

    if (btnTarjetas) {
        btnTarjetas.addEventListener("click", () => activarSeccion("tarjetas"));
    }
}

function activarSeccion(nombreSeccion) {
    const secciones = {
        perfil: document.getElementById("seccion-perfil"),
        password: document.getElementById("seccion-password"),
        tarjetas: document.getElementById("seccion-tarjetas")
    };

    const botones = {
        perfil: document.getElementById("btn-menu-perfil"),
        password: document.getElementById("btn-menu-password"),
        tarjetas: document.getElementById("btn-menu-tarjetas")
    };

    Object.values(secciones).forEach(seccion => {
        if (seccion) {
            seccion.style.display = "none";
        }
    });

    Object.values(botones).forEach(boton => {
        if (boton) {
            boton.classList.remove("menu-lateral-activo");
        }
    });

    if (secciones[nombreSeccion]) {
        secciones[nombreSeccion].style.display = "block";
    }

    if (botones[nombreSeccion]) {
        botones[nombreSeccion].classList.add("menu-lateral-activo");
    }
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

        const inputNombre = document.getElementById("nombre");
        const inputEmail = document.getElementById("email");
        const inputRol = document.getElementById("rol");
        const inputTipoCuenta = document.getElementById("tipoCuenta");
        const inputUbicacion = document.getElementById("ubicacion");

        if (inputNombre) inputNombre.value = usuario.nombre || "";
        if (inputEmail) inputEmail.value = usuario.email || "";
        if (inputRol) inputRol.value = usuario.rol || "";
        if (inputTipoCuenta) inputTipoCuenta.value = formatearTipoCuenta(usuario.rol);
        if (inputUbicacion) inputUbicacion.value = usuario.ubicacion || "Sin configurar";

        valoresOriginalesPerfil.nombre = usuario.nombre || "";
        valoresOriginalesPerfil.email = usuario.email || "";
        valoresOriginalesPerfil.rol = usuario.rol || "";

        estadoValidacionPerfil.nombreValido = true;
        estadoValidacionPerfil.emailValido = true;

        limpiarTextoValidacion("nombre");
        limpiarTextoValidacion("email");
        actualizarEstadoBotonGuardarPerfil();

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
        miniRol.textContent = formatearTipoCuenta(usuario.rol);
    }

    if (avatarInicial) {
        const inicial = usuario.nombre && usuario.nombre.trim().length > 0
            ? usuario.nombre.trim().charAt(0).toUpperCase()
            : "U";

        avatarInicial.textContent = inicial;
    }
}

function actualizarNombreMenu(nombreNuevo) {
    const profileNameMenu = document.querySelector("#menu-container #profile-name");

    if (profileNameMenu) {
        profileNameMenu.textContent = nombreNuevo;
    }
}

function formatearTipoCuenta(rol) {
    if (!rol) return "Cuenta estándar";

    if (rol.toUpperCase() === "ADMIN") {
        return "Cuenta administrador";
    }

    return "Cuenta estándar";
}

function configurarFormularioPerfil(usuarioId) {
    const formPerfil = document.getElementById("formPerfil");
    const btnEditar = document.getElementById("btn-editar-perfil");
    const btnCancelar = document.getElementById("btn-cancelar-edicion-perfil");
    const btnGuardar = document.getElementById("btn-guardar-perfil");

    if (btnEditar) {
        btnEditar.addEventListener("click", () => {
            activarModoEdicionPerfil();
        });
    }

    if (btnCancelar) {
        btnCancelar.addEventListener("click", () => {
            restaurarValoresOriginalesPerfil();
            desactivarModoEdicionPerfil();
            limpiarTextoValidacion("nombre");
            limpiarTextoValidacion("email");
            estadoValidacionPerfil.nombreValido = true;
            estadoValidacionPerfil.emailValido = true;
            actualizarEstadoBotonGuardarPerfil();
        });
    }

    if (!formPerfil) return;

    formPerfil.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!modoEdicionPerfil) {
            return;
        }

        const nombre = document.getElementById("nombre").value.trim();
        const email = document.getElementById("email").value.trim();

        const errorValidacion = validarDatosPerfil(nombre, email);
        if (errorValidacion) {
            mostrarMensaje(errorValidacion);
            return;
        }

        if (!estadoValidacionPerfil.nombreValido || !estadoValidacionPerfil.emailValido) {
            mostrarMensaje("Corrige los campos antes de guardar.");
            return;
        }

        const datos = {
            nombre: nombre,
            email: email
        };

        try {
            if (btnGuardar) {
                btnGuardar.disabled = true;
                btnGuardar.textContent = "Guardando...";
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
                throw new Error(obtenerMensajeErrorAmigable(textoRespuesta, "perfil"));
            }

            const usuarioActualizado = textoRespuesta ? JSON.parse(textoRespuesta) : null;

            if (usuarioActualizado) {
                const inputNombre = document.getElementById("nombre");
                const inputEmail = document.getElementById("email");
                const inputRol = document.getElementById("rol");
                const inputTipoCuenta = document.getElementById("tipoCuenta");

                if (inputNombre) inputNombre.value = usuarioActualizado.nombre || "";
                if (inputEmail) inputEmail.value = usuarioActualizado.email || "";
                if (inputRol) inputRol.value = usuarioActualizado.rol || "";
                if (inputTipoCuenta) inputTipoCuenta.value = formatearTipoCuenta(usuarioActualizado.rol);

                valoresOriginalesPerfil.nombre = usuarioActualizado.nombre || "";
                valoresOriginalesPerfil.email = usuarioActualizado.email || "";
                valoresOriginalesPerfil.rol = usuarioActualizado.rol || "";

                actualizarPanelLateral(usuarioActualizado);
                actualizarNombreMenu(usuarioActualizado.nombre);
            }

            mostrarMensaje("Perfil actualizado correctamente.", "ok");
            mostrarValidacionCampo("nombre", "Nombre guardado correctamente", true);
            mostrarValidacionCampo("email", "Email guardado correctamente", true);

            estadoValidacionPerfil.nombreValido = true;
            estadoValidacionPerfil.emailValido = true;

            desactivarModoEdicionPerfil();

        } catch (error) {
            console.error("Error al actualizar perfil:", error);
            mostrarMensaje(error.message || "No se pudo actualizar el perfil.");
        } finally {
            if (btnGuardar) {
                btnGuardar.textContent = "Guardar cambios";
                actualizarEstadoBotonGuardarPerfil();
            }
        }
    });
}

function activarModoEdicionPerfil() {
    modoEdicionPerfil = true;

    const inputNombre = document.getElementById("nombre");
    const inputEmail = document.getElementById("email");
    const btnEditar = document.getElementById("btn-editar-perfil");
    const btnGuardar = document.getElementById("btn-guardar-perfil");
    const btnCancelar = document.getElementById("btn-cancelar-edicion-perfil");

    if (inputNombre) inputNombre.removeAttribute("readonly");
    if (inputEmail) inputEmail.removeAttribute("readonly");

    if (btnEditar) btnEditar.style.display = "none";
    if (btnGuardar) btnGuardar.style.display = "inline-flex";
    if (btnCancelar) btnCancelar.style.display = "inline-flex";

    actualizarEstadoBotonGuardarPerfil();
}

function desactivarModoEdicionPerfil() {
    modoEdicionPerfil = false;

    const inputNombre = document.getElementById("nombre");
    const inputEmail = document.getElementById("email");
    const btnEditar = document.getElementById("btn-editar-perfil");
    const btnGuardar = document.getElementById("btn-guardar-perfil");
    const btnCancelar = document.getElementById("btn-cancelar-edicion-perfil");

    if (inputNombre) inputNombre.setAttribute("readonly", true);
    if (inputEmail) inputEmail.setAttribute("readonly", true);

    if (btnEditar) btnEditar.style.display = "inline-flex";
    if (btnGuardar) btnGuardar.style.display = "none";
    if (btnCancelar) btnCancelar.style.display = "none";
}

function restaurarValoresOriginalesPerfil() {
    const inputNombre = document.getElementById("nombre");
    const inputEmail = document.getElementById("email");
    const inputRol = document.getElementById("rol");
    const inputTipoCuenta = document.getElementById("tipoCuenta");

    if (inputNombre) inputNombre.value = valoresOriginalesPerfil.nombre;
    if (inputEmail) inputEmail.value = valoresOriginalesPerfil.email;
    if (inputRol) inputRol.value = valoresOriginalesPerfil.rol;
    if (inputTipoCuenta) inputTipoCuenta.value = formatearTipoCuenta(valoresOriginalesPerfil.rol);
}

function configurarValidacionEnVivoPerfil(usuarioId) {
    const inputNombre = document.getElementById("nombre");
    const inputEmail = document.getElementById("email");

    if (inputNombre) {
        inputNombre.addEventListener("input", () => {
            if (!modoEdicionPerfil) return;

            clearTimeout(timeoutValidacionNombre);

            const nombre = inputNombre.value.trim();
            const errorLocal = validarNombreLocal(nombre);

            if (errorLocal) {
                mostrarValidacionCampo("nombre", errorLocal, false);
                estadoValidacionPerfil.nombreValido = false;
                actualizarEstadoBotonGuardarPerfil();
                return;
            }

            mostrarValidacionCampo("nombre", "Comprobando disponibilidad...", true);

            timeoutValidacionNombre = setTimeout(async () => {
                await validarNombreEnVivo(usuarioId);
            }, 1000);
        });
    }

    if (inputEmail) {
        inputEmail.addEventListener("input", () => {
            if (!modoEdicionPerfil) return;

            clearTimeout(timeoutValidacionEmail);

            const email = inputEmail.value.trim();
            const errorLocal = validarEmailLocal(email);

            if (errorLocal) {
                mostrarValidacionCampo("email", errorLocal, false);
                estadoValidacionPerfil.emailValido = false;
                actualizarEstadoBotonGuardarPerfil();
                return;
            }

            mostrarValidacionCampo("email", "Comprobando disponibilidad...", true);

            timeoutValidacionEmail = setTimeout(async () => {
                await validarEmailEnVivo(usuarioId);
            }, 1000);
        });
    }
}

async function validarNombreEnVivo(usuarioId) {
    const inputNombre = document.getElementById("nombre");
    if (!inputNombre) return;

    const nombre = inputNombre.value.trim();

    if (nombre.toLowerCase() === valoresOriginalesPerfil.nombre.trim().toLowerCase()) {
        mostrarValidacionCampo("nombre", "Es tu nombre actual", true);
        estadoValidacionPerfil.nombreValido = true;
        actualizarEstadoBotonGuardarPerfil();
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/validar-nombre?nombre=${encodeURIComponent(nombre)}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();
        let data = null;

        try {
            data = texto ? JSON.parse(texto) : null;
        } catch (e) {
            data = null;
        }

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "perfil"));
        }

        const mensaje = data?.mensaje || "Nombre validado";
        const disponible = !!data?.disponible;

        mostrarValidacionCampo("nombre", mensaje, disponible);
        estadoValidacionPerfil.nombreValido = disponible;
        actualizarEstadoBotonGuardarPerfil();

    } catch (error) {
        console.error("Error validando nombre:", error);
        mostrarValidacionCampo("nombre", error.message || "No se pudo validar el nombre", false);
        estadoValidacionPerfil.nombreValido = false;
        actualizarEstadoBotonGuardarPerfil();
    }
}

async function validarEmailEnVivo(usuarioId) {
    const inputEmail = document.getElementById("email");
    if (!inputEmail) return;

    const email = inputEmail.value.trim();

    if (email.toLowerCase() === valoresOriginalesPerfil.email.trim().toLowerCase()) {
        mostrarValidacionCampo("email", "Es tu email actual", true);
        estadoValidacionPerfil.emailValido = true;
        actualizarEstadoBotonGuardarPerfil();
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/usuarios/${usuarioId}/validar-email?email=${encodeURIComponent(email)}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();
        let data = null;

        try {
            data = texto ? JSON.parse(texto) : null;
        } catch (e) {
            data = null;
        }

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "perfil"));
        }

        const mensaje = data?.mensaje || "Email validado";
        const disponible = !!data?.disponible;

        mostrarValidacionCampo("email", mensaje, disponible);
        estadoValidacionPerfil.emailValido = disponible;
        actualizarEstadoBotonGuardarPerfil();

    } catch (error) {
        console.error("Error validando email:", error);
        mostrarValidacionCampo("email", error.message || "No se pudo validar el email", false);
        estadoValidacionPerfil.emailValido = false;
        actualizarEstadoBotonGuardarPerfil();
    }
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

function validarNombreLocal(nombre) {
    if (!nombre) {
        return "El nombre no puede estar vacío.";
    }

    if (nombre.length < 3) {
        return "Debe tener al menos 3 caracteres.";
    }

    if (nombre.length > 30) {
        return "No puede superar los 30 caracteres.";
    }

    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9._\-\s]+$/;
    if (!regexNombre.test(nombre)) {
        return "Contiene caracteres no permitidos.";
    }

    return null;
}

function validarEmailLocal(email) {
    if (!email) {
        return "El email no puede estar vacío.";
    }

    if (email.length > 100) {
        return "El email es demasiado largo.";
    }

    if (!esEmailValido(email)) {
        return "Formato de email no válido.";
    }

    return null;
}

function esEmailValido(email) {
    const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex.test(email);
}

function mostrarValidacionCampo(campo, mensaje, esValido) {
    const elemento = document.getElementById(`validacion-${campo}`);
    if (!elemento) return;

    elemento.textContent = mensaje;
    elemento.classList.remove("ok", "error");
    elemento.classList.add(esValido ? "ok" : "error");
}

function limpiarTextoValidacion(campo) {
    const elemento = document.getElementById(`validacion-${campo}`);
    if (!elemento) return;

    elemento.textContent = "";
    elemento.classList.remove("ok", "error");
}

function actualizarEstadoBotonGuardarPerfil() {
    const btnGuardarPerfil = document.getElementById("btn-guardar-perfil");
    const inputNombre = document.getElementById("nombre");
    const inputEmail = document.getElementById("email");

    if (!btnGuardarPerfil || !inputNombre || !inputEmail) return;

    if (!modoEdicionPerfil) {
        btnGuardarPerfil.disabled = true;
        return;
    }

    const nombre = inputNombre.value.trim();
    const email = inputEmail.value.trim();

    const datosMinimosOk = !validarDatosPerfil(nombre, email);
    const validacionRemotaOk = estadoValidacionPerfil.nombreValido && estadoValidacionPerfil.emailValido;

    btnGuardarPerfil.disabled = !(datosMinimosOk && validacionRemotaOk);
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
            mostrarMensaje("La nueva contraseña y la confirmación no coinciden.");
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
                throw new Error(obtenerMensajeErrorAmigable(texto, "password"));
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
            ocultarMensajeModalTarjeta();
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

        ocultarMensajeModalTarjeta();
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

            ocultarMensajeModalTarjeta();

            const titular = document.getElementById("titularTarjeta").value.trim();
            const numeroTarjeta = document.getElementById("numeroTarjeta").value.trim();
            const fechaExpiracion = document.getElementById("fechaExpiracionTarjeta").value.trim();
            const tipo = document.getElementById("tipoTarjeta").value;

            const errorTarjeta = validarDatosTarjeta(titular, numeroTarjeta, fechaExpiracion, tipo);

            if (errorTarjeta) {
                mostrarMensajeModalTarjeta(errorTarjeta, "error");
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

                const textoRespuesta = await response.text();

                if (!response.ok) {
                    throw new Error(obtenerMensajeErrorAmigable(textoRespuesta, "tarjeta"));
                }

                formTarjeta.reset();
                ocultarMensajeModalTarjeta();

                if (modalTarjeta) {
                    modalTarjeta.style.display = "none";
                }

                mostrarMensaje("Tarjeta guardada correctamente.", "ok");
                await cargarTarjetas(usuarioId);

            } catch (error) {
                console.error("Error al guardar tarjeta:", error);
                mostrarMensajeModalTarjeta(error.message || "No se pudo guardar la tarjeta.", "error");
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

    const partesFecha = fechaExpiracion.split("/");
    const mes = parseInt(partesFecha[0], 10);
    const anio = parseInt(partesFecha[1], 10);

    if (Number.isNaN(mes) || Number.isNaN(anio)) {
        return "La fecha de expiración no es válida.";
    }

    if (mes < 1 || mes > 12) {
        return "El mes de expiración no es válido.";
    }

    const hoy = new Date();
    const anioActual2Digitos = hoy.getFullYear() % 100;
    const mesActual = hoy.getMonth() + 1;

    if (anio < anioActual2Digitos) {
        return "La tarjeta está caducada.";
    }

    if (anio === anioActual2Digitos && mes < mesActual) {
        return "La tarjeta está caducada.";
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
                    throw new Error(obtenerMensajeErrorAmigable(textoError, "tarjeta"));
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

        const textoRespuesta = await response.text();

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(textoRespuesta, "tarjeta"));
        }

        const tarjetas = textoRespuesta ? JSON.parse(textoRespuesta) : [];
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

function mostrarMensaje(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-perfil");

    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.style.display = "block";

    if (tipo === "ok") {
        mensaje.style.backgroundColor = "#ecfdf3";
        mensaje.style.color = "#166534";
        mensaje.style.border = "1px solid #a7f3c0";
    } else {
        mensaje.style.backgroundColor = "#fff1f2";
        mensaje.style.color = "#b42318";
        mensaje.style.border = "1px solid #fecdd3";
    }

    clearTimeout(timeoutMensajePerfil);

    timeoutMensajePerfil = setTimeout(() => {
        mensaje.style.display = "none";
    }, 4000);
}

function mostrarMensajeModalTarjeta(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-modal-tarjeta");
    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.style.display = "block";
    mensaje.classList.remove("ok", "error");
    mensaje.classList.add(tipo);
}

function ocultarMensajeModalTarjeta() {
    const mensaje = document.getElementById("mensaje-modal-tarjeta");
    if (!mensaje) return;

    mensaje.textContent = "";
    mensaje.style.display = "none";
    mensaje.classList.remove("ok", "error");
}

function obtenerMensajeErrorAmigable(textoError, contexto = "") {
    if (!textoError) {
        return "No se pudo completar la operación.";
    }

    let json = null;

    try {
        json = JSON.parse(textoError);
    } catch (e) {
        json = null;
    }

    const texto = json
        ? `${json.error || ""} ${json.message || ""} ${json.path || ""}`.toLowerCase()
        : textoError.toLowerCase();

    if (contexto === "tarjeta") {
        if (texto.includes("mes")) {
            return "El mes de expiración no es válido.";
        }

        if (texto.includes("caduc")) {
            return "La tarjeta está caducada.";
        }

        if (texto.includes("fecha")) {
            return "La fecha de expiración no es válida.";
        }

        if (texto.includes("titular")) {
            return "El titular de la tarjeta no es válido.";
        }

        if (texto.includes("número") || texto.includes("numero")) {
            return "El número de tarjeta no es válido.";
        }

        return "No se pudo guardar la tarjeta.";
    }

    if (contexto === "perfil") {
        if (texto.includes("nombre de usuario ya está en uso") || texto.includes("nombre ya está en uso")) {
            return "Ese nombre de usuario ya está en uso.";
        }

        if (texto.includes("email ya está en uso")) {
            return "Ese correo electrónico ya está en uso.";
        }

        return "No se pudo actualizar el perfil.";
    }

    if (contexto === "password") {
        if (texto.includes("contraseña actual no es correcta")) {
            return "La contraseña actual no es correcta.";
        }

        if (texto.includes("no coinciden")) {
            return "La nueva contraseña y la confirmación no coinciden.";
        }

        return "No se pudo actualizar la contraseña.";
    }

    return "Ha ocurrido un error inesperado.";
}