document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaPerfil();
});

let tarjetaIdPendienteEliminar = null;
let direccionIdPendienteEliminar = null;
let direccionIdEnEdicion = null;

let timeoutValidacionNombre = null;
let timeoutValidacionEmail = null;
let timeoutMensajePerfil = null;

let modoEdicionPerfil = false;

const estadoValidacionPerfil = {
    nombreValido: true,
    emailValido: true
};

const valoresOriginalesPerfil = {
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

    configurarDirecciones(sesion.id);
    configurarModalEliminarDireccion(sesion.id);

    await cargarPerfil(sesion.id);
    await cargarTarjetas(sesion.id);
    await cargarDirecciones(sesion.id);

    activarSeccion("perfil");
    desactivarModoEdicionPerfil();
}

async function obtenerSesionActual() {
    try {
        const response = await fetch(`${BASE_URL}/auth/session`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) return null;
        return await response.json();
    } catch (error) {
        console.error("Error al comprobar sesión:", error);
        return null;
    }
}

function configurarNavegacionSecciones() {
    const botones = {
        perfil: document.getElementById("btn-menu-perfil"),
        password: document.getElementById("btn-menu-password"),
        direcciones: document.getElementById("btn-menu-direcciones"),
        tarjetas: document.getElementById("btn-menu-tarjetas")
    };

    Object.entries(botones).forEach(([seccion, boton]) => {
        if (!boton) return;
        boton.addEventListener("click", () => activarSeccion(seccion));
    });
}

function activarSeccion(nombreSeccion) {
    const secciones = {
        perfil: document.getElementById("seccion-perfil"),
        password: document.getElementById("seccion-password"),
        direcciones: document.getElementById("seccion-direcciones"),
        tarjetas: document.getElementById("seccion-tarjetas")
    };

    const botones = {
        perfil: document.getElementById("btn-menu-perfil"),
        password: document.getElementById("btn-menu-password"),
        direcciones: document.getElementById("btn-menu-direcciones"),
        tarjetas: document.getElementById("btn-menu-tarjetas")
    };

    Object.values(secciones).forEach((seccion) => {
        if (seccion) seccion.classList.add("hidden");
    });

    Object.values(botones).forEach((boton) => {
        if (boton) boton.classList.remove("perfil-nav-btn-active");
    });

    if (secciones[nombreSeccion]) {
        secciones[nombreSeccion].classList.remove("hidden");
    }

    if (botones[nombreSeccion]) {
        botones[nombreSeccion].classList.add("perfil-nav-btn-active");
    }
}

async function cargarPerfil(usuarioId) {
    try {
        const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}/perfil`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el perfil");
        }

        const usuario = await response.json();

        asignarValor("nombre", usuario.nombre || "");
        asignarValor("email", usuario.email || "");
        asignarValor("rol", usuario.rol || "");
        asignarValor("tipoCuenta", formatearTipoCuenta(usuario.rol));

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
    const miniRolResumen = document.getElementById("mini-rol-resumen");
    const avatarInicial = document.getElementById("avatar-inicial");

    if (nombreLateral) nombreLateral.textContent = usuario.nombre || "Usuario";
    if (miniEmail) miniEmail.textContent = usuario.email || "";

    const tipoCuenta = formatearTipoCuenta(usuario.rol);

    if (miniRol) miniRol.textContent = tipoCuenta;
    if (miniRolResumen) miniRolResumen.textContent = tipoCuenta;

    if (avatarInicial) {
        const inicial = usuario.nombre && usuario.nombre.trim()
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
    return rol.toUpperCase() === "ADMIN" ? "Cuenta administrador" : "Cuenta estándar";
}

function configurarFormularioPerfil(usuarioId) {
    const formPerfil = document.getElementById("formPerfil");
    const btnEditar = document.getElementById("btn-editar-perfil");
    const btnCancelar = document.getElementById("btn-cancelar-edicion-perfil");
    const btnGuardar = document.getElementById("btn-guardar-perfil");

    if (btnEditar) {
        btnEditar.addEventListener("click", () => activarModoEdicionPerfil());
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

        if (!modoEdicionPerfil) return;

        const nombre = obtenerValor("nombre");
        const email = obtenerValor("email");

        const errorValidacion = validarDatosPerfil(nombre, email);
        if (errorValidacion) {
            mostrarMensaje(errorValidacion);
            return;
        }

        if (!estadoValidacionPerfil.nombreValido || !estadoValidacionPerfil.emailValido) {
            mostrarMensaje("Corrige los campos antes de guardar.");
            return;
        }

        try {
            if (btnGuardar) {
                btnGuardar.disabled = true;
                btnGuardar.textContent = "Guardando...";
            }

            const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}/perfil`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ nombre, email })
            });

            const textoRespuesta = await response.text();

            if (!response.ok) {
                throw new Error(obtenerMensajeErrorAmigable(textoRespuesta, "perfil"));
            }

            const usuarioActualizado = textoRespuesta ? JSON.parse(textoRespuesta) : null;

            if (usuarioActualizado) {
                asignarValor("nombre", usuarioActualizado.nombre || "");
                asignarValor("email", usuarioActualizado.email || "");
                asignarValor("rol", usuarioActualizado.rol || "");
                asignarValor("tipoCuenta", formatearTipoCuenta(usuarioActualizado.rol));

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

    setReadOnly("nombre", false);
    setReadOnly("email", false);

    toggleHidden("btn-editar-perfil", true);
    toggleHidden("btn-guardar-perfil", false);
    toggleHidden("btn-cancelar-edicion-perfil", false);

    actualizarEstadoBotonGuardarPerfil();
}

