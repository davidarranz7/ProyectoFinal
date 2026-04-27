document.addEventListener("DOMContentLoaded", () => {
    const formIncidencia = document.getElementById("form-incidencia");

    const nombreContactoInput = document.getElementById("nombreContacto");
    const emailContactoInput = document.getElementById("emailContacto");
    const usuarioRelacionadoInput = document.getElementById("usuarioRelacionado");
    const numeroPedidoInput = document.getElementById("numeroPedido");
    const tipoIncidenciaInput = document.getElementById("tipoIncidencia");
    const asuntoInput = document.getElementById("asunto");
    const mensajeInput = document.getElementById("mensaje");

    const bloqueCuenta = document.getElementById("bloque-cuenta");
    const bloquePedido = document.getElementById("bloque-pedido");

    const mensajeIncidencia = document.getElementById("mensaje-incidencia");
    const btnEnviarIncidencia = document.getElementById("btn-enviar-incidencia");

    tipoIncidenciaInput.addEventListener("change", () => {
        actualizarCamposSegunTipo();
    });

    formIncidencia.addEventListener("submit", async (event) => {
        event.preventDefault();

        limpiarMensaje();

        const nombreContacto = nombreContactoInput.value.trim();
        const emailContacto = emailContactoInput.value.trim();
        const usuarioRelacionado = usuarioRelacionadoInput.value.trim();
        const numeroPedido = numeroPedidoInput.value.trim();
        const tipoIncidencia = tipoIncidenciaInput.value;
        const asunto = asuntoInput.value.trim();
        const mensaje = mensajeInput.value.trim();

        if (!nombreContacto) {
            mostrarMensaje("El nombre de contacto es obligatorio.", "error");
            return;
        }

        if (!emailContacto) {
            mostrarMensaje("El email de contacto es obligatorio.", "error");
            return;
        }

        if (!validarEmail(emailContacto)) {
            mostrarMensaje("El formato del email no es válido.", "error");
            return;
        }

        if (!tipoIncidencia) {
            mostrarMensaje("Debes seleccionar un tipo de incidencia.", "error");
            return;
        }

        if (!asunto) {
            mostrarMensaje("El asunto es obligatorio.", "error");
            return;
        }

        if (!mensaje) {
            mostrarMensaje("El mensaje es obligatorio.", "error");
            return;
        }

        if (mensaje.length < 10) {
            mostrarMensaje("El mensaje debe tener al menos 10 caracteres.", "error");
            return;
        }

        btnEnviarIncidencia.disabled = true;
        btnEnviarIncidencia.textContent = "Enviando incidencia...";

        try {
            const response = await fetch(`${BASE_URL}/incidencias`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nombreContacto: nombreContacto,
                    emailContacto: emailContacto,
                    usuarioRelacionado: obtenerUsuarioRelacionadoParaEnviar(tipoIncidencia, usuarioRelacionado),
                    numeroPedido: obtenerNumeroPedidoParaEnviar(tipoIncidencia, numeroPedido),
                    tipoIncidencia: tipoIncidencia,
                    asunto: asunto,
                    mensaje: mensaje
                })
            });

            const data = await leerRespuesta(response);

            if (!response.ok) {
                mostrarMensaje(data || "No se pudo crear la incidencia.", "error");
                return;
            }

            mostrarMensaje(
                `Incidencia creada correctamente. Código de seguimiento: ${data.codigoSeguimiento}. Te hemos enviado un correo de confirmación.`,
                "ok"
            );

            formIncidencia.reset();
            actualizarCamposSegunTipo();

            window.scrollTo({
                top: 0,
                behavior: "smooth"
            });

        } catch (error) {
            mostrarMensaje("No se pudo conectar con el servidor.", "error");
        } finally {
            btnEnviarIncidencia.disabled = false;
            btnEnviarIncidencia.textContent = "Enviar incidencia";
        }
    });

    function actualizarCamposSegunTipo() {
        const tipo = tipoIncidenciaInput.value;

        const mostrarCuenta = necesitaDatosCuenta(tipo);
        const mostrarPedido = necesitaDatosPedido(tipo);

        if (mostrarCuenta) {
            bloqueCuenta.classList.remove("hidden");
        } else {
            bloqueCuenta.classList.add("hidden");
            usuarioRelacionadoInput.value = "";
        }

        if (mostrarPedido) {
            bloquePedido.classList.remove("hidden");
        } else {
            bloquePedido.classList.add("hidden");
            numeroPedidoInput.value = "";
        }
    }

    function necesitaDatosCuenta(tipo) {
        return tipo === "PROBLEMA_ACCESO"
            || tipo === "NO_RECUERDO_DATOS"
            || tipo === "SIN_ACCESO_EMAIL";
    }

    function necesitaDatosPedido(tipo) {
        return tipo === "PROBLEMA_PEDIDO"
            || tipo === "PROBLEMA_PAGO"
            || tipo === "PRODUCTO_DEFECTUOSO";
    }

    function obtenerUsuarioRelacionadoParaEnviar(tipo, usuarioRelacionado) {
        if (!necesitaDatosCuenta(tipo)) {
            return null;
        }

        return usuarioRelacionado || null;
    }

    function obtenerNumeroPedidoParaEnviar(tipo, numeroPedido) {
        if (!necesitaDatosPedido(tipo)) {
            return null;
        }

        return numeroPedido ? Number(numeroPedido) : null;
    }

    async function leerRespuesta(response) {
        const texto = await response.text();

        try {
            return JSON.parse(texto);
        } catch (error) {
            return texto;
        }
    }

    function mostrarMensaje(texto, tipo) {
        mensajeIncidencia.textContent = texto;
        mensajeIncidencia.classList.remove("ok", "error");
        mensajeIncidencia.classList.add(tipo);
    }

    function limpiarMensaje() {
        mensajeIncidencia.textContent = "";
        mensajeIncidencia.classList.remove("ok", "error");
    }

    function validarEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    actualizarCamposSegunTipo();
});