function desactivarModoEdicionPerfil() {
    modoEdicionPerfil = false;

    setReadOnly("nombre", true);
    setReadOnly("email", true);

    toggleHidden("btn-editar-perfil", false);
    toggleHidden("btn-guardar-perfil", true);
    toggleHidden("btn-cancelar-edicion-perfil", true);
}

function restaurarValoresOriginalesPerfil() {
    asignarValor("nombre", valoresOriginalesPerfil.nombre);
    asignarValor("email", valoresOriginalesPerfil.email);
    asignarValor("rol", valoresOriginalesPerfil.rol);
    asignarValor("tipoCuenta", formatearTipoCuenta(valoresOriginalesPerfil.rol));
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
            }, 800);
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
            }, 800);
        });
    }
}

async function validarNombreEnVivo(usuarioId) {
    const nombre = obtenerValor("nombre");

    if (nombre.toLowerCase() === valoresOriginalesPerfil.nombre.trim().toLowerCase()) {
        mostrarValidacionCampo("nombre", "Es tu nombre actual", true);
        estadoValidacionPerfil.nombreValido = true;
        actualizarEstadoBotonGuardarPerfil();
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}/validar-nombre?nombre=${encodeURIComponent(nombre)}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();
        const data = texto ? JSON.parse(texto) : null;

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "perfil"));
        }

        mostrarValidacionCampo("nombre", data?.mensaje || "Nombre validado", !!data?.disponible);
        estadoValidacionPerfil.nombreValido = !!data?.disponible;
        actualizarEstadoBotonGuardarPerfil();
    } catch (error) {
        console.error("Error validando nombre:", error);
        mostrarValidacionCampo("nombre", error.message || "No se pudo validar el nombre", false);
        estadoValidacionPerfil.nombreValido = false;
        actualizarEstadoBotonGuardarPerfil();
    }
}

async function validarEmailEnVivo(usuarioId) {
    const email = obtenerValor("email");

    if (email.toLowerCase() === valoresOriginalesPerfil.email.trim().toLowerCase()) {
        mostrarValidacionCampo("email", "Es tu email actual", true);
        estadoValidacionPerfil.emailValido = true;
        actualizarEstadoBotonGuardarPerfil();
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}/validar-email?email=${encodeURIComponent(email)}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();
        const data = texto ? JSON.parse(texto) : null;

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "perfil"));
        }

        mostrarValidacionCampo("email", data?.mensaje || "Email validado", !!data?.disponible);
        estadoValidacionPerfil.emailValido = !!data?.disponible;
        actualizarEstadoBotonGuardarPerfil();
    } catch (error) {
        console.error("Error validando email:", error);
        mostrarValidacionCampo("email", error.message || "No se pudo validar el email", false);
        estadoValidacionPerfil.emailValido = false;
        actualizarEstadoBotonGuardarPerfil();
    }
}

function validarDatosPerfil(nombre, email) {
    if (!nombre || !email) return "Nombre y email son obligatorios.";
    if (nombre.length < 3) return "El nombre debe tener al menos 3 caracteres.";
    if (nombre.length > 30) return "El nombre no puede superar los 30 caracteres.";

    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9._\-\s]+$/;
    if (!regexNombre.test(nombre)) return "El nombre contiene caracteres no permitidos.";

    if (!esEmailValido(email)) return "Introduce un email válido.";
    if (email.length > 100) return "El email es demasiado largo.";

    return null;
}

function validarNombreLocal(nombre) {
    if (!nombre) return "El nombre no puede estar vacío.";
    if (nombre.length < 3) return "Debe tener al menos 3 caracteres.";
    if (nombre.length > 30) return "No puede superar los 30 caracteres.";

    const regexNombre = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9._\-\s]+$/;
    if (!regexNombre.test(nombre)) return "Contiene caracteres no permitidos.";

    return null;
}

function validarEmailLocal(email) {
    if (!email) return "El email no puede estar vacío.";
    if (email.length > 100) return "El email es demasiado largo.";
    if (!esEmailValido(email)) return "Formato de email no válido.";
    return null;
}

function esEmailValido(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
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

        const passwordActual = obtenerValor("passwordActual");
        const passwordNueva = obtenerValor("passwordNueva");
        const confirmarPassword = obtenerValor("confirmarPassword");

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

        try {
            const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}/password`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ passwordActual, passwordNueva, confirmarPassword })
            });

            const texto = await response.text();

            if (!response.ok) {
                throw new Error(obtenerMensajeErrorAmigable(texto, "password"));
            }

            formPassword.reset();
            mostrarMensaje("Contraseña actualizada correctamente.", "ok");
        } catch (error) {
            console.error("Error al cambiar contraseña:", error);
            mostrarMensaje(error.message || "No se pudo actualizar la contraseña.");
        }
    });
}

/* TARJETAS */

function configurarTarjetas(usuarioId) {
    const btnAnadirTarjeta = document.getElementById("btn-anadir-tarjeta");
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");
    const modalTarjeta = document.getElementById("modal-tarjeta");
    const cerrarModalTarjeta = document.getElementById("cerrar-modal-tarjeta");
    const cancelarModalTarjeta = document.getElementById("cancelar-modal-tarjeta");
    const formTarjeta = document.getElementById("formTarjeta");
    const btnCambiarFoto = document.getElementById("btn-cambiar-foto");

    function abrirModalTarjeta() {
        ocultarMensajeModalTarjeta();
        mostrarElemento(modalTarjeta);
    }

    function cerrarModalTarjetaLocal() {
        ocultarElemento(modalTarjeta);
        if (formTarjeta) formTarjeta.reset();
        ocultarMensajeModalTarjeta();
    }

    if (btnAnadirTarjeta) btnAnadirTarjeta.addEventListener("click", abrirModalTarjeta);
    if (cerrarModalTarjeta) cerrarModalTarjeta.addEventListener("click", cerrarModalTarjetaLocal);
    if (cancelarModalTarjeta) cancelarModalTarjeta.addEventListener("click", cerrarModalTarjetaLocal);

    if (modalTarjeta) {
        modalTarjeta.addEventListener("click", (e) => {
            if (e.target === modalTarjeta) cerrarModalTarjetaLocal();
        });
    }

    if (btnCambiarFoto) {
        btnCambiarFoto.addEventListener("click", () => {
            mostrarMensaje("La funcionalidad de cambiar foto la hacemos después.", "ok");
        });
    }

    if (contenedorTarjetas) {
        contenedorTarjetas.addEventListener("click", (e) => {
            const bloqueAnadir = e.target.closest("[data-action='anadir-tarjeta']");
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

            const titular = obtenerValor("titularTarjeta");
            const numeroTarjeta = obtenerValor("numeroTarjeta");
            const fechaExpiracion = obtenerValor("fechaExpiracionTarjeta");
            const tipo = obtenerValor("tipoTarjeta");

            const errorTarjeta = validarDatosTarjeta(titular, numeroTarjeta, fechaExpiracion, tipo);
            if (errorTarjeta) {
                mostrarMensajeModalTarjeta(errorTarjeta, "error");
                return;
            }

            try {
                const response = await fetch(`${BASE_URL}/tarjetas/usuario/${usuarioId}`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify({
                        titular,
                        numeroTarjeta: numeroTarjeta.replace(/\s+/g, ""),
                        fechaExpiracion,
                        tipo
                    })
                });

                const textoRespuesta = await response.text();

                if (!response.ok) {
                    throw new Error(obtenerMensajeErrorAmigable(textoRespuesta, "tarjeta"));
                }

                formTarjeta.reset();
                ocultarElemento(modalTarjeta);
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

    if (titular.length < 3) return "El nombre del titular es demasiado corto.";

    const numeroLimpio = numeroTarjeta.replace(/\s+/g, "");
    if (!/^\d{16}$/.test(numeroLimpio)) return "El número de tarjeta debe tener 16 dígitos.";
    if (!/^\d{2}\/\d{2}$/.test(fechaExpiracion)) return "La fecha de expiración debe tener formato MM/AA.";

    const [mesTexto, anioTexto] = fechaExpiracion.split("/");
    const mes = parseInt(mesTexto, 10);
    const anio = parseInt(anioTexto, 10);

    if (Number.isNaN(mes) || Number.isNaN(anio)) return "La fecha de expiración no es válida.";
    if (mes < 1 || mes > 12) return "El mes de expiración no es válido.";

    const hoy = new Date();
    const anioActual2Digitos = hoy.getFullYear() % 100;
    const mesActual = hoy.getMonth() + 1;

    if (anio < anioActual2Digitos) return "La tarjeta está caducada.";
    if (anio === anioActual2Digitos && mes < mesActual) return "La tarjeta está caducada.";

    return null;
}

function formatearNumeroTarjetaInput(valor) {
    const soloNumeros = valor.replace(/\D/g, "").slice(0, 16);
    return soloNumeros.replace(/(\d{4})(?=\d)/g, "$1 ");
}

function formatearFechaExpiracionInput(valor) {
    const soloNumeros = valor.replace(/\D/g, "").slice(0, 4);
    if (soloNumeros.length <= 2) return soloNumeros;
    return `${soloNumeros.slice(0, 2)}/${soloNumeros.slice(2)}`;
}

function configurarModalEliminarTarjeta(usuarioId) {
    const modalEliminar = document.getElementById("modal-confirmar-eliminar");
    const cerrarModalEliminar = document.getElementById("cerrar-modal-eliminar");
    const cancelarEliminar = document.getElementById("cancelar-eliminar-tarjeta");
    const confirmarEliminar = document.getElementById("confirmar-eliminar-tarjeta");

    function cerrarModal() {
        ocultarElemento(modalEliminar);
        tarjetaIdPendienteEliminar = null;
    }

    if (cerrarModalEliminar) cerrarModalEliminar.addEventListener("click", cerrarModal);
    if (cancelarEliminar) cancelarEliminar.addEventListener("click", cerrarModal);

    if (modalEliminar) {
        modalEliminar.addEventListener("click", (e) => {
            if (e.target === modalEliminar) cerrarModal();
        });
    }

    if (confirmarEliminar) {
        confirmarEliminar.addEventListener("click", async () => {
            if (!tarjetaIdPendienteEliminar) return;

            try {
                confirmarEliminar.disabled = true;
                confirmarEliminar.textContent = "Eliminando...";

                const response = await fetch(`${BASE_URL}/tarjetas/${tarjetaIdPendienteEliminar}`, {
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
    tarjetaIdPendienteEliminar = tarjetaId;
    mostrarElemento(document.getElementById("modal-confirmar-eliminar"));
}

async function cargarTarjetas(usuarioId) {
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");
    if (!contenedorTarjetas) return;

    try {
        const response = await fetch(`${BASE_URL}/tarjetas/usuario/${usuarioId}`, {
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
        renderizarEstadoVacioTarjetas("No se pudieron cargar las tarjetas");
    }
}

function renderizarTarjetas(tarjetas) {
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");
    if (!contenedorTarjetas) return;

    limpiarContenedor(contenedorTarjetas);

    if (!Array.isArray(tarjetas) || tarjetas.length === 0) {
        contenedorTarjetas.appendChild(crearTarjetaAgregar());
        return;
    }

    tarjetas.forEach((tarjeta, index) => {
        contenedorTarjetas.appendChild(crearTarjetaPago(tarjeta, index));
    });

    contenedorTarjetas.appendChild(crearTarjetaAgregar());
}

function crearTarjetaPago(tarjeta, index) {
    const variantes = ["card-black", "card-rose", "card-gold"];
    const variante = variantes[index % variantes.length];

    const article = document.createElement("article");
    article.className = `payment-card ${variante}`;

    const btnEliminar = document.createElement("button");
    btnEliminar.type = "button";
    btnEliminar.className = "btn-eliminar-tarjeta";
    btnEliminar.dataset.id = tarjeta.id;
    btnEliminar.textContent = "Eliminar";

    const top = document.createElement("div");
    top.className = "payment-top";

    const type = document.createElement("div");
    type.className = "payment-type";
    type.textContent = formatearTipoTarjeta(tarjeta.tipo);

    const chip = document.createElement("div");
    chip.className = "chip-card";

    top.append(type, chip);

    const numero = document.createElement("div");
    numero.className = "payment-number";
    numero.textContent = tarjeta.numeroEnmascarado || "**** **** **** 0000";

    const bottom = document.createElement("div");
    bottom.className = "payment-bottom";

    const titularBox = document.createElement("div");
    const titularSmall = document.createElement("small");
    titularSmall.textContent = "Titular";
    const titularStrong = document.createElement("strong");
    titularStrong.textContent = tarjeta.titular || "Usuario";
    titularBox.append(titularSmall, titularStrong);

    const fechaBox = document.createElement("div");
    const fechaSmall = document.createElement("small");
    fechaSmall.textContent = "Expira";
    const fechaStrong = document.createElement("strong");
    fechaStrong.textContent = tarjeta.fechaExpiracion || "--/--";
    fechaBox.append(fechaSmall, fechaStrong);

    bottom.append(titularBox, fechaBox);

    const tag = document.createElement("div");
    tag.className = "tag-primary";
    tag.textContent = index === 0 ? "Preferida" : "Guardada";

    article.append(btnEliminar, top, numero, bottom, tag);
    return article;
}

function crearTarjetaAgregar() {
    const box = document.createElement("button");
    box.type = "button";
    box.className = "add-card";
    box.dataset.action = "anadir-tarjeta";
    box.textContent = "Añadir nueva tarjeta de pago";
    return box;
}

function renderizarEstadoVacioTarjetas(texto) {
    const contenedorTarjetas = document.getElementById("contenedor-tarjetas");
    if (!contenedorTarjetas) return;

    limpiarContenedor(contenedorTarjetas);

    const box = document.createElement("div");
    box.className = "add-card";
    box.textContent = texto;
    contenedorTarjetas.appendChild(box);
}

function formatearTipoTarjeta(tipo) {
    if (!tipo) return "Tarjeta";
    if (tipo.toUpperCase() === "VISA") return "Visa";
    if (tipo.toUpperCase() === "MASTERCARD") return "Mastercard";
    return tipo;
}

/* DIRECCIONES */

function configurarDirecciones(usuarioId) {
    const btnAnadirDireccion = document.getElementById("btn-anadir-direccion");
    const contenedorDirecciones = document.getElementById("contenedor-direcciones");
    const modalDireccion = document.getElementById("modal-direccion");
    const cerrarModalDireccion = document.getElementById("cerrar-modal-direccion");
    const cancelarModalDireccion = document.getElementById("cancelar-modal-direccion");
    const formDireccion = document.getElementById("formDireccion");

    function abrirModalDireccionNueva() {
        direccionIdEnEdicion = null;
        if (formDireccion) formDireccion.reset();
        asignarTexto("titulo-modal-direccion", "Añadir dirección");
        setChecked("principalDireccion", false);
        ocultarMensajeModalDireccion();
        mostrarElemento(modalDireccion);
    }

    function cerrarModalDireccionLocal() {
        direccionIdEnEdicion = null;
        if (formDireccion) formDireccion.reset();
        ocultarMensajeModalDireccion();
        asignarTexto("titulo-modal-direccion", "Añadir dirección");
        ocultarElemento(modalDireccion);
    }

    if (btnAnadirDireccion) btnAnadirDireccion.addEventListener("click", abrirModalDireccionNueva);
    if (cerrarModalDireccion) cerrarModalDireccion.addEventListener("click", cerrarModalDireccionLocal);
    if (cancelarModalDireccion) cancelarModalDireccion.addEventListener("click", cerrarModalDireccionLocal);

    if (modalDireccion) {
        modalDireccion.addEventListener("click", (e) => {
            if (e.target === modalDireccion) cerrarModalDireccionLocal();
        });
    }

    if (contenedorDirecciones) {
        contenedorDirecciones.addEventListener("click", async (e) => {
            const btnEditar = e.target.closest(".btn-editar-direccion");
            const btnEliminar = e.target.closest(".btn-eliminar-direccion");
            const btnPrincipal = e.target.closest(".btn-principal-direccion");
            const bloqueAnadir = e.target.closest("[data-action='anadir-direccion']");

            if (bloqueAnadir) {
                abrirModalDireccionNueva();
                return;
            }

            if (btnEditar) {
                const direccionId = btnEditar.dataset.id;
                if (!direccionId) return;
                await abrirModalEditarDireccion(usuarioId, direccionId);
                return;
            }

            if (btnEliminar) {
                const direccionId = btnEliminar.dataset.id;
                if (!direccionId) return;
                abrirModalEliminarDireccion(direccionId);
                return;
            }

            if (btnPrincipal) {
                const direccionId = btnPrincipal.dataset.id;
                if (!direccionId) return;
                await marcarDireccionComoPrincipal(usuarioId, direccionId);
            }
        });
    }

    if (formDireccion) {
        formDireccion.addEventListener("submit", async (e) => {
            e.preventDefault();

            ocultarMensajeModalDireccion();

            const datos = obtenerDatosFormularioDireccion();
            const error = validarDatosDireccion(datos);

            if (error) {
                mostrarMensajeModalDireccion(error, "error");
                return;
            }

            try {
                let response;

                if (direccionIdEnEdicion) {
                    response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}/${direccionIdEnEdicion}`, {
                        method: "PUT",
                        headers: { "Content-Type": "application/json" },
                        credentials: "include",
                        body: JSON.stringify(datos)
                    });
                } else {
                    response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}`, {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        credentials: "include",
                        body: JSON.stringify(datos)
                    });
                }

                const texto = await response.text();

                if (!response.ok) {
                    throw new Error(obtenerMensajeErrorAmigable(texto, "direccion"));
                }

                cerrarModalDireccionLocal();
                mostrarMensaje(
                    direccionIdEnEdicion ? "Dirección actualizada correctamente." : "Dirección añadida correctamente.",
                    "ok"
                );
                await cargarDirecciones(usuarioId);
            } catch (error) {
                console.error("Error guardando dirección:", error);
                mostrarMensajeModalDireccion(error.message || "No se pudo guardar la dirección.", "error");
            }
        });
    }
}

function obtenerDatosFormularioDireccion() {
    return {
        alias: obtenerValor("aliasDireccion"),
        provincia: obtenerValor("provinciaDireccion"),
        municipio: obtenerValor("municipioDireccion"),
        calle: obtenerValor("calleDireccion"),
        numero: obtenerValor("numeroDireccionModal"),
        piso: obtenerValor("pisoDireccion"),
        puerta: obtenerValor("puertaDireccion"),
        codigoPostal: obtenerValor("codigoPostalDireccion"),
        principal: document.getElementById("principalDireccion")?.checked || false
    };
}

function validarDatosDireccion(datos) {
    if (!datos.alias) return "El alias de la dirección es obligatorio.";
    if (!datos.provincia) return "La provincia es obligatoria.";
    if (!datos.municipio) return "El municipio es obligatorio.";
    if (!datos.calle) return "La calle es obligatoria.";
    if (!datos.numero) return "El número es obligatorio.";
    if (!datos.codigoPostal) return "El código postal es obligatorio.";
    if (datos.alias.length < 2) return "El alias es demasiado corto.";
    if (datos.codigoPostal.length !== 5 || !/^\d{5}$/.test(datos.codigoPostal)) {
        return "El código postal debe tener 5 dígitos.";
    }
    return null;
}

async function cargarDirecciones(usuarioId) {
    const contenedorDirecciones = document.getElementById("contenedor-direcciones");
    if (!contenedorDirecciones) return;

    try {
        const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "direccion"));
        }

        const direcciones = texto ? JSON.parse(texto) : [];
        renderizarDirecciones(direcciones);
    } catch (error) {
        console.error("Error al cargar direcciones:", error);
        renderizarEstadoVacioDirecciones("No se pudieron cargar las direcciones");
    }
}

function renderizarDirecciones(direcciones) {
    const contenedorDirecciones = document.getElementById("contenedor-direcciones");
    if (!contenedorDirecciones) return;

    limpiarContenedor(contenedorDirecciones);

    if (!Array.isArray(direcciones) || direcciones.length === 0) {
        contenedorDirecciones.appendChild(crearDireccionAgregar());
        return;
    }

    direcciones.forEach((direccion) => {
        contenedorDirecciones.appendChild(crearDireccionCard(direccion));
    });

    contenedorDirecciones.appendChild(crearDireccionAgregar());
}

function crearDireccionCard(direccion) {
    const card = document.createElement("article");
    card.className = "address-card";

    const top = document.createElement("div");
    top.className = "address-top";

    const topLeft = document.createElement("div");
    const titulo = document.createElement("h4");
    titulo.textContent = direccion.alias || "Dirección";
    const subtitulo = document.createElement("p");
    subtitulo.textContent = direccion.principal
        ? "Dirección principal para tus pedidos"
        : "Dirección guardada";
    topLeft.append(titulo, subtitulo);

    top.appendChild(topLeft);

    if (direccion.principal) {
        const tag = document.createElement("span");
        tag.className = "tag-ok";
        tag.textContent = "Principal";
        top.appendChild(tag);
    }

    const lineas = document.createElement("div");
    lineas.className = "direccion-lineas";

    const linea1 = document.createElement("p");
    linea1.textContent = construirLineaDireccion(direccion);

    const linea2 = document.createElement("p");
    linea2.textContent = `${direccion.codigoPostal || ""} ${direccion.municipio || ""}, ${direccion.provincia || ""}`.trim();

    lineas.append(linea1, linea2);

    if (direccion.puerta) {
        const linea3 = document.createElement("p");
        linea3.textContent = `Puerta: ${direccion.puerta}`;
        lineas.appendChild(linea3);
    }

    const acciones = document.createElement("div");
    acciones.className = "direccion-acciones";

    const btnEditar = document.createElement("button");
    btnEditar.type = "button";
    btnEditar.className = "btn-editar-direccion";
    btnEditar.dataset.id = direccion.id;
    btnEditar.textContent = "Editar";

    acciones.appendChild(btnEditar);

    if (direccion.principal) {
        const textoPrincipal = document.createElement("span");
        textoPrincipal.className = "texto-principal-actual";
        textoPrincipal.textContent = "Dirección principal actual";
        acciones.appendChild(textoPrincipal);
    } else {
        const btnPrincipal = document.createElement("button");
        btnPrincipal.type = "button";
        btnPrincipal.className = "btn-principal-direccion";
        btnPrincipal.dataset.id = direccion.id;
        btnPrincipal.textContent = "Marcar principal";
        acciones.appendChild(btnPrincipal);
    }

    const btnEliminar = document.createElement("button");
    btnEliminar.type = "button";
    btnEliminar.className = "btn-eliminar-direccion";
    btnEliminar.dataset.id = direccion.id;
    btnEliminar.textContent = "Eliminar";
    acciones.appendChild(btnEliminar);

    card.append(top, lineas, acciones);
    return card;
}

function crearDireccionAgregar() {
    const box = document.createElement("button");
    box.type = "button";
    box.className = "add-card-direccion";
    box.dataset.action = "anadir-direccion";
    box.textContent = "Añadir nueva dirección";
    return box;
}

function renderizarEstadoVacioDirecciones(texto) {
    const contenedorDirecciones = document.getElementById("contenedor-direcciones");
    if (!contenedorDirecciones) return;

    limpiarContenedor(contenedorDirecciones);

    const box = document.createElement("div");
    box.className = "add-card-direccion";
    box.textContent = texto;
    contenedorDirecciones.appendChild(box);
}

function construirLineaDireccion(direccion) {
    const partes = [];
    if (direccion.calle) partes.push(direccion.calle);

    const numero = [];
    if (direccion.numero) numero.push(`Nº ${direccion.numero}`);
    if (direccion.piso) numero.push(`Piso ${direccion.piso}`);
    if (direccion.puerta) numero.push(`Puerta ${direccion.puerta}`);

    if (numero.length > 0) {
        partes.push(numero.join(" · "));
    }

    return partes.join(", ");
}

async function abrirModalEditarDireccion(usuarioId, direccionId) {
    const modal = document.getElementById("modal-direccion");

    try {
        const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        const texto = await response.text();

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "direccion"));
        }

        const direcciones = texto ? JSON.parse(texto) : [];
        const direccion = direcciones.find((d) => String(d.id) === String(direccionId));

        if (!direccion) throw new Error("No se encontró la dirección seleccionada.");

        direccionIdEnEdicion = direccion.id;

        asignarTexto("titulo-modal-direccion", "Editar dirección");
        asignarValor("aliasDireccion", direccion.alias || "");
        asignarValor("provinciaDireccion", direccion.provincia || "");
        asignarValor("municipioDireccion", direccion.municipio || "");
        asignarValor("calleDireccion", direccion.calle || "");
        asignarValor("numeroDireccionModal", direccion.numero || "");
        asignarValor("pisoDireccion", direccion.piso || "");
        asignarValor("puertaDireccion", direccion.puerta || "");
        asignarValor("codigoPostalDireccion", direccion.codigoPostal || "");
        setChecked("principalDireccion", !!direccion.principal);

        ocultarMensajeModalDireccion();
        mostrarElemento(modal);
    } catch (error) {
        console.error("Error al abrir edición de dirección:", error);
        mostrarMensaje(error.message || "No se pudo cargar la dirección.");
    }
}

async function marcarDireccionComoPrincipal(usuarioId, direccionId) {
    try {
        const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}/${direccionId}/principal`, {
            method: "PUT",
            credentials: "include"
        });

        const texto = await response.text();

        if (!response.ok) {
            throw new Error(obtenerMensajeErrorAmigable(texto, "direccion"));
        }

        mostrarMensaje("Dirección principal actualizada.", "ok");
        await cargarDirecciones(usuarioId);
    } catch (error) {
        console.error("Error marcando dirección principal:", error);
        mostrarMensaje(error.message || "No se pudo marcar la dirección como principal.");
    }
}

function configurarModalEliminarDireccion(usuarioId) {
    const modal = document.getElementById("modal-confirmar-eliminar-direccion");
    const btnCerrar = document.getElementById("cerrar-modal-eliminar-direccion");
    const btnCancelar = document.getElementById("cancelar-eliminar-direccion");
    const btnConfirmar = document.getElementById("confirmar-eliminar-direccion");

    function cerrarModal() {
        ocultarElemento(modal);
        direccionIdPendienteEliminar = null;
    }

    if (btnCerrar) btnCerrar.addEventListener("click", cerrarModal);
    if (btnCancelar) btnCancelar.addEventListener("click", cerrarModal);

    if (modal) {
        modal.addEventListener("click", (e) => {
            if (e.target === modal) cerrarModal();
        });
    }

    if (btnConfirmar) {
        btnConfirmar.addEventListener("click", async () => {
            if (!direccionIdPendienteEliminar) return;

            try {
                btnConfirmar.disabled = true;
                btnConfirmar.textContent = "Eliminando...";

                const response = await fetch(`${BASE_URL}/direcciones/usuario/${usuarioId}/${direccionIdPendienteEliminar}`, {
                    method: "DELETE",
                    credentials: "include"
                });

                const texto = await response.text();

                if (!response.ok) {
                    throw new Error(obtenerMensajeErrorAmigable(texto, "direccion"));
                }

                cerrarModal();
                mostrarMensaje("Dirección eliminada correctamente.", "ok");
                await cargarDirecciones(usuarioId);
            } catch (error) {
                console.error("Error al eliminar dirección:", error);
                mostrarMensaje(error.message || "No se pudo eliminar la dirección.");
            } finally {
                btnConfirmar.disabled = false;
                btnConfirmar.textContent = "Eliminar";
            }
        });
    }
}

function abrirModalEliminarDireccion(direccionId) {
    direccionIdPendienteEliminar = direccionId;
    mostrarElemento(document.getElementById("modal-confirmar-eliminar-direccion"));
}

function mostrarMensajeModalDireccion(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-modal-direccion");
    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.classList.remove("hidden", "ok", "error");
    mensaje.classList.add(tipo);
}

function ocultarMensajeModalDireccion() {
    const mensaje = document.getElementById("mensaje-modal-direccion");
    if (!mensaje) return;

    mensaje.textContent = "";
    mensaje.classList.add("hidden");
    mensaje.classList.remove("ok", "error");
}

/* MENSAJES */

function mostrarMensaje(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-perfil");
    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.classList.remove("hidden", "ok", "error");
    mensaje.classList.add(tipo === "ok" ? "ok" : "error");

    clearTimeout(timeoutMensajePerfil);

    timeoutMensajePerfil = setTimeout(() => {
        mensaje.classList.add("hidden");
    }, 4000);
}

function mostrarMensajeModalTarjeta(texto, tipo = "error") {
    const mensaje = document.getElementById("mensaje-modal-tarjeta");
    if (!mensaje) return;

    mensaje.textContent = texto;
    mensaje.classList.remove("hidden", "ok", "error");
    mensaje.classList.add(tipo);
}

function ocultarMensajeModalTarjeta() {
    const mensaje = document.getElementById("mensaje-modal-tarjeta");
    if (!mensaje) return;

    mensaje.textContent = "";
    mensaje.classList.add("hidden");
    mensaje.classList.remove("ok", "error");
}

/* ERRORES */

function obtenerMensajeErrorAmigable(textoError, contexto = "") {
    if (!textoError) return "No se pudo completar la operación.";

    let json = null;

    try {
        json = JSON.parse(textoError);
    } catch (_) {
        json = null;
    }

    const texto = json
        ? `${json.error || ""} ${json.message || ""} ${json.path || ""}`.toLowerCase()
        : textoError.toLowerCase();

    if (contexto === "tarjeta") {
        if (texto.includes("mes")) return "El mes de expiración no es válido.";
        if (texto.includes("caduc")) return "La tarjeta está caducada.";
        if (texto.includes("fecha")) return "La fecha de expiración no es válida.";
        if (texto.includes("titular")) return "El titular de la tarjeta no es válido.";
        if (texto.includes("número") || texto.includes("numero")) return "El número de tarjeta no es válido.";
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

    if (contexto === "direccion") {
        if (texto.includes("alias")) return "El alias de la dirección no es válido.";
        if (texto.includes("provincia")) return "La provincia no es válida.";
        if (texto.includes("municipio")) return "El municipio no es válido.";
        if (texto.includes("calle")) return "La calle no es válida.";
        if (texto.includes("codigo postal") || texto.includes("código postal")) return "El código postal no es válido.";
        if (texto.includes("principal")) return "No se pudo cambiar la dirección principal.";
        return "No se pudo guardar la dirección.";
    }

    return "Ha ocurrido un error inesperado.";
}

/* HELPERS */

function obtenerValor(id) {
    const el = document.getElementById(id);
    return el ? el.value.trim() : "";
}

function asignarValor(id, valor) {
    const el = document.getElementById(id);
    if (el) el.value = valor;
}

function asignarTexto(id, texto) {
    const el = document.getElementById(id);
    if (el) el.textContent = texto;
}

function setReadOnly(id, estado) {
    const el = document.getElementById(id);
    if (!el) return;
    if (estado) el.setAttribute("readonly", true);
    else el.removeAttribute("readonly");
}

function setChecked(id, estado) {
    const el = document.getElementById(id);
    if (el) el.checked = estado;
}

function toggleHidden(id, oculto) {
    const el = document.getElementById(id);
    if (!el) return;
    el.hidden = oculto;
}

function mostrarElemento(el) {
    if (!el) return;
    el.classList.remove("hidden");
}

function ocultarElemento(el) {
    if (!el) return;
    el.classList.add("hidden");
}

function limpiarContenedor(contenedor) {
    if (!contenedor) return;
    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
}