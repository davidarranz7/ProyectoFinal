document.addEventListener("DOMContentLoaded", async () => {
    const accesoPermitido = await comprobarAccesoAdmin();
    if (!accesoPermitido) return;
    iniciarAdmin();
});

async function comprobarAccesoAdmin() {
    try {
        const response = await fetch(`${BASE_URL}/auth/session`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            window.location.href = "/index.html";
            return false;
        }

        const sesion = await response.json();

        if (!sesion || sesion.rol !== "ADMIN") {
            window.location.href = "/index.html";
            return false;
        }

        return true;
    } catch (error) {
        console.error("Error al comprobar acceso admin:", error);
        window.location.href = "/index.html";
        return false;
    }
}

function iniciarAdmin() {
    const refs = obtenerReferencias();
    const state = crearEstadoInicial();

    configurarNavegacion(refs);
    configurarScraping(refs, state);
    configurarProductos(refs, state);
    configurarEstablecimientos(refs, state);
    configurarPuntosRecogida(refs, state);
    configurarUsuarios(refs, state);
    configurarPedidos(refs, state);
    configurarIncidencias(refs, state);
    configurarConfirmacionEntrega(refs, state);
    configurarModales(refs, state);

    actualizarEstadoScraping(refs, "Inactivo", "Sin procesos activos en este momento.", "neutral");
    actualizarCampoMotivoEstablecimiento(refs);
    actualizarCampoMotivoPuntoRecogida(refs);

    cargarTodo(refs, state);
    iniciarRefrescoAutomaticoIncidencias(refs, state);
    iniciarRefrescoEstadoScraping(refs, state);
}

function obtenerReferencias() {
    return {
        botonesNav: document.querySelectorAll(".admin-nav-btn"),
        secciones: document.querySelectorAll(".admin-section"),
        mensajeAdmin: document.getElementById("mensaje-admin"),

        totalProductos: document.getElementById("total-productos"),
        totalUsuarios: document.getElementById("total-usuarios"),
        totalPedidos: document.getElementById("total-pedidos"),

        estadoScraping: document.getElementById("estado-scraping"),
        detalleScraping: document.getElementById("detalle-scraping"),
        chipScraping: document.getElementById("chip-scraping"),
        scrapingEstadoBox: document.getElementById("scraping-estado-box"),
        scrapingUltimaAccion: document.getElementById("scraping-ultima-accion"),
        scrapingRelayBox: document.getElementById("scraping-relay-box"),
        scrapingRelayDetalle: document.getElementById("scraping-relay-detalle"),
        scrapingAutoBox: document.getElementById("scraping-auto-box"),
        scrapingAutoDetalle: document.getElementById("scraping-auto-detalle"),
        scrapingColaTitulo: document.getElementById("scraping-cola-titulo"),
        scrapingColaBadge: document.getElementById("scraping-cola-badge"),
        scrapingColaResumen: document.getElementById("scraping-cola-resumen"),
        scrapingPendientesLista: document.getElementById("scraping-pendientes-lista"),

        scrapingResultadoPanel: document.getElementById("scraping-resultado-panel"),
        scrapingResultadoTitulo: document.getElementById("scraping-resultado-titulo"),
        scrapingResultadoDuracion: document.getElementById("scraping-resultado-duracion"),
        scrapingTotalEncontrados: document.getElementById("scraping-total-encontrados"),
        scrapingTotalGuardados: document.getElementById("scraping-total-guardados"),
        scrapingTotalNuevos: document.getElementById("scraping-total-nuevos"),
        scrapingTotalActualizados: document.getElementById("scraping-total-actualizados"),
        scrapingTotalCambiosPrecio: document.getElementById("scraping-total-cambios-precio"),
        scrapingTotalBajadasPrecio: document.getElementById("scraping-total-bajadas-precio"),
        scrapingTotalSubidasPrecio: document.getElementById("scraping-total-subidas-precio"),
        scrapingTotalDesactivados: document.getElementById("scraping-total-desactivados"),
        scrapingTotalSinImagen: document.getElementById("scraping-total-sin-imagen"),
        scrapingTotalSinPrecio: document.getElementById("scraping-total-sin-precio"),
        scrapingTiendasLista: document.getElementById("scraping-tiendas-lista"),
        scrapingCambiosLista: document.getElementById("scraping-cambios-lista"),
        scrapingOverlay: document.getElementById("scraping-overlay"),
        scrapingOverlayTitulo: document.getElementById("scraping-overlay-titulo"),
        scrapingOverlayDetalle: document.getElementById("scraping-overlay-detalle"),
        scrapingOverlayTimer: document.getElementById("scraping-overlay-timer"),
        scrapingOverlayStep: document.getElementById("scraping-overlay-step"),

        btnScrapingZara: document.getElementById("btn-scraping-zara"),
        btnScrapingBershka: document.getElementById("btn-scraping-bershka"),
        btnScrapingPull: document.getElementById("btn-scraping-pull"),
        btnScrapingTodo: document.getElementById("btn-scraping-todo"),

        buscadorProductos: document.getElementById("buscador-productos-admin"),
        filtroTiendaProductos: document.getElementById("filtro-tienda-productos-admin"),
        filtroSeccionProductos: document.getElementById("filtro-seccion-productos-admin"),
        filtroCategoriaProductos: document.getElementById("filtro-categoria-productos-admin"),
        filtroOrdenProductos: document.getElementById("filtro-orden-productos-admin"),
        btnLimpiarFiltrosProductos: document.getElementById("btn-limpiar-filtros-productos"),
        btnModoSeleccionProductos: document.getElementById("btn-modo-seleccion-productos"),
        btnSeleccionarTodosProductos: document.getElementById("btn-seleccionar-todos-productos"),
        btnSeleccionarSinStockProductos: document.getElementById("btn-seleccionar-sin-stock-productos"),
        btnStockSeleccionados: document.getElementById("btn-stock-seleccionados"),
        contenedorProductos: document.getElementById("contenedor-productos-admin"),
        estadoProductos: document.getElementById("productos-admin-estado"),

        buscadorEstablecimientos: document.getElementById("buscador-establecimientos-admin"),
        btnNuevoEstablecimiento: document.getElementById("btn-nuevo-establecimiento"),
        contenedorEstablecimientos: document.getElementById("contenedor-establecimientos-admin"),
        estadoEstablecimientos: document.getElementById("establecimientos-admin-estado"),

        modalCrearEstablecimiento: document.getElementById("modal-crear-establecimiento"),
        cerrarModalCrearEstablecimiento: document.getElementById("cerrar-modal-crear-establecimiento"),
        cancelarModalCrearEstablecimiento: document.getElementById("cancelar-modal-crear-establecimiento"),
        formCrearEstablecimiento: document.getElementById("form-crear-establecimiento"),
        crearEstablecimientoNombre: document.getElementById("crear-establecimiento-nombre"),
        crearEstablecimientoTienda: document.getElementById("crear-establecimiento-tienda"),
        crearEstablecimientoDireccion: document.getElementById("crear-establecimiento-direccion"),
        crearEstablecimientoCiudad: document.getElementById("crear-establecimiento-ciudad"),
        crearEstablecimientoProvincia: document.getElementById("crear-establecimiento-provincia"),
        crearEstablecimientoDisponible: document.getElementById("crear-establecimiento-disponible"),
        crearEstablecimientoMotivo: document.getElementById("crear-establecimiento-motivo"),
        guardarCrearEstablecimiento: document.getElementById("guardar-crear-establecimiento"),

        modalEditarEstablecimiento: document.getElementById("modal-editar-establecimiento"),
        cerrarModalEditarEstablecimiento: document.getElementById("cerrar-modal-editar-establecimiento"),
        cancelarModalEditarEstablecimiento: document.getElementById("cancelar-modal-editar-establecimiento"),
        formEditarEstablecimiento: document.getElementById("form-editar-establecimiento"),
        editarEstablecimientoId: document.getElementById("editar-establecimiento-id"),
        editarEstablecimientoNombre: document.getElementById("editar-establecimiento-nombre"),
        editarEstablecimientoTienda: document.getElementById("editar-establecimiento-tienda"),
        editarEstablecimientoDireccion: document.getElementById("editar-establecimiento-direccion"),
        editarEstablecimientoCiudad: document.getElementById("editar-establecimiento-ciudad"),
        editarEstablecimientoProvincia: document.getElementById("editar-establecimiento-provincia"),
        guardarEditarEstablecimiento: document.getElementById("guardar-editar-establecimiento"),

        buscadorPuntosRecogida: document.getElementById("buscador-puntos-recogida-admin"),
        btnNuevoPuntoRecogida: document.getElementById("btn-nuevo-punto-recogida"),
        contenedorPuntosRecogida: document.getElementById("contenedor-puntos-recogida-admin"),
        estadoPuntosRecogida: document.getElementById("puntos-recogida-admin-estado"),

        modalCrearPuntoRecogida: document.getElementById("modal-crear-punto-recogida"),
        cerrarModalCrearPuntoRecogida: document.getElementById("cerrar-modal-crear-punto-recogida"),
        cancelarModalCrearPuntoRecogida: document.getElementById("cancelar-modal-crear-punto-recogida"),
        formCrearPuntoRecogida: document.getElementById("form-crear-punto-recogida"),
        crearPuntoRecogidaNombre: document.getElementById("crear-punto-recogida-nombre"),
        crearPuntoRecogidaDireccion: document.getElementById("crear-punto-recogida-direccion"),
        crearPuntoRecogidaCiudad: document.getElementById("crear-punto-recogida-ciudad"),
        crearPuntoRecogidaProvincia: document.getElementById("crear-punto-recogida-provincia"),
        crearPuntoRecogidaDisponible: document.getElementById("crear-punto-recogida-disponible"),
        crearPuntoRecogidaMotivo: document.getElementById("crear-punto-recogida-motivo"),
        guardarCrearPuntoRecogida: document.getElementById("guardar-crear-punto-recogida"),

        modalEditarPuntoRecogida: document.getElementById("modal-editar-punto-recogida"),
        cerrarModalEditarPuntoRecogida: document.getElementById("cerrar-modal-editar-punto-recogida"),
        cancelarModalEditarPuntoRecogida: document.getElementById("cancelar-modal-editar-punto-recogida"),
        formEditarPuntoRecogida: document.getElementById("form-editar-punto-recogida"),
        editarPuntoRecogidaId: document.getElementById("editar-punto-recogida-id"),
        editarPuntoRecogidaNombre: document.getElementById("editar-punto-recogida-nombre"),
        editarPuntoRecogidaDireccion: document.getElementById("editar-punto-recogida-direccion"),
        editarPuntoRecogidaCiudad: document.getElementById("editar-punto-recogida-ciudad"),
        editarPuntoRecogidaProvincia: document.getElementById("editar-punto-recogida-provincia"),
        guardarEditarPuntoRecogida: document.getElementById("guardar-editar-punto-recogida"),

        contenedorUsuarios: document.getElementById("contenedor-usuarios-admin"),
        estadoUsuarios: document.getElementById("usuarios-admin-estado"),

        filtroEstadoPedidos: document.getElementById("filtro-estado-pedidos"),
        contenedorPedidos: document.getElementById("contenedor-pedidos-admin"),
        estadoPedidos: document.getElementById("pedidos-admin-estado"),

        filtroEstadoIncidencias: document.getElementById("filtro-estado-incidencias"),
        contenedorIncidencias: document.getElementById("contenedor-incidencias-admin"),
        estadoIncidencias: document.getElementById("incidencias-admin-estado"),

        modalDetalleIncidencia: document.getElementById("modal-detalle-incidencia"),
        cerrarModalDetalleIncidencia: document.getElementById("cerrar-modal-detalle-incidencia"),
        contenidoDetalleIncidencia: document.getElementById("contenido-detalle-incidencia"),
        formResponderIncidencia: document.getElementById("form-responder-incidencia"),
        responderIncidenciaId: document.getElementById("responder-incidencia-id"),
        textoRespuestaIncidencia: document.getElementById("texto-respuesta-incidencia"),
        btnEnviarRespuestaIncidencia: document.getElementById("btn-enviar-respuesta-incidencia"),

        qrReaderEntrega: document.getElementById("qr-reader-entrega"),
        btnIniciarEscanerEntrega: document.getElementById("btn-iniciar-escaner-entrega"),
        btnDetenerEscanerEntrega: document.getElementById("btn-detener-escaner-entrega"),
        inputTokenEntrega: document.getElementById("input-token-entrega"),
        btnConfirmarTokenEntrega: document.getElementById("btn-confirmar-token-entrega"),
        resultadoQrEntrega: document.getElementById("resultado-qr-entrega"),

        modalEditarProducto: document.getElementById("modal-editar-producto"),
        cerrarModalEditarProducto: document.getElementById("cerrar-modal-editar-producto"),
        cancelarModalEditarProducto: document.getElementById("cancelar-modal-editar-producto"),
        formEditarProducto: document.getElementById("form-editar-producto"),
        editarProductoId: document.getElementById("editar-producto-id"),
        editarProductoNombre: document.getElementById("editar-producto-nombre"),
        editarProductoDescripcion: document.getElementById("editar-producto-descripcion"),
        editarProductoPrecio: document.getElementById("editar-producto-precio"),
        editarProductoUrlImagen: document.getElementById("editar-producto-url-imagen"),
        editarProductoUrlProducto: document.getElementById("editar-producto-url-producto"),
        guardarCambiosProducto: document.getElementById("guardar-cambios-producto"),

        modalEliminarProducto: document.getElementById("modal-eliminar-producto"),
        cerrarModalEliminarProducto: document.getElementById("cerrar-modal-eliminar-producto"),
        cancelarModalEliminarProducto: document.getElementById("cancelar-modal-eliminar-producto"),
        confirmarEliminarProducto: document.getElementById("confirmar-eliminar-producto"),

        modalStockProductos: document.getElementById("modal-stock-productos"),
        cerrarModalStockProductos: document.getElementById("cerrar-modal-stock-productos"),
        cancelarModalStockProductos: document.getElementById("cancelar-modal-stock-productos"),
        formStockProductos: document.getElementById("form-stock-productos"),
        stockResumenProductos: document.getElementById("stock-resumen-productos"),
        stockActualProducto: document.getElementById("stock-actual-producto"),
        tallasStockProductos: document.getElementById("tallas-stock-productos"),
        stockCantidadProductos: document.getElementById("stock-cantidad-productos"),
        guardarStockProductos: document.getElementById("guardar-stock-productos"),

        modalEditarUsuario: document.getElementById("modal-editar-usuario"),
        cerrarModalEditarUsuario: document.getElementById("cerrar-modal-editar-usuario"),
        cancelarModalEditarUsuario: document.getElementById("cancelar-modal-editar-usuario"),
        formEditarUsuario: document.getElementById("form-editar-usuario"),
        editarUsuarioId: document.getElementById("editar-usuario-id"),
        editarUsuarioNombre: document.getElementById("editar-usuario-nombre"),
        editarUsuarioEmail: document.getElementById("editar-usuario-email"),
        editarUsuarioRol: document.getElementById("editar-usuario-rol"),
        guardarCambiosUsuario: document.getElementById("guardar-cambios-usuario"),

        modalEliminarUsuario: document.getElementById("modal-eliminar-usuario"),
        cerrarModalEliminarUsuario: document.getElementById("cerrar-modal-eliminar-usuario"),
        cancelarModalEliminarUsuario: document.getElementById("cancelar-modal-eliminar-usuario"),
        confirmarEliminarUsuario: document.getElementById("confirmar-eliminar-usuario"),

        modalDetalleUsuario: document.getElementById("modal-detalle-usuario"),
        cerrarModalDetalleUsuario: document.getElementById("cerrar-modal-detalle-usuario"),
        contenidoDetalleUsuario: document.getElementById("contenido-detalle-usuario"),

        modalDetallePedido: document.getElementById("modal-detalle-pedido"),
        cerrarModalDetallePedido: document.getElementById("cerrar-modal-detalle-pedido"),
        contenidoDetallePedido: document.getElementById("contenido-detalle-pedido"),

        modalCambiarEstadoPedido: document.getElementById("modal-cambiar-estado-pedido"),
        cerrarModalCambiarEstadoPedido: document.getElementById("cerrar-modal-cambiar-estado-pedido"),
        cancelarModalCambiarEstadoPedido: document.getElementById("cancelar-modal-cambiar-estado-pedido"),
        formCambiarEstadoPedido: document.getElementById("form-cambiar-estado-pedido"),
        cambiarEstadoPedidoId: document.getElementById("cambiar-estado-pedido-id"),
        nuevoEstadoPedido: document.getElementById("nuevo-estado-pedido"),
        textoAyudaEstadoPedido: document.getElementById("texto-ayuda-estado-pedido"),
        guardarCambioEstadoPedido: document.getElementById("guardar-cambio-estado-pedido")
    };
}

function crearEstadoInicial() {
    return {
        productos: [],
        productosFiltrados: [],
        establecimientos: [],
        establecimientosFiltrados: [],
        puntosRecogida: [],
        puntosRecogidaFiltrados: [],
        usuarios: [],
        pedidos: [],
        incidencias: [],
        estadosPedidoDisponibles: [],
        estadosIncidenciaDisponibles: [],

        incidenciaDetalleAbiertaId: null,
        intervaloDetalleIncidencia: null,
        cargandoDetalleIncidencia: false,
        snapshotDetalleIncidencia: "",

        intervaloIncidenciasAdmin: null,
        cargandoIncidencias: false,
        snapshotIncidencias: "",
        intervaloEstadoScraping: null,
        actualizandoEstadoScraping: false,
        ultimoEstadoScrapingAdmin: null,
        scrapingOverlayInicio: null,
        scrapingOverlayIntervalo: null,

        modoSeleccionProductos: false,
        productosSeleccionados: new Set(),
        productosSeleccionadosInfo: new Map(),
        seleccionMasivaSinStockActiva: false,
        productoIdPendienteEliminar: null,
        usuarioIdPendienteEliminar: null,
        productosStockObjetivo: [],
        gruposStockObjetivo: [],
        tipoStockObjetivo: null,

        pedidoCambioEstado: null,

        qrScannerEntrega: null,
        escanerEntregaActivo: false,
        ultimoTokenEntregaLeido: null,
        confirmandoEntrega: false,

        paginaProductos: 0,
        sizeProductosAdmin: 20,
        totalProductosCatalogo: 0,
        ultimaPaginaProductos: false,
        cargandoProductos: false,
        timeoutBusquedaProductos: null,
        observadorProductos: null
    };
}

function crearSnapshotMensajesIncidencia(incidencia) {
    const mensajes = obtenerMensajesIncidencia(incidencia);

    return JSON.stringify({
        id: incidencia.id,
        estado: incidencia.estadoIncidencia,
        mensajes: mensajes.map((mensaje) => ({
            id: mensaje.id,
            remitente: mensaje.remitente,
            contenido: mensaje.contenido,
            fecha: mensaje.fechaMensaje
        }))
    });
}

/* =========================
   CONFIGURACIÓN GENERAL
========================= */

function configurarNavegacion(refs) {
    refs.botonesNav.forEach((boton) => {
        boton.addEventListener("click", () => {
            const idSeccion = boton.dataset.seccion;
            const destino = document.getElementById(idSeccion);
            if (!destino) return;

            destino.scrollIntoView({ behavior: "smooth", block: "start" });
            activarBotonNav(refs, idSeccion);
        });
    });

    const observer = new IntersectionObserver((entries) => {
        let activa = null;

        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                activa = entry.target.id;
            }
        });

        if (activa) activarBotonNav(refs, activa);
    }, {
        root: null,
        rootMargin: "-140px 0px -55% 0px",
        threshold: 0.15
    });

    refs.secciones.forEach((seccion) => observer.observe(seccion));
}

function activarBotonNav(refs, idSeccion) {
    refs.botonesNav.forEach((boton) => {
        boton.classList.toggle("admin-nav-btn-activo", boton.dataset.seccion === idSeccion);
    });
}

function configurarScraping(refs, state) {
    refs.btnScrapingZara?.addEventListener("click", () =>
        ejecutarScraping(refs, state, "/productos/scrapear/zara", "Zara", refs.btnScrapingZara, "Ejecutar")
    );

    refs.btnScrapingBershka?.addEventListener("click", () =>
        ejecutarScraping(refs, state, "/productos/scrapear/bershka", "Bershka", refs.btnScrapingBershka, "Ejecutar")
    );

    refs.btnScrapingPull?.addEventListener("click", () =>
        ejecutarScraping(refs, state, "/productos/scrapear/pullandbear", "Pull&Bear", refs.btnScrapingPull, "Ejecutar")
    );

    refs.btnScrapingTodo?.addEventListener("click", () =>
        ejecutarScraping(refs, state, "/productos/scrapear/total", "Scraping completo", refs.btnScrapingTodo, "Ejecutar todo")
    );

    cargarEstadoScrapingAdmin(refs, state, { silencioso: true });
}

function configurarProductos(refs, state) {
    refs.buscadorProductos?.addEventListener("input", () => {
        clearTimeout(state.timeoutBusquedaProductos);

        state.timeoutBusquedaProductos = setTimeout(() => {
            aplicarFiltroProductos(refs, state);
        }, 350);
    });

    refs.filtroTiendaProductos?.addEventListener("change", () => aplicarFiltroProductos(refs, state));
    refs.filtroSeccionProductos?.addEventListener("change", () => aplicarFiltroProductos(refs, state));
    refs.filtroCategoriaProductos?.addEventListener("change", () => aplicarFiltroProductos(refs, state));
    refs.filtroOrdenProductos?.addEventListener("change", () => aplicarFiltroProductos(refs, state));

    refs.btnLimpiarFiltrosProductos?.addEventListener("click", () => {
        if (refs.buscadorProductos) refs.buscadorProductos.value = "";
        if (refs.filtroTiendaProductos) refs.filtroTiendaProductos.value = "";
        if (refs.filtroSeccionProductos) refs.filtroSeccionProductos.value = "";
        if (refs.filtroCategoriaProductos) refs.filtroCategoriaProductos.value = "";
        if (refs.filtroOrdenProductos) refs.filtroOrdenProductos.value = "recientes";

        aplicarFiltroProductos(refs, state);
    });

    refs.btnModoSeleccionProductos?.addEventListener("click", () => {
        state.modoSeleccionProductos = !state.modoSeleccionProductos;
        refs.btnModoSeleccionProductos.textContent = state.modoSeleccionProductos ? "Salir selección" : "Modo selección";

        if (!state.modoSeleccionProductos) {
            limpiarSeleccionProductos(state);
        }

        state.seleccionMasivaSinStockActiva = false;
        actualizarBotonStockSeleccionados(refs, state);
        renderizarProductos(refs, state);
    });

    refs.btnSeleccionarTodosProductos?.addEventListener("click", () => {
        if (!state.modoSeleccionProductos) {
            mostrarMensaje(refs, "Activa primero el modo selección.", "info");
            return;
        }

        const visibles = state.productosFiltrados;

        if (!visibles.length) {
            mostrarMensaje(refs, "No hay productos visibles para seleccionar.", "info");
            return;
        }

        const todosSeleccionados = visibles.every((p) => state.productosSeleccionados.has(p.id));

        if (todosSeleccionados) {
            desregistrarProductosSeleccionados(state, visibles);
        } else {
            registrarProductosSeleccionados(state, visibles);
        }

        state.seleccionMasivaSinStockActiva = false;
        actualizarBotonStockSeleccionados(refs, state);
        renderizarProductos(refs, state);
    });

    refs.btnSeleccionarSinStockProductos?.addEventListener("click", async () => {
        if (!state.modoSeleccionProductos) {
            mostrarMensaje(refs, "Activa primero el modo selecciÃ³n.", "info");
            return;
        }

        try {
            bloquearBoton(refs.btnSeleccionarSinStockProductos, "Buscando...");

            const response = await fetch(construirUrlSeleccionSinStockProductosAdmin(refs), {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) {
                throw new Error("No se pudieron cargar los productos sin stock");
            }

            const productosSinStock = await response.json();

            limpiarSeleccionProductos(state);

            if (!Array.isArray(productosSinStock) || productosSinStock.length === 0) {
                actualizarBotonStockSeleccionados(refs, state);
                renderizarProductos(refs, state);
                mostrarMensaje(refs, "No hay productos sin stock con los filtros actuales.", "info");
                return;
            }

            registrarProductosSeleccionados(state, productosSinStock);
            state.seleccionMasivaSinStockActiva = true;

            actualizarBotonStockSeleccionados(refs, state);
            renderizarProductos(refs, state);
            mostrarMensaje(refs, `${formatearNumero(productosSinStock.length)} productos sin stock seleccionados.`, "ok");
        } catch (error) {
            console.error(error);
            mostrarMensaje(refs, "No se pudo completar la selecciÃ³n sin stock.", "error");
        } finally {
            restaurarBoton(refs.btnSeleccionarSinStockProductos, "Seleccionar sin stock");
        }
    });

    refs.btnStockSeleccionados?.addEventListener("click", () => {
        const seleccionados = obtenerProductosSeleccionados(state);

        if (!seleccionados.length) {
            mostrarMensaje(refs, "Selecciona al menos un producto.", "error");
            return;
        }

        abrirModalStock(refs, state, seleccionados);
    });

    refs.formEditarProducto?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarEdicionProducto(refs, state);
    });

    refs.formStockProductos?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarStockProductos(refs, state);
    });
}

function configurarEstablecimientos(refs, state) {
    refs.buscadorEstablecimientos?.addEventListener("input", () => {
        aplicarFiltroEstablecimientos(refs, state);
    });

    refs.btnNuevoEstablecimiento?.addEventListener("click", () => {
        refs.formCrearEstablecimiento?.reset();
        actualizarCampoMotivoEstablecimiento(refs);
        abrirModal(refs.modalCrearEstablecimiento);
    });

    refs.formCrearEstablecimiento?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarNuevoEstablecimiento(refs, state);
    });

    refs.crearEstablecimientoDisponible?.addEventListener("change", () => {
        actualizarCampoMotivoEstablecimiento(refs);
    });

    refs.formEditarEstablecimiento?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarEdicionEstablecimiento(refs, state);
    });
}

function configurarPuntosRecogida(refs, state) {
    refs.buscadorPuntosRecogida?.addEventListener("input", () => {
        aplicarFiltroPuntosRecogida(refs, state);
    });

    refs.btnNuevoPuntoRecogida?.addEventListener("click", () => {
        refs.formCrearPuntoRecogida?.reset();
        actualizarCampoMotivoPuntoRecogida(refs);
        abrirModal(refs.modalCrearPuntoRecogida);
    });

    refs.formCrearPuntoRecogida?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarNuevoPuntoRecogida(refs, state);
    });

    refs.crearPuntoRecogidaDisponible?.addEventListener("change", () => {
        actualizarCampoMotivoPuntoRecogida(refs);
    });

    refs.formEditarPuntoRecogida?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarEdicionPuntoRecogida(refs, state);
    });
}

function configurarUsuarios(refs, state) {
    refs.formEditarUsuario?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarEdicionUsuario(refs, state);
    });
}

function configurarPedidos(refs, state) {
    refs.filtroEstadoPedidos?.addEventListener("change", async () => {
        await cargarPedidos(refs, state);
    });

    refs.formCambiarEstadoPedido?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await guardarCambioEstadoPedido(refs, state);
    });
}

function configurarIncidencias(refs, state) {
    refs.filtroEstadoIncidencias?.addEventListener("change", async () => {
        await cargarIncidencias(refs, state);
    });

    refs.formResponderIncidencia?.addEventListener("submit", async (e) => {
        e.preventDefault();
        await enviarRespuestaIncidencia(refs, state);
    });
}

function crearSnapshotIncidencias(incidencias) {
    return JSON.stringify(
        incidencias.map((incidencia) => ({
            id: incidencia.id,
            estado: incidencia.estadoIncidencia,
            fecha: incidencia.fechaUltimaActualizacion || incidencia.fechaCreacion
        }))
    );
}

function configurarConfirmacionEntrega(refs, state) {
    refs.btnIniciarEscanerEntrega?.addEventListener("click", async () => {
        await iniciarEscanerEntrega(refs, state);
    });

    refs.btnDetenerEscanerEntrega?.addEventListener("click", async () => {
        await detenerEscanerEntrega(refs, state);
    });

    refs.btnConfirmarTokenEntrega?.addEventListener("click", async () => {
        await confirmarEntregaManual(refs, state);
    });

    refs.inputTokenEntrega?.addEventListener("keydown", async (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            await confirmarEntregaManual(refs, state);
        }
    });
}

function configurarModales(refs, state) {
    configurarCerrarModal(refs.modalEditarProducto, refs.cerrarModalEditarProducto, refs.cancelarModalEditarProducto, () => {
        refs.formEditarProducto?.reset();
    });

    configurarCerrarModal(refs.modalEliminarProducto, refs.cerrarModalEliminarProducto, refs.cancelarModalEliminarProducto, () => {
        state.productoIdPendienteEliminar = null;
    });

    configurarCerrarModal(refs.modalStockProductos, refs.cerrarModalStockProductos, refs.cancelarModalStockProductos, () => {
        state.productosStockObjetivo = [];
        state.gruposStockObjetivo = [];
        state.tipoStockObjetivo = null;
        refs.formStockProductos?.reset();
        limpiarContenedor(refs.stockResumenProductos);
        limpiarContenedor(refs.stockActualProducto);
        limpiarContenedor(refs.tallasStockProductos);
        if (refs.guardarStockProductos) {
            refs.guardarStockProductos.disabled = false;
        }
        refs.stockActualProducto?.appendChild(crearTextoVacio("texto-box-vacio", "Selecciona un producto para ver el stock actual por talla."));
    });

    configurarCerrarModal(
        refs.modalCrearEstablecimiento,
        refs.cerrarModalCrearEstablecimiento,
        refs.cancelarModalCrearEstablecimiento,
        () => {
            refs.formCrearEstablecimiento?.reset();
            actualizarCampoMotivoEstablecimiento(refs);
        }
    );

    configurarCerrarModal(
        refs.modalEditarEstablecimiento,
        refs.cerrarModalEditarEstablecimiento,
        refs.cancelarModalEditarEstablecimiento,
        () => {
            refs.formEditarEstablecimiento?.reset();
        }
    );

    configurarCerrarModal(
        refs.modalCrearPuntoRecogida,
        refs.cerrarModalCrearPuntoRecogida,
        refs.cancelarModalCrearPuntoRecogida,
        () => {
            refs.formCrearPuntoRecogida?.reset();
            actualizarCampoMotivoPuntoRecogida(refs);
        }
    );

    configurarCerrarModal(
        refs.modalEditarPuntoRecogida,
        refs.cerrarModalEditarPuntoRecogida,
        refs.cancelarModalEditarPuntoRecogida,
        () => {
            refs.formEditarPuntoRecogida?.reset();
        }
    );

    configurarCerrarModal(refs.modalEditarUsuario, refs.cerrarModalEditarUsuario, refs.cancelarModalEditarUsuario, () => {
        refs.formEditarUsuario?.reset();
    });

    configurarCerrarModal(refs.modalEliminarUsuario, refs.cerrarModalEliminarUsuario, refs.cancelarModalEliminarUsuario, () => {
        state.usuarioIdPendienteEliminar = null;
    });

    configurarCerrarModal(refs.modalDetalleUsuario, refs.cerrarModalDetalleUsuario, null, () => {
        limpiarContenedor(refs.contenidoDetalleUsuario);
    });

    configurarCerrarModal(refs.modalDetallePedido, refs.cerrarModalDetallePedido, null, () => {
        limpiarContenedor(refs.contenidoDetallePedido);
    });

    configurarCerrarModal(refs.modalDetalleIncidencia, refs.cerrarModalDetalleIncidencia, null, () => {
        detenerRefrescoDetalleIncidencia(state);

        state.incidenciaDetalleAbiertaId = null;

        limpiarContenedor(refs.contenidoDetalleIncidencia);
        refs.formResponderIncidencia?.reset();

        if (refs.responderIncidenciaId) refs.responderIncidenciaId.value = "";

        if (refs.textoRespuestaIncidencia) {
            refs.textoRespuestaIncidencia.disabled = false;
            refs.textoRespuestaIncidencia.placeholder = "Escribe aquí la respuesta que se enviará por correo al usuario...";
        }

        if (refs.btnEnviarRespuestaIncidencia) {
            refs.btnEnviarRespuestaIncidencia.disabled = false;
        }
    });

    configurarCerrarModal(refs.modalCambiarEstadoPedido, refs.cerrarModalCambiarEstadoPedido, refs.cancelarModalCambiarEstadoPedido, () => {
        refs.formCambiarEstadoPedido?.reset();
        state.pedidoCambioEstado = null;
        limpiarOpcionesEstadosPedido(refs);
    });

    refs.confirmarEliminarProducto?.addEventListener("click", async () => {
        await eliminarProducto(refs, state);
    });

    refs.confirmarEliminarUsuario?.addEventListener("click", async () => {
        await eliminarUsuario(refs, state);
    });
}

function configurarCerrarModal(modal, btnCerrar, btnCancelar, onClose) {
    btnCerrar?.addEventListener("click", () => cerrarModal(modal, onClose));
    btnCancelar?.addEventListener("click", () => cerrarModal(modal, onClose));

    modal?.addEventListener("click", (e) => {
        if (e.target === modal) cerrarModal(modal, onClose);
    });
}

/* =========================
   CARGAS
========================= */

async function cargarTodo(refs, state) {
    await Promise.all([
        cargarMetricas(refs),
        cargarEstadosPedidoDisponibles(refs, state),
        cargarEstadosIncidenciaDisponibles(refs, state)
    ]);

    await Promise.all([
        cargarProductos(refs, state, true),
        cargarEstablecimientos(refs, state),
        cargarPuntosRecogida(refs, state),
        cargarUsuarios(refs, state),
        cargarPedidos(refs, state),
        cargarIncidencias(refs, state)
    ]);
}

function iniciarRefrescoAutomaticoIncidencias(refs, state) {
    if (state.intervaloIncidenciasAdmin) {
        clearInterval(state.intervaloIncidenciasAdmin);
    }

    state.intervaloIncidenciasAdmin = setInterval(async () => {
        await cargarIncidencias(refs, state, true);
    }, 10000);
}

function iniciarRefrescoEstadoScraping(refs, state) {
    if (state.intervaloEstadoScraping) {
        clearInterval(state.intervaloEstadoScraping);
    }

    state.intervaloEstadoScraping = setInterval(async () => {
        await cargarEstadoScrapingAdmin(refs, state, { silencioso: true });
    }, 15000);
}

async function cargarMetricas(refs) {
    try {
        const [resProductos, resUsuarios, resPedidos] = await Promise.all([
            fetch(`${BASE_URL}/productos/catalogo?incluirNoDisponibles=true&page=0&size=1`, {
                method: "GET",
                credentials: "include"
            }),
            fetch(`${BASE_URL}/usuarios`, {
                method: "GET",
                credentials: "include"
            }),
            fetch(`${BASE_URL}/pedidos`, {
                method: "GET",
                credentials: "include"
            })
        ]);

        if (resProductos.ok) {
            const dataProductos = await resProductos.json();
            refs.totalProductos.textContent = obtenerTotalElementos(dataProductos);
        }

        if (resUsuarios.ok) {
            const usuarios = await resUsuarios.json();
            refs.totalUsuarios.textContent = Array.isArray(usuarios) ? usuarios.length : 0;
        }

        if (resPedidos.ok) {
            const pedidos = await resPedidos.json();
            refs.totalPedidos.textContent = Array.isArray(pedidos) ? pedidos.length : 0;
        }
    } catch (error) {
        console.error("Error cargando métricas:", error);
    }
}

async function cargarEstadosPedidoDisponibles(refs, state) {
    try {
        const response = await fetch(`${BASE_URL}/pedidos/estados-disponibles`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los estados de pedido");
        }

        state.estadosPedidoDisponibles = await response.json();
        renderizarFiltroEstadosPedido(refs, state);
    } catch (error) {
        console.error("Error cargando estados disponibles del pedido:", error);
        renderizarFiltroEstadosPedidoFallback(refs);
    }
}

async function cargarEstadosIncidenciaDisponibles(refs, state) {
    try {
        const response = await fetch(`${BASE_URL}/admin/incidencias/estados`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los estados de incidencia");
        }

        state.estadosIncidenciaDisponibles = await response.json();
        renderizarFiltroEstadosIncidencia(refs, state);
    } catch (error) {
        console.error("Error cargando estados de incidencia:", error);
        state.estadosIncidenciaDisponibles = [];
        renderizarFiltroEstadosIncidencia(refs, state);
    }
}

function renderizarFiltroEstadosIncidencia(refs, state) {
    if (!refs.filtroEstadoIncidencias) return;

    const valorActual = refs.filtroEstadoIncidencias.value || "ABIERTAS";

    refs.filtroEstadoIncidencias.innerHTML = "";

    const optionAbiertas = document.createElement("option");
    optionAbiertas.value = "ABIERTAS";
    optionAbiertas.textContent = "Abiertas / en curso";
    refs.filtroEstadoIncidencias.appendChild(optionAbiertas);

    const optionTodas = document.createElement("option");
    optionTodas.value = "TODOS";
    optionTodas.textContent = "Todas las incidencias";
    refs.filtroEstadoIncidencias.appendChild(optionTodas);

    if (Array.isArray(state.estadosIncidenciaDisponibles)) {
        state.estadosIncidenciaDisponibles.forEach((estado) => {
            const option = document.createElement("option");
            option.value = estado;
            option.textContent = formatearEstadoIncidenciaTexto(estado);
            refs.filtroEstadoIncidencias.appendChild(option);
        });
    }

    const existeValor = Array.from(refs.filtroEstadoIncidencias.options)
        .some((option) => option.value === valorActual);

    refs.filtroEstadoIncidencias.value = existeValor ? valorActual : "ABIERTAS";
}

async function cargarProductos(refs, state, reiniciar = true) {
    if (state.cargandoProductos) return;
    if (state.ultimaPaginaProductos && !reiniciar) return;

    if (reiniciar) {
        if (state.observadorProductos) {
            state.observadorProductos.disconnect();
            state.observadorProductos = null;
        }

        state.paginaProductos = 0;
        state.ultimaPaginaProductos = false;
        state.totalProductosCatalogo = 0;
        state.productos = [];
        state.productosFiltrados = [];
        limpiarSeleccionProductos(state);

        mostrarEstado(refs.estadoProductos, "Cargando productos...", "info");
        renderizarEstadoVacio(refs.contenedorProductos, "Cargando productos", "El catálogo aparecerá aquí automáticamente.");
        actualizarBotonStockSeleccionados(refs, state);
    }

    try {
        state.cargandoProductos = true;

        const response = await fetch(construirUrlProductosAdmin(refs, state), {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los productos");
        }

        const data = await response.json();
        const pagina = normalizarPaginaProductos(data, state);

        state.totalProductosCatalogo = pagina.totalElementos;
        state.ultimaPaginaProductos = pagina.ultimaPagina;
        state.paginaProductos = pagina.paginaActual;

        if (reiniciar) {
            state.productos = [...pagina.productos];
        } else {
            state.productos = unirProductosSinDuplicados(state.productos, pagina.productos);
        }

        sincronizarInformacionProductosSeleccionados(state, pagina.productos);
        state.productosFiltrados = [...state.productos];

        ocultarEstado(refs.estadoProductos);
        renderizarProductos(refs, state);
    } catch (error) {
        console.error("Error cargando productos:", error);
        mostrarEstado(refs.estadoProductos, "No se pudieron cargar los productos.", "error");

        if (reiniciar) {
            renderizarEstadoVacio(refs.contenedorProductos, "Error al cargar", "No se pudo obtener el catálogo.");
        } else {
            state.paginaProductos = Math.max(0, state.paginaProductos - 1);
        }
    } finally {
        state.cargandoProductos = false;
    }
}

async function cargarEstablecimientos(refs, state) {
    try {
        mostrarEstado(refs.estadoEstablecimientos, "Cargando establecimientos...", "info");

        const response = await fetch(`${BASE_URL}/establecimientos`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los establecimientos");
        }

        state.establecimientos = await response.json();
        state.establecimientosFiltrados = [...state.establecimientos];

        ocultarEstado(refs.estadoEstablecimientos);
        renderizarEstablecimientos(refs, state);
    } catch (error) {
        console.error("Error cargando establecimientos:", error);
        mostrarEstado(refs.estadoEstablecimientos, "No se pudieron cargar los establecimientos.", "error");
        renderizarEstadoVacio(
            refs.contenedorEstablecimientos,
            "Error al cargar",
            "No se pudo obtener la lista de establecimientos."
        );
    }
}

async function cargarPuntosRecogida(refs, state) {
    try {
        mostrarEstado(refs.estadoPuntosRecogida, "Cargando puntos de recogida...", "info");

        const response = await fetch(`${BASE_URL}/puntos-recogida`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los puntos de recogida");
        }

        state.puntosRecogida = await response.json();
        state.puntosRecogidaFiltrados = [...state.puntosRecogida];

        ocultarEstado(refs.estadoPuntosRecogida);
        renderizarPuntosRecogida(refs, state);
    } catch (error) {
        console.error("Error cargando puntos de recogida:", error);
        mostrarEstado(refs.estadoPuntosRecogida, "No se pudieron cargar los puntos de recogida.", "error");
        renderizarEstadoVacio(
            refs.contenedorPuntosRecogida,
            "Error al cargar",
            "No se pudo obtener la lista de puntos de recogida."
        );
    }
}

async function cargarUsuarios(refs, state) {
    try {
        mostrarEstado(refs.estadoUsuarios, "Cargando usuarios...", "info");

        const response = await fetch(`${BASE_URL}/usuarios`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los usuarios");
        }

        state.usuarios = await response.json();

        ocultarEstado(refs.estadoUsuarios);
        renderizarUsuarios(refs, state);
    } catch (error) {
        console.error("Error cargando usuarios:", error);
        mostrarEstado(refs.estadoUsuarios, "No se pudieron cargar los usuarios.", "error");
        renderizarEstadoVacio(refs.contenedorUsuarios, "Error al cargar", "No se pudo obtener la lista de usuarios.");
    }
}

async function cargarPedidos(refs, state) {
    try {
        mostrarEstado(refs.estadoPedidos, "Cargando pedidos...", "info");

        const filtro = refs.filtroEstadoPedidos?.value || "TODOS";
        const url = filtro === "TODOS"
            ? `${BASE_URL}/pedidos`
            : `${BASE_URL}/pedidos/estado/${encodeURIComponent(filtro)}`;

        const response = await fetch(url, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los pedidos");
        }

        state.pedidos = await response.json();

        ocultarEstado(refs.estadoPedidos);
        renderizarPedidos(refs, state);
    } catch (error) {
        console.error("Error cargando pedidos:", error);
        mostrarEstado(refs.estadoPedidos, "No se pudieron cargar los pedidos.", "error");
        renderizarEstadoVacio(refs.contenedorPedidos, "Error al cargar", "No se pudo obtener la lista de pedidos.");
    }
}

async function cargarIncidencias(refs, state, silencioso = false) {
    if (state.cargandoIncidencias) {
        return;
    }

    try {
        state.cargandoIncidencias = true;

        if (!silencioso) {
            mostrarEstado(refs.estadoIncidencias, "Cargando incidencias...", "info");
        }

        const filtro = refs.filtroEstadoIncidencias?.value || "ABIERTAS";

        const url = filtro === "ABIERTAS" || filtro === "TODOS"
            ? `${BASE_URL}/admin/incidencias`
            : `${BASE_URL}/admin/incidencias?estado=${encodeURIComponent(filtro)}`;

        const response = await fetch(url, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            let mensaje = "No se pudieron cargar las incidencias.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        let incidencias = await response.json();

        if (!Array.isArray(incidencias)) {
            incidencias = [];
        }

        if (filtro === "ABIERTAS") {
            incidencias = incidencias.filter((incidencia) => incidencia.estadoIncidencia !== "CERRADA");
        }

        const incidenciasOrdenadas = ordenarIncidenciasParaAdmin(incidencias);
        const nuevoSnapshot = crearSnapshotIncidencias(incidenciasOrdenadas);

        if (silencioso && nuevoSnapshot === state.snapshotIncidencias) {
            return;
        }

        state.snapshotIncidencias = nuevoSnapshot;
        state.incidencias = incidenciasOrdenadas;

        if (!silencioso) {
            ocultarEstado(refs.estadoIncidencias);
        }

        renderizarIncidencias(refs, state);
    } catch (error) {
        console.error("Error cargando incidencias:", error);

        if (!silencioso) {
            mostrarEstado(refs.estadoIncidencias, error.message || "No se pudieron cargar las incidencias.", "error");
            renderizarEstadoVacio(
                refs.contenedorIncidencias,
                "Error al cargar",
                "No se pudo obtener la lista de incidencias."
            );
        }
    } finally {
        state.cargandoIncidencias = false;
    }
}

function ordenarIncidenciasParaAdmin(incidencias) {
    return [...incidencias].sort((a, b) => {
        const prioridadA = obtenerPrioridadIncidencia(a.estadoIncidencia);
        const prioridadB = obtenerPrioridadIncidencia(b.estadoIncidencia);

        if (prioridadA !== prioridadB) {
            return prioridadA - prioridadB;
        }

        const fechaA = new Date(a.fechaUltimaActualizacion || a.fechaCreacion || 0).getTime();
        const fechaB = new Date(b.fechaUltimaActualizacion || b.fechaCreacion || 0).getTime();

        return fechaB - fechaA;
    });
}

function obtenerPrioridadIncidencia(estado) {
    if (estado === "PENDIENTE") return 1;
    if (estado === "RESPONDIDA_POR_USUARIO") return 2;
    if (estado === "EN_REVISION") return 3;
    if (estado === "ESPERANDO_RESPUESTA_USUARIO") return 4;
    if (estado === "RESUELTA") return 5;
    if (estado === "CERRADA") return 99;

    return 50;
}

/* =========================
   SCRAPING
========================= */

async function cargarEstadoScrapingAdmin(refs, state, opciones = {}) {
    if (state.actualizandoEstadoScraping && !opciones.forzar) {
        return state.ultimoEstadoScrapingAdmin;
    }

    state.actualizandoEstadoScraping = true;

    try {
        const response = await fetch(`${BASE_URL}/productos/scraping/estado`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error(`No se pudo cargar el estado del scraping (HTTP ${response.status})`);
        }

        const data = await response.json();
        state.ultimoEstadoScrapingAdmin = data;
        pintarEstadoScrapingAdmin(refs, state, data);
        return data;
        await cargarEstadoScrapingAdmin(refs, state, {
            silencioso: true,
            forzar: true
        });
    } catch (error) {
        console.error("Error cargando estado del scraping:", error);

        if (!opciones.silencioso) {
            mostrarMensaje(refs, error.message || "No se pudo cargar el estado del scraping.", "error");
        }

        if (refs.scrapingRelayBox) refs.scrapingRelayBox.textContent = "Sin datos";
        if (refs.scrapingRelayDetalle) refs.scrapingRelayDetalle.textContent = "No se pudo actualizar el estado del puente local.";
        return null;
    } finally {
        state.actualizandoEstadoScraping = false;
    }
}

function pintarEstadoScrapingAdmin(refs, state, data) {
    if (!data) {
        return;
    }

    const ultima = data.ultimaEjecucion || null;
    const totalPendientes = numeroSeguro(data.totalPendientes);
    const relayHabilitado = Boolean(data.relayHabilitado);
    const relayDisponible = Boolean(data.relayDisponible);
    const automaticoHabilitado = Boolean(data.automaticoHabilitado);

    if (ultima?.estado === "EN_CURSO" || data.scrapingEnCurso) {
        actualizarEstadoScraping(
            refs,
            "En curso",
            construirDetalleEstadoScraping(ultima, "Hay un scraping ejecutandose ahora mismo."),
            "info"
        );
    } else if (totalPendientes > 0 || ultima?.estado === "PENDIENTE") {
        actualizarEstadoScraping(
            refs,
            "Pendiente",
            construirDetallePendienteScraping(data, ultima),
            "info"
        );
    } else if (ultima?.estado === "ERROR") {
        actualizarEstadoScraping(
            refs,
            "Error",
            construirDetalleEstadoScraping(ultima, ultima?.detalleError || "El ultimo scraping termino con errores."),
            "error"
        );
    } else if (ultima) {
        actualizarEstadoScraping(
            refs,
            "Finalizado",
            construirDetalleEstadoScraping(ultima, ultima?.mensajeEstado || "Ultimo scraping completado."),
            "success"
        );
    } else {
        actualizarEstadoScraping(refs, "Inactivo", "Sin ejecuciones recientes.", "neutral");
    }

    if (refs.scrapingRelayBox) {
        refs.scrapingRelayBox.textContent = relayHabilitado
            ? (relayDisponible ? "Conectado" : "Servidor local apagado")
            : "No usa puente";
    }

    if (refs.scrapingRelayDetalle) {
        refs.scrapingRelayDetalle.textContent = relayHabilitado
            ? (data.relayMensaje || (relayDisponible ? "Puente local disponible." : "Puente local no disponible."))
            : "El scraping se ejecuta en el propio servidor.";
    }

    if (refs.scrapingAutoBox) {
        refs.scrapingAutoBox.textContent = automaticoHabilitado
            ? `Activo cada ${formatearFrecuenciaScraping(data.frecuenciaAutomaticaMs)}`
            : "Desactivado";
    }

    if (refs.scrapingAutoDetalle) {
        refs.scrapingAutoDetalle.textContent = automaticoHabilitado
            ? `Si algo queda pendiente, se reintentara cada ${formatearFrecuenciaScraping(data.intervaloReintentoMs)}.`
            : "Solo funcionaran los lanzamientos manuales desde el panel.";
    }

    renderizarPendientesScraping(refs, data);

    if (data.ultimoResultado && ultima?.estado !== "EN_CURSO") {
        pintarResultadoScraping(
            refs,
            normalizarResultadoScraping(data.ultimoResultado, ultima?.nombreProceso || "Scraping")
        );
    } else if (!data.ultimoResultado && refs.scrapingResultadoPanel) {
        refs.scrapingResultadoPanel.style.display = "none";
    }
}

function construirDetalleEstadoScraping(ultima, fallback) {
    if (!ultima) {
        return fallback || "Sin actividad reciente.";
    }

    const partes = [];

    if (ultima.nombreProceso) {
        partes.push(ultima.nombreProceso);
    }

    if (ultima.origen) {
        partes.push(`origen ${formatearOrigenScraping(ultima.origen)}`);
    }

    if (ultima.fechaInicio) {
        partes.push(`inicio ${formatearFecha(ultima.fechaInicio)}`);
    }

    if (ultima.duracionMs) {
        partes.push(`duracion ${formatearDuracionScraping(ultima.duracionMs)}`);
    }

    if (fallback) {
        partes.push(fallback);
    }

    return partes.join(" · ");
}

function construirDetallePendienteScraping(data, ultima) {
    const totalPendientes = numeroSeguro(data?.totalPendientes);
    const reintento = formatearFrecuenciaScraping(data?.intervaloReintentoMs);

    if (totalPendientes > 0) {
        return `${totalPendientes} scraping pendiente(s). Se volvera a intentar cada ${reintento}.`;
    }

    return construirDetalleEstadoScraping(
        ultima,
        ultima?.mensajeEstado || "Hay una peticion esperando a que vuelva el puente local."
    );
}

function renderizarPendientesScraping(refs, data) {
    if (!refs.scrapingPendientesLista) {
        return;
    }

    const pendientes = Array.isArray(data?.pendientes) ? data.pendientes : [];
    const totalPendientes = numeroSeguro(data?.totalPendientes);

    if (refs.scrapingColaTitulo) {
        refs.scrapingColaTitulo.textContent = totalPendientes > 0
            ? `${totalPendientes} tarea(s) pendiente(s)`
            : "Sin tareas pendientes";
    }

    if (refs.scrapingColaBadge) {
        refs.scrapingColaBadge.textContent = `${totalPendientes} pendiente${totalPendientes === 1 ? "" : "s"}`;
    }

    if (refs.scrapingColaResumen) {
        if (totalPendientes > 0) {
            refs.scrapingColaResumen.textContent = `El servidor ya ha guardado estas peticiones. Se relanzaran automaticamente cada ${formatearFrecuenciaScraping(data?.intervaloReintentoMs)} cuando el puente local vuelva a responder.`;
        } else if (data?.relayHabilitado && !data?.relayDisponible) {
            refs.scrapingColaResumen.textContent = "Ahora mismo el puente local no esta accesible, pero todavia no hay tareas guardadas en cola.";
        } else {
            refs.scrapingColaResumen.textContent = "No hay peticiones pendientes. Si el ordenador local se apaga, las nuevas solicitudes se guardaran aqui.";
        }
    }

    limpiarContenedor(refs.scrapingPendientesLista);

    if (!pendientes.length) {
        refs.scrapingPendientesLista.appendChild(
            el("div", {
                className: "scraping-pendiente-card",
                children: [
                    el("p", {
                        text: "Todo limpio. No hay ningun scraping esperando a que vuelva el equipo local."
                    })
                ]
            })
        );
        return;
    }

    pendientes.forEach((pendiente) => {
        refs.scrapingPendientesLista.appendChild(crearCardPendienteScraping(pendiente));
    });
}

function crearCardPendienteScraping(pendiente) {
    const ultimoError = pendiente?.ultimoError || "Sin detalle del ultimo intento.";

    return el("article", {
        className: "scraping-pendiente-card",
        children: [
            el("div", {
                className: "scraping-pendiente-header",
                children: [
                    el("h5", { text: pendiente?.nombreProceso || "Scraping pendiente" }),
                    el("span", {
                        className: "scraping-pendiente-badge",
                        text: `${numeroSeguro(pendiente?.intentos)} intento(s)`
                    })
                ]
            }),
            el("div", {
                className: "scraping-pendiente-meta",
                children: [
                    crearMetaPendienteScraping("Creado", formatearFecha(pendiente?.fechaCreacion)),
                    crearMetaPendienteScraping("Ultimo intento", formatearFecha(pendiente?.fechaUltimoIntento)),
                    crearMetaPendienteScraping("Estado", pendiente?.estado || "PENDIENTE"),
                    crearMetaPendienteScraping("Ultimo error", recortarTexto(ultimoError, 120))
                ]
            })
        ]
    });
}

function crearMetaPendienteScraping(etiqueta, valor) {
    return el("div", {
        className: "scraping-pendiente-meta-item",
        children: [
            el("span", { text: etiqueta }),
            el("strong", { text: valor || "-" })
        ]
    });
}

function formatearFrecuenciaScraping(valorMs) {
    const ms = numeroSeguro(valorMs);

    if (!ms) {
        return "0 min";
    }

    const totalMinutos = Math.max(1, Math.round(ms / 60000));

    if (totalMinutos < 60) {
        return `${totalMinutos} min`;
    }

    const horas = Math.floor(totalMinutos / 60);
    const minutosRestantes = totalMinutos % 60;

    if (!minutosRestantes) {
        return `${horas} h`;
    }

    return `${horas} h ${minutosRestantes} min`;
}

function formatearOrigenScraping(origen) {
    switch (origen) {
        case "AUTOMATICO": return "automatico";
        case "REINTENTO_PENDIENTE": return "reintento";
        case "MANUAL": return "manual";
        default: return "desconocido";
    }
}

function mostrarOverlayScraping(refs, titulo, detalle, paso) {
    if (!refs.scrapingOverlay) {
        return;
    }

    refs.scrapingOverlay.style.display = "flex";
    refs.scrapingOverlay.setAttribute("aria-hidden", "false");
    statefulActualizarOverlayScrapingTimer(refs, true);
    actualizarOverlayScraping(refs, titulo, detalle, paso);
}

function actualizarOverlayScraping(refs, titulo, detalle, paso) {
    if (refs.scrapingOverlayTitulo && titulo) refs.scrapingOverlayTitulo.textContent = titulo;
    if (refs.scrapingOverlayDetalle && detalle) refs.scrapingOverlayDetalle.textContent = detalle;
    if (refs.scrapingOverlayStep && paso) refs.scrapingOverlayStep.textContent = paso;
}

function ocultarOverlayScraping(refs) {
    if (!refs.scrapingOverlay) {
        return;
    }

    refs.scrapingOverlay.style.display = "none";
    refs.scrapingOverlay.setAttribute("aria-hidden", "true");
    statefulActualizarOverlayScrapingTimer(refs, false);
}

function statefulActualizarOverlayScrapingTimer(refs, activar) {
    if (activar) {
        if (!refs.scrapingOverlayTimer) {
            return;
        }

        refs.scrapingOverlayTimer.dataset.start = String(Date.now());
        refs.scrapingOverlayTimer.textContent = "00:00";

        if (refs.scrapingOverlayTimer.intervalId) {
            clearInterval(refs.scrapingOverlayTimer.intervalId);
        }

        refs.scrapingOverlayTimer.intervalId = setInterval(() => {
            const inicio = Number(refs.scrapingOverlayTimer.dataset.start || Date.now());
            const transcurrido = Math.max(0, Date.now() - inicio);
            const segundos = Math.floor(transcurrido / 1000);
            const minutos = String(Math.floor(segundos / 60)).padStart(2, "0");
            const restoSegundos = String(segundos % 60).padStart(2, "0");
            refs.scrapingOverlayTimer.textContent = `${minutos}:${restoSegundos}`;
        }, 1000);

        return;
    }

    if (refs.scrapingOverlayTimer?.intervalId) {
        clearInterval(refs.scrapingOverlayTimer.intervalId);
        refs.scrapingOverlayTimer.intervalId = null;
    }
}

async function ejecutarScraping(refs, state, url, nombre, boton, textoOriginal) {
    try {
        bloquearBoton(boton, "Ejecutando...");
        actualizarEstadoScraping(refs, "Preparando", `Preparando ${nombre}...`, "info");
        mostrarMensaje(refs, `Iniciando ${nombre}...`, "info");
        mostrarOverlayScraping(
            refs,
            `Preparando ${nombre}...`,
            "Comprobando el estado del servidor y del puente local.",
            "Preparando solicitud..."
        );

        const estadoPrevio = await cargarEstadoScrapingAdmin(refs, state, {
            silencioso: true,
            forzar: true
        });

        if (estadoPrevio?.relayHabilitado) {
            if (estadoPrevio?.relayDisponible) {
                actualizarEstadoScraping(refs, "Conectando", `El puente local esta disponible para ${nombre}.`, "info");
                actualizarOverlayScraping(
                    refs,
                    `${nombre} en camino`,
                    "Puente local conectado. La peticion se va a enviar al relay real.",
                    "Puente local conectado"
                );
            } else {
                actualizarEstadoScraping(refs, "Pendiente", `El puente local no responde. Si falla la llamada, ${nombre} se guardara en cola.`, "info");
                actualizarOverlayScraping(
                    refs,
                    `${nombre} esperando al puente`,
                    "El ordenador local parece apagado. Si no responde, guardaremos la peticion para reintentarlo despues.",
                    "Servidor local apagado o no disponible"
                );
            }
        } else {
            actualizarOverlayScraping(
                refs,
                `${nombre} en servidor`,
                "Este scraping se ejecuta directamente en el servidor.",
                "Ejecucion directa"
            );
        }

        actualizarOverlayScraping(
            refs,
            `${nombre} ejecutandose`,
            "La peticion ya se ha enviado. Estamos esperando a que termine el scraping.",
            "Esperando resultado del scraping..."
        );

        const response = await fetch(`${BASE_URL}${url}`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            let mensajeError = `Error HTTP ${response.status}`;

            try {
                const texto = await response.text();
                if (texto) mensajeError = texto;
            } catch (_) {}

            throw new Error(mensajeError);
        }

        const data = await response.json();
        const resultado = normalizarResultadoScraping(data, nombre);

        if (resultado.pendiente) {
            if (refs.scrapingResultadoPanel) {
                refs.scrapingResultadoPanel.style.display = "none";
            }

            actualizarEstadoScraping(
                refs,
                "Pendiente",
                resultado.mensajeEstado || `${resultado.nombreProceso} queda pendiente.`,
                "info"
            );

            actualizarOverlayScraping(
                refs,
                `${resultado.nombreProceso} en cola`,
                resultado.mensajeEstado || "La peticion se ha guardado para cuando vuelva el equipo local.",
                "Solicitud guardada correctamente"
            );

            mostrarMensaje(
                refs,
                resultado.mensajeEstado || `${resultado.nombreProceso} queda pendiente.`,
                "info"
            );

            await cargarEstadoScrapingAdmin(refs, state, {
                silencioso: true,
                forzar: true
            });
            return;
        }

        pintarResultadoScraping(refs, resultado);

        actualizarEstadoScraping(
            refs,
            "Completado",
            `${resultado.nombreProceso} finalizado. Encontrados: ${formatearNumero(resultado.totalProductosEncontrados)} · Nuevos: ${formatearNumero(resultado.totalProductosNuevos)} · Actualizados: ${formatearNumero(resultado.totalProductosActualizados)}`,
            "success"
        );

        actualizarOverlayScraping(
            refs,
            `${resultado.nombreProceso} finalizado`,
            `Encontrados: ${formatearNumero(resultado.totalProductosEncontrados)} · Nuevos: ${formatearNumero(resultado.totalProductosNuevos)} · Actualizados: ${formatearNumero(resultado.totalProductosActualizados)}`,
            "Proceso completado"
        );

        mostrarMensaje(refs, `${resultado.nombreProceso} completado correctamente.`, "ok");

        await cargarProductos(refs, state, true);
        await cargarMetricas(refs);
        await cargarEstadoScrapingAdmin(refs, state, {
            silencioso: true,
            forzar: true
        });
    } catch (error) {
        console.error(`Error en ${nombre}:`, error);
        actualizarEstadoScraping(refs, "Error", `Falló ${nombre}`, "error");
        actualizarOverlayScraping(
            refs,
            `${nombre} con error`,
            error.message || `No se pudo ejecutar ${nombre}.`,
            "Proceso interrumpido"
        );
        mostrarMensaje(refs, error.message || `Error al ejecutar ${nombre}.`, "error");
    } finally {
        setTimeout(() => {
            ocultarOverlayScraping(refs);
        }, 700);
        restaurarBoton(boton, textoOriginal);
    }
}

function actualizarEstadoScraping(refs, estado, detalle, tipo) {
    if (refs.estadoScraping) refs.estadoScraping.textContent = estado;
    if (refs.detalleScraping) refs.detalleScraping.textContent = detalle;
    if (refs.scrapingEstadoBox) refs.scrapingEstadoBox.textContent = estado;
    if (refs.scrapingUltimaAccion) refs.scrapingUltimaAccion.textContent = detalle;

    if (refs.chipScraping) {
        refs.chipScraping.className = "status-chip";

        if (tipo === "success") refs.chipScraping.classList.add("status-chip-success");
        else if (tipo === "error") refs.chipScraping.classList.add("status-chip-error");
        else if (tipo === "info") refs.chipScraping.classList.add("status-chip-info");
        else refs.chipScraping.classList.add("status-chip-neutral");

        refs.chipScraping.textContent = estado;
    }
}

function normalizarResultadoScraping(data, nombreFallback) {
    if (Array.isArray(data)) {
        return crearResultadoScrapingDesdeLista(data, nombreFallback);
    }

    const resultadosPorTienda = Array.isArray(data?.resultadosPorTienda)
        ? data.resultadosPorTienda
        : [];

    return {
        nombreProceso: data?.nombreProceso || nombreFallback || "Scraping",
        totalProductosEncontrados: numeroSeguro(data?.totalProductosEncontrados),
        totalProductosGuardados: numeroSeguro(data?.totalProductosGuardados),
        totalProductosNuevos: numeroSeguro(data?.totalProductosNuevos),
        totalProductosActualizados: numeroSeguro(data?.totalProductosActualizados),
        totalProductosCambioPrecio: numeroSeguro(data?.totalProductosCambioPrecio),
        totalProductosBajadaPrecio: numeroSeguro(data?.totalProductosBajadaPrecio),
        totalProductosSubidaPrecio: numeroSeguro(data?.totalProductosSubidaPrecio),
        totalProductosDesactivados: numeroSeguro(data?.totalProductosDesactivados),
        totalProductosSinImagen: numeroSeguro(data?.totalProductosSinImagen),
        totalProductosSinPrecio: numeroSeguro(data?.totalProductosSinPrecio),
        duracionMs: numeroSeguro(data?.duracionMs),
        pendiente: Boolean(data?.pendiente),
        mensajeEstado: data?.mensajeEstado || "",
        cambiosPrecio: Array.isArray(data?.cambiosPrecio) ? data.cambiosPrecio : [],
        resultadosPorTienda
    };
}

function crearResultadoScrapingDesdeLista(productos, nombreProceso) {
    const resultadosPorTienda = new Map();

    productos.forEach((producto) => {
        const tienda = producto?.tienda?.nombre || "Sin tienda";

        if (!resultadosPorTienda.has(tienda)) {
            resultadosPorTienda.set(tienda, {
                tienda,
                productosEncontrados: 0,
                productosGuardados: 0,
                productosNuevos: 0,
                productosActualizados: 0,
                productosCambioPrecio: 0,
                productosBajadaPrecio: 0,
                productosSubidaPrecio: 0,
                productosDesactivados: 0,
                productosSinImagen: 0,
                productosSinPrecio: 0
            });
        }

        const resumenTienda = resultadosPorTienda.get(tienda);

        resumenTienda.productosEncontrados++;
        resumenTienda.productosGuardados++;

        if (!producto?.urlImagen) {
            resumenTienda.productosSinImagen++;
        }

        if (!producto?.precio || Number(producto.precio) <= 0) {
            resumenTienda.productosSinPrecio++;
        }
    });

    const totalSinImagen = productos.filter((producto) => !producto?.urlImagen).length;
    const totalSinPrecio = productos.filter((producto) => !producto?.precio || Number(producto.precio) <= 0).length;

    return {
        nombreProceso: nombreProceso || "Scraping",
        totalProductosEncontrados: productos.length,
        totalProductosGuardados: productos.length,
        totalProductosNuevos: 0,
        totalProductosActualizados: 0,
        totalProductosCambioPrecio: 0,
        totalProductosBajadaPrecio: 0,
        totalProductosSubidaPrecio: 0,
        totalProductosDesactivados: 0,
        totalProductosSinImagen: totalSinImagen,
        totalProductosSinPrecio: totalSinPrecio,
        duracionMs: 0,
        pendiente: false,
        mensajeEstado: "",
        cambiosPrecio: [],
        resultadosPorTienda: Array.from(resultadosPorTienda.values())
    };
}

function pintarResultadoScraping(refs, resultado) {
    if (!refs.scrapingResultadoPanel) return;

    refs.scrapingResultadoPanel.style.display = "block";

    if (refs.scrapingResultadoTitulo) {
        refs.scrapingResultadoTitulo.textContent = `${resultado.nombreProceso} finalizado`;
    }

    if (refs.scrapingResultadoDuracion) {
        refs.scrapingResultadoDuracion.textContent = formatearDuracionScraping(resultado.duracionMs);
    }

    pintarNumeroScraping(refs.scrapingTotalEncontrados, resultado.totalProductosEncontrados);
    pintarNumeroScraping(refs.scrapingTotalGuardados, resultado.totalProductosGuardados);
    pintarNumeroScraping(refs.scrapingTotalNuevos, resultado.totalProductosNuevos);
    pintarNumeroScraping(refs.scrapingTotalActualizados, resultado.totalProductosActualizados);
    pintarNumeroScraping(refs.scrapingTotalCambiosPrecio, resultado.totalProductosCambioPrecio);
    pintarNumeroScraping(refs.scrapingTotalBajadasPrecio, resultado.totalProductosBajadaPrecio);
    pintarNumeroScraping(refs.scrapingTotalSubidasPrecio, resultado.totalProductosSubidaPrecio);
    pintarNumeroScraping(refs.scrapingTotalDesactivados, resultado.totalProductosDesactivados);
    pintarNumeroScraping(refs.scrapingTotalSinImagen, resultado.totalProductosSinImagen);
    pintarNumeroScraping(refs.scrapingTotalSinPrecio, resultado.totalProductosSinPrecio);

    renderizarResultadoPorTienda(refs, resultado.resultadosPorTienda);
    renderizarCambiosPrecioScraping(refs, resultado.cambiosPrecio);
}

function renderizarResultadoPorTienda(refs, resultadosPorTienda) {
    if (!refs.scrapingTiendasLista) return;

    limpiarContenedor(refs.scrapingTiendasLista);

    if (!Array.isArray(resultadosPorTienda) || resultadosPorTienda.length === 0) {
        refs.scrapingTiendasLista.appendChild(
            el("p", {
                className: "texto-box-vacio",
                text: "No hay datos por tienda para este proceso."
            })
        );
        return;
    }

    resultadosPorTienda.forEach((resultadoTienda) => {
        refs.scrapingTiendasLista.appendChild(crearCardResultadoTienda(resultadoTienda));
    });
}

function crearCardResultadoTienda(resultadoTienda) {
    const card = el("article", {
        className: "scraping-tienda-card"
    });

    const header = el("div", {
        className: "scraping-tienda-header"
    });

    header.appendChild(el("h5", {
        text: resultadoTienda.tienda || "Sin tienda"
    }));

    card.appendChild(header);

    const datos = el("div", {
        className: "scraping-tienda-datos"
    });

    datos.append(
        crearDatoResultadoScraping("Encontrados", resultadoTienda.productosEncontrados),
        crearDatoResultadoScraping("Guardados", resultadoTienda.productosGuardados),
        crearDatoResultadoScraping("Nuevos", resultadoTienda.productosNuevos),
        crearDatoResultadoScraping("Actualizados", resultadoTienda.productosActualizados),
        crearDatoResultadoScraping("Cambios precio", resultadoTienda.productosCambioPrecio),
        crearDatoResultadoScraping("Bajadas", resultadoTienda.productosBajadaPrecio),
        crearDatoResultadoScraping("Subidas", resultadoTienda.productosSubidaPrecio),
        crearDatoResultadoScraping("No disponibles", resultadoTienda.productosDesactivados),
        crearDatoResultadoScraping("Sin imagen", resultadoTienda.productosSinImagen),
        crearDatoResultadoScraping("Sin precio", resultadoTienda.productosSinPrecio)
    );

    card.appendChild(datos);

    return card;
}

function crearDatoResultadoScraping(label, valor) {
    const item = el("div", {
        className: "scraping-tienda-dato"
    });

    item.append(
        el("span", { text: label }),
        el("strong", { text: formatearNumero(valor) })
    );

    return item;
}

function pintarNumeroScraping(elemento, valor) {
    if (!elemento) return;
    elemento.textContent = formatearNumero(valor);
}

function formatearDuracionScraping(duracionMs) {
    const ms = numeroSeguro(duracionMs);

    if (ms <= 0) {
        return "Duración no disponible";
    }

    const segundosTotales = Math.round(ms / 1000);

    if (segundosTotales < 60) {
        return `${segundosTotales} s`;
    }

    const minutos = Math.floor(segundosTotales / 60);
    const segundos = segundosTotales % 60;

    return `${minutos} min ${segundos} s`;
}

function formatearNumero(valor) {
    return numeroSeguro(valor).toLocaleString("es-ES");
}

function numeroSeguro(valor) {
    const numero = Number(valor);
    return Number.isFinite(numero) ? numero : 0;
}

/* =========================
   PRODUCTOS
========================= */

function construirUrlProductosAdmin(refs, state) {
    const params = construirParamsFiltroProductosAdmin(refs);
    params.append("incluirNoDisponibles", "true");
    params.append("page", state.paginaProductos);
    params.append("size", state.sizeProductosAdmin);

    return `${BASE_URL}/productos/catalogo?${params.toString()}`;
}

function renderizarCambiosPrecioScraping(refs, cambiosPrecio) {
    if (!refs.scrapingCambiosLista) return;

    limpiarContenedor(refs.scrapingCambiosLista);

    if (!Array.isArray(cambiosPrecio) || cambiosPrecio.length === 0) {
        refs.scrapingCambiosLista.appendChild(
            el("p", {
                className: "texto-box-vacio",
                text: "No se han detectado cambios de precio en esta ejecucion."
            })
        );
        return;
    }

    cambiosPrecio.forEach((cambio) => {
        refs.scrapingCambiosLista.appendChild(crearCardCambioPrecioScraping(cambio));
    });
}

function crearCardCambioPrecioScraping(cambio) {
    const card = el("article", {
        className: "scraping-cambio-card"
    });

    const header = el("div", {
        className: "scraping-cambio-header"
    });

    const tituloWrap = el("div");
    tituloWrap.appendChild(el("h5", {
        text: cambio?.nombreProducto || "Producto"
    }));
    tituloWrap.appendChild(el("p", {
        text: `${cambio?.tienda || "Sin tienda"} · ${formatearCambioPrecioScraping(cambio)}`
    }));

    const badge = el("span", {
        className: `scraping-cambio-badge ${obtenerClaseCambioPrecioScraping(cambio?.tipoCambio)}`,
        text: formatearTipoCambioPrecio(cambio?.tipoCambio, cambio?.rebajaMayor)
    });

    header.append(tituloWrap, badge);
    card.appendChild(header);

    const detalle = [];

    if (cambio?.porcentajeDescuentoAnterior !== undefined || cambio?.porcentajeDescuentoNuevo !== undefined) {
        detalle.push(`Descuento: ${numeroSeguro(cambio?.porcentajeDescuentoAnterior)}% -> ${numeroSeguro(cambio?.porcentajeDescuentoNuevo)}%`);
    }

    if (cambio?.fechaCambio) {
        detalle.push(`Fecha: ${formatearFecha(cambio.fechaCambio)}`);
    }

    if (detalle.length > 0) {
        card.appendChild(el("p", {
            text: detalle.join(" · ")
        }));
    }

    if (cambio?.urlProducto) {
        const link = document.createElement("a");
        link.href = cambio.urlProducto;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.textContent = "Ver producto original";
        card.appendChild(link);
    }

    return card;
}

function formatearCambioPrecioScraping(cambio) {
    if (!cambio) return "Cambio detectado";

    const precioAnterior = formatearPrecio(cambio.precioAnterior);
    const precioNuevo = formatearPrecio(cambio.precioNuevo);
    const variacion = cambio.porcentajeVariacionPrecio;

    if (variacion === null || variacion === undefined || variacion === "") {
        return `${precioAnterior} -> ${precioNuevo}`;
    }

    const numero = Number(variacion);
    const signo = Number.isFinite(numero) && numero > 0 ? "+" : "";
    return `${precioAnterior} -> ${precioNuevo} (${signo}${variacion}%)`;
}

function obtenerClaseCambioPrecioScraping(tipoCambio) {
    switch (tipoCambio) {
        case "BAJADA": return "scraping-cambio-bajada";
        case "SUBIDA": return "scraping-cambio-subida";
        default: return "scraping-cambio-generico";
    }
}

function formatearTipoCambioPrecio(tipoCambio, rebajaMayor) {
    if (tipoCambio === "BAJADA") {
        return rebajaMayor ? "Bajada fuerte" : "Bajada";
    }

    if (tipoCambio === "SUBIDA") {
        return "Subida";
    }

    return "Cambio";
}

function construirUrlSeleccionSinStockProductosAdmin(refs) {
    const params = construirParamsFiltroProductosAdmin(refs);
    return `${BASE_URL}/productos/catalogo/seleccion-sin-stock?${params.toString()}`;
}

function construirParamsFiltroProductosAdmin(refs) {
    const params = new URLSearchParams();
    const busqueda = (refs.buscadorProductos?.value || "").trim();
    const tienda = refs.filtroTiendaProductos?.value || "";
    const seccion = refs.filtroSeccionProductos?.value || "";
    const categoria = refs.filtroCategoriaProductos?.value || "";
    const orden = refs.filtroOrdenProductos?.value || "recientes";

    if (tienda) params.append("tienda", tienda);
    if (seccion) params.append("seccion", seccion);
    if (categoria) params.append("categoria", categoria);
    if (busqueda) params.append("busqueda", busqueda);
    if (orden) params.append("orden", orden);

    return params;
}

function limpiarSeleccionProductos(state) {
    state.productosSeleccionados.clear();
    state.productosSeleccionadosInfo.clear();
    state.seleccionMasivaSinStockActiva = false;
}

function registrarProductosSeleccionados(state, productos) {
    if (!Array.isArray(productos)) {
        return;
    }

    productos.forEach((producto) => {
        if (!producto?.id) {
            return;
        }

        state.productosSeleccionados.add(producto.id);
        state.productosSeleccionadosInfo.set(producto.id, producto);
    });
}

function desregistrarProductosSeleccionados(state, productos) {
    if (!Array.isArray(productos)) {
        return;
    }

    productos.forEach((producto) => {
        if (!producto?.id) {
            return;
        }

        state.productosSeleccionados.delete(producto.id);
        state.productosSeleccionadosInfo.delete(producto.id);
    });
}

function sincronizarInformacionProductosSeleccionados(state, productos) {
    if (!Array.isArray(productos)) {
        return;
    }

    productos.forEach((producto) => {
        if (producto?.id && state.productosSeleccionados.has(producto.id)) {
            state.productosSeleccionadosInfo.set(producto.id, producto);
        }
    });
}

function obtenerProductosSeleccionados(state) {
    return Array.from(state.productosSeleccionados)
        .map((productoId) => state.productosSeleccionadosInfo.get(productoId))
        .filter(Boolean);
}

async function aplicarFiltroProductos(refs, state) {
    await cargarProductos(refs, state, true);
}

async function cargarMasProductosSiProcede(refs, state) {
    if (state.cargandoProductos || state.ultimaPaginaProductos) return;

    state.paginaProductos++;
    await cargarProductos(refs, state, false);
}

function prepararScrollInfinitoProductos(refs, state) {
    const sentinel = document.getElementById("sentinel-productos-admin");

    if (!sentinel) return;

    if (state.observadorProductos) {
        state.observadorProductos.disconnect();
    }

    state.observadorProductos = new IntersectionObserver(async (entries) => {
        const entrada = entries[0];

        if (entrada.isIntersecting) {
            await cargarMasProductosSiProcede(refs, state);
        }
    }, {
        root: null,
        rootMargin: "450px",
        threshold: 0.1
    });

    state.observadorProductos.observe(sentinel);
}

function renderizarProductos(refs, state) {
    limpiarContenedor(refs.contenedorProductos);

    if (!Array.isArray(state.productosFiltrados) || state.productosFiltrados.length === 0) {
        renderizarEstadoVacio(refs.contenedorProductos, "Sin resultados", "No se encontraron productos con los filtros actuales.");
        actualizarBotonStockSeleccionados(refs, state);
        return;
    }

    const resumen = el("div", {
        className: "estado-panel-admin info",
        text: `Mostrando ${state.productosFiltrados.length} de ${state.totalProductosCatalogo} productos`
    });

    refs.contenedorProductos.appendChild(resumen);

    state.productosFiltrados.forEach((producto) => {
        refs.contenedorProductos.appendChild(crearCardProducto(producto, refs, state));
    });

    const sentinel = el("div", {
        id: "sentinel-productos-admin",
        className: "empty-admin-state"
    });

    if (state.cargandoProductos) {
        sentinel.appendChild(el("p", { text: "Cargando más productos..." }));
    } else if (state.ultimaPaginaProductos) {
        sentinel.appendChild(el("p", { text: "No hay más productos para cargar." }));
    } else {
        sentinel.appendChild(el("p", { text: "Baja un poco más para cargar más productos..." }));
    }

    refs.contenedorProductos.appendChild(sentinel);

    actualizarBotonStockSeleccionados(refs, state);
    prepararScrollInfinitoProductos(refs, state);
}

function crearCardProducto(producto, refs, state) {
    const article = el("article", {
        className: `item-admin-card ${state.modoSeleccionProductos ? "seleccionable" : ""}`
    });

    if (state.modoSeleccionProductos) {
        const selector = el("div", { className: "selector-item-admin" });
        const check = el("input", { type: "checkbox" });
        check.checked = state.productosSeleccionados.has(producto.id);

        check.addEventListener("change", () => {
            if (check.checked) {
                registrarProductosSeleccionados(state, [producto]);
            } else {
                desregistrarProductosSeleccionados(state, [producto]);
            }

            state.seleccionMasivaSinStockActiva = false;
            actualizarBotonStockSeleccionados(refs, state);
        });

        selector.appendChild(check);
        article.appendChild(selector);
    }

    const media = el("div", { className: "item-admin-media" });

    if (producto.urlImagen) {
        const img = el("img", {
            src: producto.urlImagen,
            alt: producto.nombre || "Producto"
        });

        img.loading = "lazy";

        img.onerror = () => {
            limpiarContenedor(media);
            media.appendChild(el("span", { text: "Sin imagen" }));
        };

        media.appendChild(img);
    } else {
        media.appendChild(el("span", { text: "Sin imagen" }));
    }

    const body = el("div", { className: "item-admin-body" });
    body.appendChild(el("h3", { text: producto.nombre || "Sin nombre" }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadge(producto.tienda?.nombre || "Sin tienda"));
    meta.appendChild(crearBadge(producto.categoria?.nombre || "Sin categoría"));
    meta.appendChild(crearBadge(producto.seccion || "Sin sección"));
    if (producto.disponibleCatalogo === false) {
        meta.appendChild(crearBadge("No disponible"));
    }

    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-precio",
        text: formatearPrecio(producto.precio)
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: recortarTexto(producto.descripcion || "Sin descripción", 140)
    }));

    const acciones = el("div", { className: "item-admin-acciones" });

    const btnStock = crearBoton("Stock", "btn btn-secondary", () => {
        abrirModalStock(refs, state, [producto]);
    });

    const btnEditar = crearBoton("Editar", "btn btn-secondary", async () => {
        await abrirEditarProducto(refs, producto.id);
    });

    const btnEliminar = crearBoton("Eliminar", "btn btn-danger", () => {
        state.productoIdPendienteEliminar = producto.id;
        abrirModal(refs.modalEliminarProducto);
    });

    acciones.append(btnStock, btnEditar, btnEliminar);
    article.append(media, body, acciones);

    return article;
}

async function abrirEditarProducto(refs, productoId) {
    try {
        const response = await fetch(`${BASE_URL}/productos/${productoId}?incluirNoDisponibles=true`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) throw new Error("No se pudo cargar el producto");

        const producto = await response.json();

        refs.editarProductoId.value = producto.id || "";
        refs.editarProductoNombre.value = producto.nombre || "";
        refs.editarProductoDescripcion.value = producto.descripcion || "";
        refs.editarProductoPrecio.value = producto.precio ?? "";
        refs.editarProductoUrlImagen.value = producto.urlImagen || "";
        refs.editarProductoUrlProducto.value = producto.urlProducto || "";

        abrirModal(refs.modalEditarProducto);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo cargar el producto.", "error");
    }
}

async function guardarEdicionProducto(refs, state) {
    const productoId = refs.editarProductoId.value.trim();
    const nombre = refs.editarProductoNombre.value.trim();
    const descripcion = refs.editarProductoDescripcion.value.trim();
    const precio = refs.editarProductoPrecio.value.trim();
    const urlImagen = refs.editarProductoUrlImagen.value.trim();

    if (!productoId || !nombre || !precio) {
        mostrarMensaje(refs, "Nombre y precio son obligatorios.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarCambiosProducto, "Guardando...");

        const responseActual = await fetch(`${BASE_URL}/productos/${productoId}?incluirNoDisponibles=true`, {
            method: "GET",
            credentials: "include"
        });

        if (!responseActual.ok) throw new Error("No se pudo recuperar el producto actual");

        const productoCompleto = await responseActual.json();

        const payload = {
            id: productoCompleto.id,
            nombre,
            descripcion,
            precio,
            urlImagen,
            urlProducto: productoCompleto.urlProducto,
            seccion: productoCompleto.seccion,
            categoria: productoCompleto.categoria,
            tienda: productoCompleto.tienda,
            tallaStocks: productoCompleto.tallaStocks || []
        };

        const responseActualizar = await fetch(`${BASE_URL}/productos/${productoId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (!responseActualizar.ok) throw new Error("No se pudo actualizar el producto");

        cerrarModal(refs.modalEditarProducto, () => refs.formEditarProducto.reset());
        mostrarMensaje(refs, "Producto actualizado correctamente.", "ok");

        await cargarProductos(refs, state, true);
        await cargarMetricas(refs);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo actualizar el producto.", "error");
    } finally {
        restaurarBoton(refs.guardarCambiosProducto, "Guardar cambios");
    }
}

async function eliminarProducto(refs, state) {
    if (!state.productoIdPendienteEliminar) return;

    try {
        bloquearBoton(refs.confirmarEliminarProducto, "Eliminando...");

        const response = await fetch(`${BASE_URL}/productos/${state.productoIdPendienteEliminar}`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok) throw new Error("No se pudo eliminar el producto");

        cerrarModal(refs.modalEliminarProducto, () => {
            state.productoIdPendienteEliminar = null;
        });

        mostrarMensaje(refs, "Producto eliminado correctamente.", "ok");

        await cargarProductos(refs, state, true);
        await cargarMetricas(refs);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo eliminar el producto.", "error");
    } finally {
        restaurarBoton(refs.confirmarEliminarProducto, "Eliminar");
    }
}

function obtenerHelperTallasProducto() {
    return window.TallasProducto;
}

const ORDEN_TIPOS_STOCK = ["ropa", "calzado", "accesorio"];
const LABELS_TIPOS_STOCK = {
    ropa: "Ropa",
    calzado: "Calzado",
    accesorio: "Accesorios"
};

function agruparProductosStockPorTipo(productos) {
    const helperTallas = obtenerHelperTallasProducto();

    if (!helperTallas || !Array.isArray(productos) || productos.length === 0) {
        return [];
    }

    const grupos = new Map();

    productos.forEach((producto) => {
        const tipo = helperTallas.obtenerTipo(producto);

        if (!grupos.has(tipo)) {
            grupos.set(tipo, []);
        }

        grupos.get(tipo).push(producto);
    });

    return ORDEN_TIPOS_STOCK
        .filter((tipo) => grupos.has(tipo))
        .map((tipo) => ({
            tipo,
            label: LABELS_TIPOS_STOCK[tipo] || tipo,
            productos: grupos.get(tipo)
        }));
}

function renderizarOpcionesStock(refs, gruposStock) {
    const helperTallas = obtenerHelperTallasProducto();

    limpiarContenedor(refs.tallasStockProductos);

    if (!helperTallas || !Array.isArray(gruposStock) || gruposStock.length === 0) {
        refs.tallasStockProductos?.appendChild(
            crearTextoVacio("texto-box-vacio stock-tallas-aviso", "No hay productos seleccionados para editar stock.")
        );
        return;
    }

    gruposStock.forEach((grupo) => {
        const grupoBox = el("div", { className: "stock-tallas-grupo" });
        const cabecera = el("div", { className: "stock-tallas-grupo-header" });
        const contador = grupo.productos.length === 1 ? "1 producto" : `${grupo.productos.length} productos`;

        cabecera.append(
            el("strong", { text: grupo.label }),
            el("span", { text: contador })
        );

        const opciones = el("div", { className: "stock-tallas-opciones" });

        helperTallas.obtenerTallasPermitidas(grupo.productos[0]).forEach((talla) => {
            const input = el("input", {
                className: "check-stock-talla",
                type: "checkbox",
                value: talla
            });
            input.dataset.tipo = grupo.tipo;

            const label = el("label", { className: "check-talla" });
            label.append(input, el("span", { text: helperTallas.formatearTalla(talla) }));
            opciones.appendChild(label);
        });

        grupoBox.append(cabecera, opciones);
        refs.tallasStockProductos.appendChild(grupoBox);
    });
}

function renderizarResumenStockSeleccion(refs, gruposStock, productos) {
    limpiarContenedor(refs.stockResumenProductos);

    if (!Array.isArray(productos) || productos.length === 0) {
        return;
    }

    if (productos.length <= 24) {
        productos.forEach((producto) => {
            refs.stockResumenProductos.appendChild(el("span", {
                className: "resumen-stock-badge",
                text: producto.nombre || `Producto ${producto.id}`
            }));
        });
        return;
    }

    refs.stockResumenProductos.appendChild(el("span", {
        className: "resumen-stock-badge resumen-stock-badge-total",
        text: `${formatearNumero(productos.length)} productos seleccionados`
    }));

    gruposStock.forEach((grupo) => {
        refs.stockResumenProductos.appendChild(el("span", {
            className: "resumen-stock-badge",
            text: `${grupo.label}: ${formatearNumero(grupo.productos.length)}`
        }));
    });

    const nombresMuestra = productos
        .slice(0, 6)
        .map((producto) => producto.nombre || `Producto ${producto.id}`)
        .join(" · ");

    refs.stockResumenProductos.appendChild(el("p", {
        className: "stock-resumen-texto",
        text: nombresMuestra
    }));

    const restantes = productos.length - 6;
    if (restantes > 0) {
        refs.stockResumenProductos.appendChild(el("p", {
            className: "stock-resumen-texto stock-resumen-texto-soft",
            text: `y ${formatearNumero(restantes)} más`
        }));
    }
}

async function abrirModalStock(refs, state, productos) {
    const helperTallas = obtenerHelperTallasProducto();
    const gruposStock = agruparProductosStockPorTipo(productos);

    state.productosStockObjetivo = productos;
    state.gruposStockObjetivo = gruposStock;
    state.tipoStockObjetivo = gruposStock.length === 1 ? gruposStock[0].tipo : "mixto";

    if (refs.guardarStockProductos) {
        refs.guardarStockProductos.disabled = gruposStock.length === 0;
    }

    refs.stockCantidadProductos.value = "";
    renderizarResumenStockSeleccion(refs, gruposStock, productos);
    renderizarOpcionesStock(refs, gruposStock);

    limpiarContenedor(refs.stockActualProducto);

    if (productos.length === 1) {
        try {
            const response = await fetch(`${BASE_URL}/productos/${productos[0].id}/talla-stock?incluirNoDisponibles=true`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) throw new Error("No se pudo cargar stock");

            const tallas = await response.json();
            const tallasFiltradas = helperTallas
                ? helperTallas.filtrarTallaStocks(productos[0], tallas)
                : tallas;

            if (Array.isArray(tallasFiltradas) && tallasFiltradas.length > 0) {
                tallasFiltradas.forEach((item) => {
                    refs.stockActualProducto.appendChild(el("span", {
                        className: "stock-talla-badge",
                        text: `${helperTallas.formatearTalla(item.talla)}: ${item.stock}`
                    }));
                });
            } else {
                refs.stockActualProducto.appendChild(crearTextoVacio("texto-box-vacio", "No hay stock cargado todavía."));
            }
        } catch (error) {
            console.error(error);
            refs.stockActualProducto.appendChild(crearTextoVacio("texto-box-vacio", "No se pudo cargar el stock actual."));
        }
    } else {
        refs.stockActualProducto.appendChild(
            crearTextoVacio("texto-box-vacio", "Se aplicara el stock a cada grupo de productos con sus tallas correspondientes.")
        );
    }

    abrirModal(refs.modalStockProductos);
}

function obtenerTallasSeleccionadasPorTipo(refs) {
    const tallasPorTipo = new Map();

    Array.from(refs.tallasStockProductos?.querySelectorAll(".check-stock-talla:checked") || []).forEach((check) => {
        const tipo = check.dataset.tipo;

        if (!tipo) {
            return;
        }

        if (!tallasPorTipo.has(tipo)) {
            tallasPorTipo.set(tipo, []);
        }

        tallasPorTipo.get(tipo).push(check.value);
    });

    return tallasPorTipo;
}

function dividirEnBloques(items, tamano) {
    const lista = Array.isArray(items) ? items : [];
    const size = Math.max(1, Number(tamano) || 1);
    const bloques = [];

    for (let indice = 0; indice < lista.length; indice += size) {
        bloques.push(lista.slice(indice, indice + size));
    }

    return bloques;
}

async function guardarStockProductos(refs, state) {
    const helperTallas = obtenerHelperTallasProducto();
    const cantidad = Number(refs.stockCantidadProductos.value);
    const tallasSeleccionadasPorTipo = obtenerTallasSeleccionadasPorTipo(refs);
    const totalTallasSeleccionadas = Array.from(tallasSeleccionadasPorTipo.values())
        .reduce((total, tallas) => total + tallas.length, 0);

    if (!state.productosStockObjetivo.length) {
        mostrarMensaje(refs, "No hay productos seleccionados.", "error");
        return;
    }

    if (!state.gruposStockObjetivo.length) {
        mostrarMensaje(refs, "No hay grupos de stock disponibles.", "error");
        return;
    }

    if (!totalTallasSeleccionadas) {
        mostrarMensaje(refs, "Selecciona al menos una talla.", "error");
        return;
    }

    for (const grupo of state.gruposStockObjetivo) {
        const tallasSeleccionadas = tallasSeleccionadasPorTipo.get(grupo.tipo) || [];
        const tallasPermitidas = new Set(helperTallas.obtenerTallasPermitidas(grupo.productos[0]));

        if (tallasSeleccionadas.some((talla) => !tallasPermitidas.has(talla))) {
            mostrarMensaje(refs, "Hay tallas que no corresponden con estos productos.", "error");
            return;
        }
    }

    if (Number.isNaN(cantidad) || cantidad < 0) {
        mostrarMensaje(refs, "Introduce una cantidad válida.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarStockProductos, "Guardando...");

        for (const grupo of state.gruposStockObjetivo) {
            const tallasSeleccionadas = tallasSeleccionadasPorTipo.get(grupo.tipo) || [];
            const productoIds = grupo.productos
                .map((producto) => producto?.id)
                .filter(Boolean);

            if (!tallasSeleccionadas.length || !productoIds.length) {
                continue;
            }

            for (const bloqueProductoIds of dividirEnBloques(productoIds, 500)) {
                const response = await fetch(`${BASE_URL}/productos/talla-stock/masivo`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify({
                        productoIds: bloqueProductoIds,
                        tallas: tallasSeleccionadas,
                        stock: cantidad
                    })
                });

                if (!response.ok) {
                    throw new Error(`No se pudo guardar el stock para el grupo ${grupo.label}`);
                }
            }
        }

        cerrarModal(refs.modalStockProductos, () => {
            refs.formStockProductos.reset();
            state.productosStockObjetivo = [];
            state.gruposStockObjetivo = [];
            state.tipoStockObjetivo = null;
        });

        mostrarMensaje(refs, "Stock guardado correctamente.", "ok");
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo guardar el stock.", "error");
    } finally {
        restaurarBoton(refs.guardarStockProductos, "Guardar stock");
    }
}

function actualizarBotonStockSeleccionados(refs, state) {
    if (!refs.btnStockSeleccionados) return;
    refs.btnStockSeleccionados.disabled = state.productosSeleccionados.size === 0;
    refs.btnStockSeleccionados.textContent = state.productosSeleccionados.size > 0
        ? `Editar stock (${formatearNumero(state.productosSeleccionados.size)})`
        : "Editar stock";
}

/* =========================
   ESTABLECIMIENTOS
========================= */

function aplicarFiltroEstablecimientos(refs, state) {
    const termino = (refs.buscadorEstablecimientos?.value || "").trim().toLowerCase();

    if (!termino) {
        state.establecimientosFiltrados = [...state.establecimientos];
    } else {
        state.establecimientosFiltrados = state.establecimientos.filter((establecimiento) => {
            const nombre = (establecimiento.nombre || "").toLowerCase();
            const ciudad = (establecimiento.ciudad || "").toLowerCase();
            const provincia = (establecimiento.provincia || "").toLowerCase();
            const tienda = (establecimiento.tienda?.nombre || "").toLowerCase();
            const direccion = (establecimiento.direccion || "").toLowerCase();

            return (
                nombre.includes(termino) ||
                ciudad.includes(termino) ||
                provincia.includes(termino) ||
                tienda.includes(termino) ||
                direccion.includes(termino)
            );
        });
    }

    renderizarEstablecimientos(refs, state);
}

function renderizarEstablecimientos(refs, state) {
    limpiarContenedor(refs.contenedorEstablecimientos);

    if (!Array.isArray(state.establecimientosFiltrados) || state.establecimientosFiltrados.length === 0) {
        renderizarEstadoVacio(
            refs.contenedorEstablecimientos,
            "Sin establecimientos",
            "No se encontraron establecimientos con los filtros actuales."
        );
        return;
    }

    state.establecimientosFiltrados.forEach((establecimiento) => {
        refs.contenedorEstablecimientos.appendChild(
            crearCardEstablecimiento(establecimiento, refs, state)
        );
    });
}

function crearCardEstablecimiento(establecimiento, refs, state) {
    const article = el("article", { className: "item-admin-card" });

    const avatar = el("div", {
        className: "item-admin-avatar",
        text: "🏬"
    });

    const body = el("div", { className: "item-admin-body" });
    body.appendChild(el("h3", { text: establecimiento.nombre || "Sin nombre" }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadge(establecimiento.tienda?.nombre || "Sin tienda"));
    meta.appendChild(crearBadge(establecimiento.disponible ? "Disponible" : "No disponible"));
    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Dirección: ${establecimiento.direccion || "Sin dirección"}`
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Ciudad: ${establecimiento.ciudad || "-"} · Provincia: ${establecimiento.provincia || "-"}`
    }));

    if (!establecimiento.disponible && establecimiento.motivoNoDisponible) {
        body.appendChild(el("p", {
            className: "item-admin-texto",
            text: `Motivo: ${establecimiento.motivoNoDisponible}`
        }));
    }

    const acciones = el("div", { className: "item-admin-acciones" });

    const btnEditar = crearBoton("Editar", "btn btn-secondary", async () => {
        await abrirEditarEstablecimiento(refs, establecimiento);
    });

    const btnDisponibilidad = crearBoton(
        establecimiento.disponible ? "Bloquear" : "Reactivar",
        establecimiento.disponible ? "btn btn-danger" : "btn btn-primary",
        async () => {
            await cambiarDisponibilidadEstablecimiento(refs, state, establecimiento);
        }
    );

    acciones.append(btnEditar, btnDisponibilidad);
    article.append(avatar, body, acciones);

    return article;
}

function actualizarCampoMotivoEstablecimiento(refs) {
    const disponible = refs.crearEstablecimientoDisponible?.value === "true";

    if (!refs.crearEstablecimientoMotivo) return;

    refs.crearEstablecimientoMotivo.disabled = disponible;

    if (disponible) {
        refs.crearEstablecimientoMotivo.value = "";
    }
}

async function guardarNuevoEstablecimiento(refs, state) {
    const nombre = refs.crearEstablecimientoNombre.value.trim();
    const nombreTienda = refs.crearEstablecimientoTienda.value.trim();
    const direccion = refs.crearEstablecimientoDireccion.value.trim();
    const ciudad = refs.crearEstablecimientoCiudad.value.trim();
    const provincia = refs.crearEstablecimientoProvincia.value.trim();
    const disponible = refs.crearEstablecimientoDisponible.value === "true";
    const motivoNoDisponible = refs.crearEstablecimientoMotivo.value.trim();

    if (!nombre || !nombreTienda || !direccion || !ciudad || !provincia) {
        mostrarMensaje(refs, "Completa todos los campos obligatorios.", "error");
        return;
    }

    if (!disponible && !motivoNoDisponible) {
        mostrarMensaje(refs, "Indica el motivo si el establecimiento no está disponible.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarCrearEstablecimiento, "Creando...");

        const payload = {
            nombre,
            direccion,
            ciudad,
            provincia,
            nombreTienda,
            disponible,
            motivoNoDisponible: disponible ? null : motivoNoDisponible
        };

        const response = await fetch(`${BASE_URL}/establecimientos`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let mensaje = "No se pudo crear el establecimiento.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        cerrarModal(refs.modalCrearEstablecimiento, () => {
            refs.formCrearEstablecimiento.reset();
            actualizarCampoMotivoEstablecimiento(refs);
        });

        mostrarMensaje(refs, "Establecimiento creado correctamente.", "ok");
        await cargarEstablecimientos(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo crear el establecimiento.", "error");
    } finally {
        restaurarBoton(refs.guardarCrearEstablecimiento, "Crear establecimiento");
    }
}

async function abrirEditarEstablecimiento(refs, establecimiento) {
    refs.editarEstablecimientoId.value = establecimiento.id || "";
    refs.editarEstablecimientoNombre.value = establecimiento.nombre || "";
    refs.editarEstablecimientoTienda.value = establecimiento.tienda?.nombre || "";
    refs.editarEstablecimientoDireccion.value = establecimiento.direccion || "";
    refs.editarEstablecimientoCiudad.value = establecimiento.ciudad || "";
    refs.editarEstablecimientoProvincia.value = establecimiento.provincia || "";

    abrirModal(refs.modalEditarEstablecimiento);
}

async function guardarEdicionEstablecimiento(refs, state) {
    const id = refs.editarEstablecimientoId.value.trim();
    const nombre = refs.editarEstablecimientoNombre.value.trim();
    const nombreTienda = refs.editarEstablecimientoTienda.value.trim();
    const direccion = refs.editarEstablecimientoDireccion.value.trim();
    const ciudad = refs.editarEstablecimientoCiudad.value.trim();
    const provincia = refs.editarEstablecimientoProvincia.value.trim();

    if (!id || !nombre || !nombreTienda || !direccion || !ciudad || !provincia) {
        mostrarMensaje(refs, "Completa todos los campos del establecimiento.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarEditarEstablecimiento, "Guardando...");

        const payload = {
            nombre,
            direccion,
            ciudad,
            provincia,
            nombreTienda
        };

        const response = await fetch(`${BASE_URL}/establecimientos/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let mensaje = "No se pudo actualizar el establecimiento.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        cerrarModal(refs.modalEditarEstablecimiento, () => {
            refs.formEditarEstablecimiento.reset();
        });

        mostrarMensaje(refs, "Establecimiento actualizado correctamente.", "ok");
        await cargarEstablecimientos(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo actualizar el establecimiento.", "error");
    } finally {
        restaurarBoton(refs.guardarEditarEstablecimiento, "Guardar cambios");
    }
}

async function cambiarDisponibilidadEstablecimiento(refs, state, establecimiento) {
    try {
        let url = "";

        if (establecimiento.disponible) {
            const motivo = prompt("Indica el motivo de no disponibilidad:", "En obras");

            if (motivo === null) return;

            url = `${BASE_URL}/establecimientos/${establecimiento.id}/disponibilidad?disponible=false&motivoNoDisponible=${encodeURIComponent(motivo)}`;
        } else {
            url = `${BASE_URL}/establecimientos/${establecimiento.id}/disponibilidad?disponible=true`;
        }

        const response = await fetch(url, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cambiar la disponibilidad");
        }

        mostrarMensaje(
            refs,
            establecimiento.disponible
                ? "Establecimiento bloqueado correctamente."
                : "Establecimiento reactivado correctamente.",
            "ok"
        );

        await cargarEstablecimientos(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo actualizar la disponibilidad del establecimiento.", "error");
    }
}

/* =========================
   PUNTOS DE RECOGIDA
========================= */

function aplicarFiltroPuntosRecogida(refs, state) {
    const termino = (refs.buscadorPuntosRecogida?.value || "").trim().toLowerCase();

    if (!termino) {
        state.puntosRecogidaFiltrados = [...state.puntosRecogida];
    } else {
        state.puntosRecogidaFiltrados = state.puntosRecogida.filter((punto) => {
            const nombre = (punto.nombre || "").toLowerCase();
            const ciudad = (punto.ciudad || "").toLowerCase();
            const provincia = (punto.provincia || "").toLowerCase();
            const direccion = (punto.direccion || "").toLowerCase();

            return (
                nombre.includes(termino) ||
                ciudad.includes(termino) ||
                provincia.includes(termino) ||
                direccion.includes(termino)
            );
        });
    }

    renderizarPuntosRecogida(refs, state);
}

function renderizarPuntosRecogida(refs, state) {
    limpiarContenedor(refs.contenedorPuntosRecogida);

    if (!Array.isArray(state.puntosRecogidaFiltrados) || state.puntosRecogidaFiltrados.length === 0) {
        renderizarEstadoVacio(
            refs.contenedorPuntosRecogida,
            "Sin puntos de recogida",
            "No se encontraron puntos de recogida con los filtros actuales."
        );
        return;
    }

    state.puntosRecogidaFiltrados.forEach((punto) => {
        refs.contenedorPuntosRecogida.appendChild(
            crearCardPuntoRecogida(punto, refs, state)
        );
    });
}

function crearCardPuntoRecogida(punto, refs, state) {
    const article = el("article", { className: "item-admin-card" });

    const avatar = el("div", {
        className: "item-admin-avatar",
        text: "📍"
    });

    const body = el("div", { className: "item-admin-body" });
    body.appendChild(el("h3", { text: punto.nombre || "Sin nombre" }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadge(punto.disponible ? "Disponible" : "No disponible"));
    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Dirección: ${punto.direccion || "Sin dirección"}`
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Ciudad: ${punto.ciudad || "-"} · Provincia: ${punto.provincia || "-"}`
    }));

    if (!punto.disponible && punto.motivoNoDisponible) {
        body.appendChild(el("p", {
            className: "item-admin-texto",
            text: `Motivo: ${punto.motivoNoDisponible}`
        }));
    }

    const acciones = el("div", { className: "item-admin-acciones" });

    const btnEditar = crearBoton("Editar", "btn btn-secondary", async () => {
        await abrirEditarPuntoRecogida(refs, punto);
    });

    const btnDisponibilidad = crearBoton(
        punto.disponible ? "Bloquear" : "Reactivar",
        punto.disponible ? "btn btn-danger" : "btn btn-primary",
        async () => {
            await cambiarDisponibilidadPuntoRecogida(refs, state, punto);
        }
    );

    acciones.append(btnEditar, btnDisponibilidad);
    article.append(avatar, body, acciones);

    return article;
}

function actualizarCampoMotivoPuntoRecogida(refs) {
    const disponible = refs.crearPuntoRecogidaDisponible?.value === "true";

    if (!refs.crearPuntoRecogidaMotivo) return;

    refs.crearPuntoRecogidaMotivo.disabled = disponible;

    if (disponible) {
        refs.crearPuntoRecogidaMotivo.value = "";
    }
}

async function guardarNuevoPuntoRecogida(refs, state) {
    const nombre = refs.crearPuntoRecogidaNombre.value.trim();
    const direccion = refs.crearPuntoRecogidaDireccion.value.trim();
    const ciudad = refs.crearPuntoRecogidaCiudad.value.trim();
    const provincia = refs.crearPuntoRecogidaProvincia.value.trim();
    const disponible = refs.crearPuntoRecogidaDisponible.value === "true";
    const motivoNoDisponible = refs.crearPuntoRecogidaMotivo.value.trim();

    if (!nombre || !direccion || !ciudad || !provincia) {
        mostrarMensaje(refs, "Completa todos los campos obligatorios del punto de recogida.", "error");
        return;
    }

    if (!disponible && !motivoNoDisponible) {
        mostrarMensaje(refs, "Indica el motivo si el punto de recogida no está disponible.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarCrearPuntoRecogida, "Creando...");

        const payload = {
            nombre,
            direccion,
            ciudad,
            provincia,
            disponible,
            motivoNoDisponible: disponible ? null : motivoNoDisponible
        };

        const response = await fetch(`${BASE_URL}/puntos-recogida`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let mensaje = "No se pudo crear el punto de recogida.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        cerrarModal(refs.modalCrearPuntoRecogida, () => {
            refs.formCrearPuntoRecogida.reset();
            actualizarCampoMotivoPuntoRecogida(refs);
        });

        mostrarMensaje(refs, "Punto de recogida creado correctamente.", "ok");
        await cargarPuntosRecogida(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo crear el punto de recogida.", "error");
    } finally {
        restaurarBoton(refs.guardarCrearPuntoRecogida, "Crear punto");
    }
}

async function abrirEditarPuntoRecogida(refs, punto) {
    refs.editarPuntoRecogidaId.value = punto.id || "";
    refs.editarPuntoRecogidaNombre.value = punto.nombre || "";
    refs.editarPuntoRecogidaDireccion.value = punto.direccion || "";
    refs.editarPuntoRecogidaCiudad.value = punto.ciudad || "";
    refs.editarPuntoRecogidaProvincia.value = punto.provincia || "";

    abrirModal(refs.modalEditarPuntoRecogida);
}

async function guardarEdicionPuntoRecogida(refs, state) {
    const id = refs.editarPuntoRecogidaId.value.trim();
    const nombre = refs.editarPuntoRecogidaNombre.value.trim();
    const direccion = refs.editarPuntoRecogidaDireccion.value.trim();
    const ciudad = refs.editarPuntoRecogidaCiudad.value.trim();
    const provincia = refs.editarPuntoRecogidaProvincia.value.trim();

    if (!id || !nombre || !direccion || !ciudad || !provincia) {
        mostrarMensaje(refs, "Completa todos los campos del punto de recogida.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarEditarPuntoRecogida, "Guardando...");

        const payload = {
            nombre,
            direccion,
            ciudad,
            provincia
        };

        const response = await fetch(`${BASE_URL}/puntos-recogida/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            let mensaje = "No se pudo actualizar el punto de recogida.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        cerrarModal(refs.modalEditarPuntoRecogida, () => {
            refs.formEditarPuntoRecogida.reset();
        });

        mostrarMensaje(refs, "Punto de recogida actualizado correctamente.", "ok");
        await cargarPuntosRecogida(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo actualizar el punto de recogida.", "error");
    } finally {
        restaurarBoton(refs.guardarEditarPuntoRecogida, "Guardar cambios");
    }
}

async function cambiarDisponibilidadPuntoRecogida(refs, state, punto) {
    try {
        let url = "";

        if (punto.disponible) {
            const motivo = prompt("Indica el motivo de no disponibilidad:", "No operativo temporalmente");

            if (motivo === null) return;

            url = `${BASE_URL}/puntos-recogida/${punto.id}/disponibilidad?disponible=false&motivoNoDisponible=${encodeURIComponent(motivo)}`;
        } else {
            url = `${BASE_URL}/puntos-recogida/${punto.id}/disponibilidad?disponible=true`;
        }

        const response = await fetch(url, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cambiar la disponibilidad");
        }

        mostrarMensaje(
            refs,
            punto.disponible
                ? "Punto de recogida bloqueado correctamente."
                : "Punto de recogida reactivado correctamente.",
            "ok"
        );

        await cargarPuntosRecogida(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo actualizar la disponibilidad del punto de recogida.", "error");
    }
}

/* =========================
   USUARIOS
========================= */

function renderizarUsuarios(refs, state) {
    limpiarContenedor(refs.contenedorUsuarios);

    if (!Array.isArray(state.usuarios) || state.usuarios.length === 0) {
        renderizarEstadoVacio(refs.contenedorUsuarios, "Sin usuarios", "No se encontraron cuentas registradas.");
        return;
    }

    state.usuarios.forEach((usuario) => {
        refs.contenedorUsuarios.appendChild(crearCardUsuario(usuario, refs, state));
    });
}

function crearCardUsuario(usuario, refs, state) {
    const article = el("article", { className: "item-admin-card" });

    const avatar = el("div", {
        className: "item-admin-avatar",
        text: inicialNombre(usuario.nombre)
    });

    const body = el("div", { className: "item-admin-body" });
    body.appendChild(el("h3", { text: usuario.nombre || "Sin nombre" }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadge(usuario.rol === "ADMIN" ? "ADMIN" : "USUARIO"));
    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: usuario.email || "Sin email"
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `ID usuario: ${usuario.id}`
    }));

    const acciones = el("div", { className: "item-admin-acciones" });

    acciones.append(
        crearBoton("Ver detalle", "btn btn-secondary", async () => {
            await abrirDetalleUsuario(refs, state, usuario.id);
        }),
        crearBoton("Editar", "btn btn-secondary", async () => {
            await abrirEditarUsuario(refs, usuario.id);
        }),
        crearBoton("Eliminar", "btn btn-danger", () => {
            state.usuarioIdPendienteEliminar = usuario.id;
            abrirModal(refs.modalEliminarUsuario);
        })
    );

    article.append(avatar, body, acciones);
    return article;
}

async function abrirEditarUsuario(refs, usuarioId) {
    try {
        const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) throw new Error("No se pudo cargar el usuario");

        const usuario = await response.json();

        refs.editarUsuarioId.value = usuario.id || "";
        refs.editarUsuarioNombre.value = usuario.nombre || "";
        refs.editarUsuarioEmail.value = usuario.email || "";
        refs.editarUsuarioRol.value = usuario.rol || "USER";

        abrirModal(refs.modalEditarUsuario);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo cargar el usuario.", "error");
    }
}

async function guardarEdicionUsuario(refs, state) {
    const usuarioId = refs.editarUsuarioId.value.trim();
    const nombre = refs.editarUsuarioNombre.value.trim();
    const email = refs.editarUsuarioEmail.value.trim();
    const rol = refs.editarUsuarioRol.value;

    if (!usuarioId || !nombre || !email) {
        mostrarMensaje(refs, "Nombre y email son obligatorios.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarCambiosUsuario, "Guardando...");

        const responseActualizar = await fetch(`${BASE_URL}/usuarios/${usuarioId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ id: usuarioId, nombre, email, rol })
        });

        if (!responseActualizar.ok) throw new Error("No se pudo actualizar el usuario");

        cerrarModal(refs.modalEditarUsuario, () => refs.formEditarUsuario.reset());
        mostrarMensaje(refs, "Usuario actualizado correctamente.", "ok");

        await cargarUsuarios(refs, state);
        await cargarMetricas(refs);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo actualizar el usuario.", "error");
    } finally {
        restaurarBoton(refs.guardarCambiosUsuario, "Guardar cambios");
    }
}

async function eliminarUsuario(refs, state) {
    if (!state.usuarioIdPendienteEliminar) return;

    try {
        bloquearBoton(refs.confirmarEliminarUsuario, "Eliminando...");

        const response = await fetch(`${BASE_URL}/usuarios/${state.usuarioIdPendienteEliminar}`, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok) throw new Error("No se pudo eliminar el usuario");

        cerrarModal(refs.modalEliminarUsuario, () => {
            state.usuarioIdPendienteEliminar = null;
        });

        mostrarMensaje(refs, "Usuario eliminado correctamente.", "ok");

        await cargarUsuarios(refs, state);
        await cargarMetricas(refs);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo eliminar el usuario.", "error");
    } finally {
        restaurarBoton(refs.confirmarEliminarUsuario, "Eliminar");
    }
}

async function abrirDetalleUsuario(refs, state, usuarioId) {
    try {
        const usuario = state.usuarios.find((u) => u.id === usuarioId);

        const [resPedidos, resFavoritos] = await Promise.all([
            fetch(`${BASE_URL}/usuarios/${usuarioId}/pedidos`, { method: "GET", credentials: "include" }),
            fetch(`${BASE_URL}/usuarios/${usuarioId}/favoritos`, { method: "GET", credentials: "include" })
        ]);

        if (!resPedidos.ok || !resFavoritos.ok) {
            throw new Error("No se pudo cargar el detalle del usuario");
        }

        const pedidos = await resPedidos.json();
        const favoritos = await resFavoritos.json();

        limpiarContenedor(refs.contenidoDetalleUsuario);

        const top = el("div", { className: "detalle-grid-top" });

        const cardUsuario = crearDetalleCard("Datos del usuario", [
            ["Nombre", usuario?.nombre || "Sin nombre"],
            ["Email", usuario?.email || "Sin email"],
            ["Rol", usuario?.rol || "USER"],
            ["ID", usuario?.id ?? "-"]
        ]);

        const cardResumen = crearDetalleCard("Resumen", [
            ["Pedidos", Array.isArray(pedidos) ? pedidos.length : 0],
            ["Favoritos", Array.isArray(favoritos) ? favoritos.length : 0]
        ]);

        top.append(cardUsuario, cardResumen);
        refs.contenidoDetalleUsuario.appendChild(top);

        refs.contenidoDetalleUsuario.appendChild(
            crearBloqueListaDetalle(
                "Compras del usuario",
                pedidos,
                (pedido) => {
                    const item = crearDetalleItem(`Pedido #${pedido.id}`);
                    item.append(
                        crearParrafoDetalle(`Fecha: ${formatearFecha(pedido.fechaPedido)}`),
                        crearParrafoDetalle(`Total: ${formatearPrecio(pedido.total)}`),
                        crearParrafoDetalle(`Método de pago: ${pedido.metodoPago || "Sin método"}`),
                        crearParrafoDetalle(`Estado: ${pedido.estado || "Sin estado"}`)
                    );
                    return item;
                },
                "Este usuario no tiene pedidos."
            )
        );

        refs.contenidoDetalleUsuario.appendChild(
            crearBloqueListaDetalle(
                "Favoritos del usuario",
                favoritos,
                (favorito) => {
                    const item = crearDetalleItem(favorito.producto?.nombre || "Producto sin nombre");
                    item.append(
                        crearParrafoDetalle(`Tienda: ${favorito.producto?.tienda?.nombre || "Sin tienda"}`),
                        crearParrafoDetalle(`Categoría: ${favorito.producto?.categoria?.nombre || "Sin categoría"}`),
                        crearParrafoDetalle(`Precio: ${formatearPrecio(favorito.producto?.precio)}`),
                        crearParrafoDetalle(`Fecha agregado: ${formatearFecha(favorito.fechaAgregado)}`)
                    );
                    return item;
                },
                "Este usuario no tiene favoritos."
            )
        );

        abrirModal(refs.modalDetalleUsuario);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo cargar el detalle del usuario.", "error");
    }
}

/* =========================
   PEDIDOS
========================= */

function renderizarFiltroEstadosPedido(refs, state) {
    if (!refs.filtroEstadoPedidos) return;

    const valorActual = refs.filtroEstadoPedidos.value || "TODOS";
    refs.filtroEstadoPedidos.innerHTML = "";

    const optionTodos = document.createElement("option");
    optionTodos.value = "TODOS";
    optionTodos.textContent = "Todos los estados";
    refs.filtroEstadoPedidos.appendChild(optionTodos);

    if (Array.isArray(state.estadosPedidoDisponibles)) {
        state.estadosPedidoDisponibles.forEach((estado) => {
            const option = document.createElement("option");
            option.value = estado;
            option.textContent = formatearEstadoPedidoTexto(estado);
            refs.filtroEstadoPedidos.appendChild(option);
        });
    }

    const existeValor = Array.from(refs.filtroEstadoPedidos.options).some(opt => opt.value === valorActual);
    refs.filtroEstadoPedidos.value = existeValor ? valorActual : "TODOS";
}

function renderizarFiltroEstadosPedidoFallback(refs) {
    if (!refs.filtroEstadoPedidos) return;

    refs.filtroEstadoPedidos.innerHTML = `
        <option value="TODOS">Todos los estados</option>
        <option value="PENDIENTE">Pendiente</option>
        <option value="CONFIRMADO">Confirmado</option>
        <option value="PREPARANDO">Preparando</option>
        <option value="ENVIADO">Enviado</option>
        <option value="LISTO_PARA_RECOGER">Listo para recoger</option>
        <option value="PENDIENTE_CONFIRMACION_ENTREGA">Pendiente confirmación entrega</option>
        <option value="ENTREGADO">Entregado</option>
        <option value="CANCELADO">Cancelado</option>
    `;
}

function renderizarPedidos(refs, state) {
    limpiarContenedor(refs.contenedorPedidos);

    if (!Array.isArray(state.pedidos) || state.pedidos.length === 0) {
        renderizarEstadoVacio(refs.contenedorPedidos, "Sin pedidos", "No se encontraron pedidos con el filtro actual.");
        return;
    }

    state.pedidos.forEach((pedido) => {
        refs.contenedorPedidos.appendChild(crearCardPedido(pedido, refs, state));
    });
}

function crearCardPedido(pedido, refs, state) {
    const article = el("article", { className: "item-admin-card" });

    const avatar = el("div", { className: "item-admin-avatar", text: "#" });

    const body = el("div", { className: "item-admin-body" });
    body.appendChild(el("h3", { text: `Pedido #${pedido.id}` }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadgeEstado(pedido.estado || "CONFIRMADO"));
    meta.appendChild(crearBadge(pedido.metodoPago || "Sin método"));
    meta.appendChild(crearBadge(formatearMetodoEntregaTexto(pedido.metodoEntrega)));
    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-precio",
        text: formatearPrecio(pedido.total)
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Usuario: ${pedido.usuario?.nombre || "Sin nombre"} · ${pedido.usuario?.email || "Sin email"}`
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Fecha: ${formatearFecha(pedido.fechaPedido)}`
    }));

    const acciones = el("div", { className: "item-admin-acciones" });

    acciones.append(
        crearBoton("Ver detalle", "btn btn-secondary", async () => {
            await abrirDetallePedido(refs, state, pedido.id);
        }),
        crearBoton("Cambiar estado", "btn btn-primary", async () => {
            await abrirModalCambioEstadoPedido(refs, state, pedido);
        })
    );

    article.append(avatar, body, acciones);
    return article;
}

async function abrirDetallePedido(refs, state, pedidoId) {
    try {
        const pedido = state.pedidos.find((p) => p.id === pedidoId);

        const response = await fetch(`${BASE_URL}/pedidos/admin/${pedidoId}/items`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) throw new Error("No se pudieron cargar los items");

        const items = await response.json();

        limpiarContenedor(refs.contenidoDetallePedido);

        const top = el("div", { className: "detalle-grid-top" });

        top.append(
            crearDetalleCard("Datos del pedido", [
                ["ID", pedido?.id ?? "-"],
                ["Fecha", formatearFecha(pedido?.fechaPedido)],
                ["Total", formatearPrecio(pedido?.total)],
                ["Método de pago", pedido?.metodoPago || "Sin método"],
                ["Método de entrega", formatearMetodoEntregaTexto(pedido?.metodoEntrega)],
                ["Estado", formatearEstadoPedidoTexto(pedido?.estado || "Sin estado")]
            ]),
            crearDetalleCard("Cliente", [
                ["Nombre", pedido?.usuario?.nombre || "Sin nombre"],
                ["Email", pedido?.usuario?.email || "Sin email"],
                ["ID usuario", pedido?.usuario?.id ?? "-"]
            ])
        );

        refs.contenidoDetallePedido.appendChild(top);

        refs.contenidoDetallePedido.appendChild(
            crearBloqueListaDetalle(
                "Productos del pedido",
                items,
                (item) => {
                    const subtotal = (Number(item.precioUnitario) || 0) * (Number(item.cantidad) || 0);
                    const bloque = crearDetalleItem(item.producto?.nombre || "Producto sin nombre");
                    bloque.append(
                        crearParrafoDetalle(`Talla: ${item.talla ? window.TallasProducto.formatearTalla(item.talla) : "-"}`),
                        crearParrafoDetalle(`Cantidad: ${item.cantidad ?? 0}`),
                        crearParrafoDetalle(`Precio unitario: ${formatearPrecio(item.precioUnitario)}`),
                        crearParrafoDetalle(`Subtotal: ${formatearPrecio(subtotal)}`)
                    );
                    return bloque;
                },
                "Este pedido no tiene items."
            )
        );

        abrirModal(refs.modalDetallePedido);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo cargar el detalle del pedido.", "error");
    }
}

async function abrirModalCambioEstadoPedido(refs, state, pedido) {
    try {
        state.pedidoCambioEstado = pedido;
        refs.cambiarEstadoPedidoId.value = pedido.id;

        refs.nuevoEstadoPedido.innerHTML = `<option value="">Cargando estados disponibles...</option>`;
        refs.nuevoEstadoPedido.disabled = true;
        refs.guardarCambioEstadoPedido.disabled = true;

        if (refs.textoAyudaEstadoPedido) {
            refs.textoAyudaEstadoPedido.textContent =
                `Estado actual: ${formatearEstadoPedidoTexto(pedido.estado)} · Método de entrega: ${formatearMetodoEntregaTexto(pedido.metodoEntrega)}`;
        }

        abrirModal(refs.modalCambiarEstadoPedido);

        const response = await fetch(`${BASE_URL}/pedidos/${pedido.id}/estados-validos`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los estados válidos");
        }

        const estadosValidos = await response.json();
        refs.nuevoEstadoPedido.innerHTML = "";

        if (!Array.isArray(estadosValidos) || estadosValidos.length === 0) {
            const option = document.createElement("option");
            option.value = "";
            option.textContent = "No hay cambios manuales disponibles";
            refs.nuevoEstadoPedido.appendChild(option);
            refs.nuevoEstadoPedido.disabled = true;
            refs.guardarCambioEstadoPedido.disabled = true;

            if (refs.textoAyudaEstadoPedido) {
                refs.textoAyudaEstadoPedido.textContent =
                    `Estado actual: ${formatearEstadoPedidoTexto(pedido.estado)}. Este pedido ya no admite cambios manuales desde el panel.`;
            }

            return;
        }

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Selecciona un nuevo estado";
        refs.nuevoEstadoPedido.appendChild(placeholder);

        estadosValidos.forEach((estado) => {
            const option = document.createElement("option");
            option.value = estado;
            option.textContent = formatearEstadoPedidoTexto(estado);
            refs.nuevoEstadoPedido.appendChild(option);
        });

        refs.nuevoEstadoPedido.disabled = false;
        refs.guardarCambioEstadoPedido.disabled = false;
        refs.nuevoEstadoPedido.value = "";

    } catch (error) {
        console.error(error);
        refs.nuevoEstadoPedido.innerHTML = `<option value="">Error al cargar estados</option>`;
        refs.nuevoEstadoPedido.disabled = true;
        refs.guardarCambioEstadoPedido.disabled = true;

        if (refs.textoAyudaEstadoPedido) {
            refs.textoAyudaEstadoPedido.textContent =
                "No se pudieron cargar los estados válidos para este pedido.";
        }

        mostrarMensaje(refs, "No se pudieron cargar los estados válidos del pedido.", "error");
    }
}

function limpiarOpcionesEstadosPedido(refs) {
    if (!refs.nuevoEstadoPedido) return;

    refs.nuevoEstadoPedido.innerHTML = `<option value="">Cargando estados disponibles...</option>`;
    refs.nuevoEstadoPedido.disabled = true;
    refs.guardarCambioEstadoPedido.disabled = false;

    if (refs.textoAyudaEstadoPedido) {
        refs.textoAyudaEstadoPedido.textContent =
            "Solo se mostrarán los estados válidos para el flujo real de este pedido.";
    }
}

async function guardarCambioEstadoPedido(refs, state) {
    const pedidoId = refs.cambiarEstadoPedidoId.value;
    const nuevoEstado = refs.nuevoEstadoPedido.value;

    if (!pedidoId || !nuevoEstado) {
        mostrarMensaje(refs, "Selecciona un estado válido.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarCambioEstadoPedido, "Guardando...");

        const response = await fetch(`${BASE_URL}/pedidos/${pedidoId}/estado/${encodeURIComponent(nuevoEstado)}`, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            let mensaje = "No se pudo cambiar el estado del pedido.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        cerrarModal(refs.modalCambiarEstadoPedido, () => {
            refs.formCambiarEstadoPedido.reset();
            state.pedidoCambioEstado = null;
            limpiarOpcionesEstadosPedido(refs);
        });

        mostrarMensaje(refs, "Estado del pedido actualizado correctamente.", "ok");

        await cargarPedidos(refs, state);
        await cargarMetricas(refs);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo cambiar el estado del pedido.", "error");
    } finally {
        restaurarBoton(refs.guardarCambioEstadoPedido, "Guardar cambio");
    }
}

/* =========================
   INCIDENCIAS
========================= */

function renderizarIncidencias(refs, state) {
    limpiarContenedor(refs.contenedorIncidencias);

    if (!Array.isArray(state.incidencias) || state.incidencias.length === 0) {
        renderizarEstadoVacio(
            refs.contenedorIncidencias,
            "Sin incidencias",
            "No se encontraron incidencias con el filtro actual."
        );
        return;
    }

    state.incidencias.forEach((incidencia) => {
        refs.contenedorIncidencias.appendChild(crearCardIncidencia(incidencia, refs, state));
    });
}

function crearCardIncidencia(incidencia, refs, state) {
    const article = el("article", { className: "item-admin-card" });

    const avatar = el("div", {
        className: "item-admin-avatar",
        text: "🎫"
    });

    const body = el("div", { className: "item-admin-body" });

    body.appendChild(el("h3", {
        text: `${incidencia.codigoSeguimiento || "INC"} · ${incidencia.asunto || "Sin asunto"}`
    }));

    const meta = el("div", { className: "item-admin-meta" });
    meta.appendChild(crearBadgeEstadoIncidencia(incidencia.estadoIncidencia));
    meta.appendChild(crearBadge(formatearTipoIncidenciaTexto(incidencia.tipoIncidencia)));
    body.appendChild(meta);

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Contacto: ${incidencia.nombreContacto || "Sin nombre"} · ${incidencia.emailContacto || "Sin email"}`
    }));

    body.appendChild(el("p", {
        className: "item-admin-texto",
        text: `Fecha: ${formatearFecha(incidencia.fechaCreacion)}`
    }));

    const acciones = el("div", { className: "item-admin-acciones" });

    acciones.append(
        crearBoton("Ver / responder", "btn btn-primary", async () => {
            await abrirDetalleIncidencia(refs, state, incidencia.id);
        }),
        crearBoton("En revisión", "btn btn-secondary", async () => {
            await cambiarEstadoIncidencia(refs, state, incidencia.id, "EN_REVISION");
        }),
        crearBoton("Cerrar", "btn btn-danger", async () => {
            await cambiarEstadoIncidencia(refs, state, incidencia.id, "CERRADA");
        })
    );

    article.append(avatar, body, acciones);
    return article;
}

async function cambiarEstadoIncidencia(refs, state, incidenciaId, nuevoEstado) {
    if (!incidenciaId || !nuevoEstado) {
        mostrarMensaje(refs, "No se pudo identificar la incidencia o el estado.", "error");
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/admin/incidencias/${incidenciaId}/estado/${encodeURIComponent(nuevoEstado)}`, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            let mensaje = "No se pudo cambiar el estado de la incidencia.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        mostrarMensaje(refs, "Estado de incidencia actualizado correctamente.", "ok");
        await cargarIncidencias(refs, state);
    } catch (error) {
        console.error("Error cambiando estado de incidencia:", error);
        mostrarMensaje(refs, error.message || "No se pudo cambiar el estado de la incidencia.", "error");
    }
}

async function abrirDetalleIncidencia(refs, state, incidenciaId) {
    detenerRefrescoDetalleIncidencia(state);

    state.incidenciaDetalleAbiertaId = incidenciaId;

    await cargarDetalleIncidencia(refs, state, incidenciaId, true);

    abrirModal(refs.modalDetalleIncidencia);

    iniciarRefrescoDetalleIncidencia(refs, state, incidenciaId);
}

async function cargarDetalleIncidencia(refs, state, incidenciaId, esPrimeraCarga = false) {
    if (state.cargandoDetalleIncidencia) {
        return;
    }

    try {
        state.cargandoDetalleIncidencia = true;

        if (esPrimeraCarga) {
            mostrarMensaje(refs, "Cargando detalle de incidencia...", "info");
        }

        const [resIncidencia, resMensajes] = await Promise.all([
            fetch(`${BASE_URL}/admin/incidencias/${incidenciaId}`, {
                method: "GET",
                credentials: "include"
            }),
            fetch(`${BASE_URL}/admin/incidencias/${incidenciaId}/mensajes`, {
                method: "GET",
                credentials: "include"
            })
        ]);

        if (!resIncidencia.ok) {
            let mensaje = "No se pudo cargar el detalle de la incidencia.";
            try {
                const texto = await resIncidencia.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        if (!resMensajes.ok) {
            let mensaje = "No se pudo cargar la conversación de la incidencia.";
            try {
                const texto = await resMensajes.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        const incidencia = await resIncidencia.json();
        const mensajes = await resMensajes.json();

        incidencia.mensajes = Array.isArray(mensajes) ? mensajes : [];

        const nuevoSnapshot = crearSnapshotMensajesIncidencia(incidencia);

        if (!esPrimeraCarga && nuevoSnapshot === state.snapshotDetalleIncidencia) {
            return;
        }

        state.snapshotDetalleIncidencia = nuevoSnapshot;

        const modalContenido = refs.modalDetalleIncidencia?.querySelector(".modal-admin");
        const scrollAnterior = modalContenido ? modalContenido.scrollTop : 0;

        renderizarDetalleIncidencia(refs, incidencia);

        if (modalContenido && !esPrimeraCarga) {
            modalContenido.scrollTop = scrollAnterior;
        }

        if (refs.responderIncidenciaId) {
            refs.responderIncidenciaId.value = incidencia.id;
        }

        if (esPrimeraCarga && refs.textoRespuestaIncidencia) {
            refs.textoRespuestaIncidencia.value = "";
        }

        const estaCerrada = incidencia.estadoIncidencia === "CERRADA";

        if (refs.textoRespuestaIncidencia) {
            refs.textoRespuestaIncidencia.disabled = estaCerrada;
            refs.textoRespuestaIncidencia.placeholder = estaCerrada
                ? "Esta incidencia está cerrada y no admite nuevas respuestas."
                : "Escribe aquí la respuesta que se enviará por correo al usuario...";
        }

        if (refs.btnEnviarRespuestaIncidencia) {
            refs.btnEnviarRespuestaIncidencia.disabled = estaCerrada;
        }

    } catch (error) {
        console.error("Error cargando detalle de incidencia:", error);

        if (esPrimeraCarga) {
            mostrarMensaje(refs, error.message || "No se pudo cargar el detalle de la incidencia.", "error");
        }
    } finally {
        state.cargandoDetalleIncidencia = false;
    }
}

function iniciarRefrescoDetalleIncidencia(refs, state, incidenciaId) {
    detenerRefrescoDetalleIncidencia(state);

    state.intervaloDetalleIncidencia = setInterval(async () => {
        const modalAbierto = refs.modalDetalleIncidencia?.style.display === "flex";

        if (!modalAbierto || state.incidenciaDetalleAbiertaId !== incidenciaId) {
            detenerRefrescoDetalleIncidencia(state);
            return;
        }

        await cargarDetalleIncidencia(refs, state, incidenciaId, false);
    }, 10000);
}

function detenerRefrescoDetalleIncidencia(state) {
    if (state.intervaloDetalleIncidencia) {
        clearInterval(state.intervaloDetalleIncidencia);
        state.intervaloDetalleIncidencia = null;
    }
}

function renderizarDetalleIncidencia(refs, incidencia) {
    limpiarContenedor(refs.contenidoDetalleIncidencia);

    const mensajes = obtenerMensajesIncidencia(incidencia);
    const primerMensajeUsuario = mensajes.find((mensaje) => mensaje.remitente === "USUARIO");

    const top = el("div", { className: "detalle-grid-top" });

    top.append(
        crearDetalleCard("Datos de la incidencia", [
            ["Código", incidencia.codigoSeguimiento || "-"],
            ["Asunto", incidencia.asunto || "-"],
            ["Tipo", formatearTipoIncidenciaTexto(incidencia.tipoIncidencia)],
            ["Estado", formatearEstadoIncidenciaTexto(incidencia.estadoIncidencia)],
            ["Fecha", formatearFecha(incidencia.fechaCreacion)]
        ]),
        crearDetalleCard("Contacto", [
            ["Nombre", incidencia.nombreContacto || "-"],
            ["Email", incidencia.emailContacto || "-"],
            ["Usuario", incidencia.usuarioRelacionado || incidencia.usuario?.nombre || "No vinculado"],
            ["ID usuario", incidencia.usuario?.id ?? "-"]
        ])
    );

    refs.contenidoDetalleIncidencia.appendChild(top);

    const descripcionCard = el("div", { className: "detalle-card" });
    descripcionCard.appendChild(el("h4", { text: "Mensaje inicial del usuario" }));
    descripcionCard.appendChild(el("p", {
        text: primerMensajeUsuario?.contenido || "Sin descripción."
    }));

    refs.contenidoDetalleIncidencia.appendChild(descripcionCard);

    const conversacionCard = el("div", { className: "detalle-card incidencia-conversacion-card" });
    conversacionCard.appendChild(el("h4", { text: "Conversación" }));

    const conversacionLista = el("div", { className: "incidencia-conversacion-lista" });

    if (mensajes.length === 0) {
        conversacionLista.appendChild(el("div", {
            className: "detalle-vacio",
            text: "Todavía no hay mensajes en esta incidencia."
        }));
    } else {
        mensajes.forEach((mensaje) => {
            conversacionLista.appendChild(crearMensajeConversacionIncidencia(mensaje));
        });
    }

    conversacionCard.appendChild(conversacionLista);
    refs.contenidoDetalleIncidencia.appendChild(conversacionCard);
}

function obtenerMensajesIncidencia(incidencia) {
    if (Array.isArray(incidencia.mensajes)) return incidencia.mensajes;
    if (Array.isArray(incidencia.respuestas)) return incidencia.respuestas;
    if (Array.isArray(incidencia.conversacion)) return incidencia.conversacion;
    return [];
}

function crearMensajeConversacionIncidencia(mensaje) {
    const remitente = mensaje.remitente || mensaje.tipoRemitente || "USUARIO";
    const esAdmin = remitente === "ADMIN";

    const item = el("div", {
        className: `incidencia-mensaje ${esAdmin ? "incidencia-mensaje-admin" : "incidencia-mensaje-usuario"}`
    });

    const cabecera = el("div", { className: "incidencia-mensaje-header" });

    cabecera.appendChild(el("strong", {
        text: esAdmin ? "Administrador" : "Usuario"
    }));

    cabecera.appendChild(el("span", {
        text: formatearFecha(
            mensaje.fechaMensaje ||
            mensaje.fechaCreacion ||
            mensaje.fechaEnvio ||
            mensaje.fechaRespuesta
        )
    }));

    item.appendChild(cabecera);

    item.appendChild(el("p", {
        text: mensaje.contenido || mensaje.mensaje || mensaje.texto || ""
    }));

    return item;
}

async function enviarRespuestaIncidencia(refs, state) {
    const incidenciaId = refs.responderIncidenciaId?.value;
    const mensaje = refs.textoRespuestaIncidencia?.value.trim();

    if (!incidenciaId) {
        mostrarMensaje(refs, "No se pudo identificar la incidencia.", "error");
        return;
    }

    if (!mensaje) {
        mostrarMensaje(refs, "Escribe una respuesta antes de enviarla.", "error");
        return;
    }

    try {
        bloquearBoton(refs.btnEnviarRespuestaIncidencia, "Enviando...");

        const response = await fetch(`${BASE_URL}/admin/incidencias/${incidenciaId}/responder`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({
                mensaje: mensaje
            })
        });

        if (!response.ok) {
            let textoError = "No se pudo enviar la respuesta.";
            try {
                const texto = await response.text();
                if (texto) textoError = texto;
            } catch (_) {}
            throw new Error(textoError);
        }

        mostrarMensaje(refs, "Respuesta enviada correctamente al usuario.", "ok");

        refs.textoRespuestaIncidencia.value = "";

        await abrirDetalleIncidencia(refs, state, incidenciaId);
        await cargarIncidencias(refs, state);
    } catch (error) {
        console.error("Error enviando respuesta de incidencia:", error);
        mostrarMensaje(refs, error.message || "No se pudo enviar la respuesta.", "error");
    } finally {
        restaurarBoton(refs.btnEnviarRespuestaIncidencia, "Enviar respuesta");
    }
}

function crearBadgeEstadoIncidencia(estado) {
    return el("span", {
        className: `item-admin-badge item-admin-badge-estado ${obtenerClaseEstadoIncidencia(estado)}`,
        text: formatearEstadoIncidenciaTexto(estado)
    });
}

function obtenerClaseEstadoIncidencia(estado) {
    switch (estado) {
        case "PENDIENTE": return "estado-pendiente";
        case "EN_REVISION": return "estado-preparando";
        case "ESPERANDO_RESPUESTA_USUARIO": return "estado-enviado";
        case "RESPONDIDA_POR_USUARIO": return "estado-confirmado";
        case "RESUELTA": return "estado-entregado";
        case "CERRADA": return "estado-cancelado";
        default: return "estado-confirmado";
    }
}

function formatearEstadoIncidenciaTexto(estado) {
    switch (estado) {
        case "PENDIENTE": return "Pendiente";
        case "EN_REVISION": return "En revisión";
        case "ESPERANDO_RESPUESTA_USUARIO": return "Esperando respuesta";
        case "RESPONDIDA_POR_USUARIO": return "Respondida por usuario";
        case "RESUELTA": return "Resuelta";
        case "CERRADA": return "Cerrada";
        default: return estado || "Sin estado";
    }
}

function formatearTipoIncidenciaTexto(tipo) {
    switch (tipo) {
        case "PROBLEMA_ACCESO": return "Problema de acceso";
        case "NO_RECUERDO_DATOS": return "No recuerda datos";
        case "SIN_ACCESO_EMAIL": return "Sin acceso al email";
        case "PROBLEMA_PEDIDO": return "Problema con pedido";
        case "PROBLEMA_PAGO": return "Problema con pago";
        case "PRODUCTO_DEFECTUOSO": return "Producto dañado o incorrecto";
        case "ERROR_WEB": return "Error web";
        case "OTRO": return "Otro";
        default: return tipo || "Sin tipo";
    }
}

/* =========================
   CONFIRMACIÓN ENTREGA QR
========================= */

async function iniciarEscanerEntrega(refs, state) {
    if (!refs.qrReaderEntrega) {
        mostrarMensaje(refs, "No se encontró el contenedor del escáner.", "error");
        return;
    }

    if (state.escanerEntregaActivo) {
        return;
    }

    if (typeof Html5Qrcode === "undefined") {
        mostrarResultadoQrEntrega(refs, "error", "Lector QR no disponible", "No se pudo cargar la librería del escáner QR.");
        mostrarMensaje(refs, "No se pudo cargar el lector QR.", "error");
        return;
    }

    try {
        refs.btnIniciarEscanerEntrega.disabled = true;
        refs.btnIniciarEscanerEntrega.textContent = "Iniciando...";
        refs.btnDetenerEscanerEntrega.disabled = true;

        if (!state.qrScannerEntrega) {
            state.qrScannerEntrega = new Html5Qrcode("qr-reader-entrega");
        }

        await state.qrScannerEntrega.start(
            { facingMode: "environment" },
            {
                fps: 10,
                qrbox: { width: 250, height: 250 }
            },
            async (decodedText) => {
                const token = extraerTokenEntrega(decodedText);

                if (!token || state.confirmandoEntrega || token === state.ultimoTokenEntregaLeido) {
                    return;
                }

                state.ultimoTokenEntregaLeido = token;

                if (refs.inputTokenEntrega) {
                    refs.inputTokenEntrega.value = token;
                }

                await detenerEscanerEntrega(refs, state, false);
                await confirmarEntregaPorToken(refs, state, token);
            },
            () => {}
        );

        state.escanerEntregaActivo = true;
        refs.btnIniciarEscanerEntrega.disabled = true;
        refs.btnIniciarEscanerEntrega.textContent = "Escáner activo";
        refs.btnDetenerEscanerEntrega.disabled = false;

        mostrarResultadoQrEntrega(refs, "info", "Escáner activo", "Enfoca el código QR del pedido para validar la entrega.");
    } catch (error) {
        console.error("Error al iniciar escáner:", error);
        state.escanerEntregaActivo = false;
        restaurarBotonEscanerEntrega(refs);
        mostrarResultadoQrEntrega(refs, "error", "No se pudo iniciar la cámara", "Revisa los permisos del navegador o prueba con la confirmación manual.");
        mostrarMensaje(refs, "No se pudo iniciar el escáner QR.", "error");
    }
}

async function detenerEscanerEntrega(refs, state, mostrarResultado = true) {
    try {
        if (state.qrScannerEntrega && state.escanerEntregaActivo) {
            await state.qrScannerEntrega.stop();
            await state.qrScannerEntrega.clear();
        }
    } catch (error) {
        console.error("Error al detener escáner:", error);
    } finally {
        state.escanerEntregaActivo = false;
        restaurarBotonEscanerEntrega(refs);

        if (mostrarResultado) {
            mostrarResultadoQrEntrega(refs, "info", "Escáner detenido", "Puedes iniciar de nuevo el escáner o confirmar un token manualmente.");
        }
    }
}

function restaurarBotonEscanerEntrega(refs) {
    if (refs.btnIniciarEscanerEntrega) {
        refs.btnIniciarEscanerEntrega.disabled = false;
        refs.btnIniciarEscanerEntrega.textContent = "Iniciar escáner";
    }

    if (refs.btnDetenerEscanerEntrega) {
        refs.btnDetenerEscanerEntrega.disabled = true;
        refs.btnDetenerEscanerEntrega.textContent = "Detener escáner";
    }
}

async function confirmarEntregaManual(refs, state) {
    const token = extraerTokenEntrega(refs.inputTokenEntrega?.value || "");
    await confirmarEntregaPorToken(refs, state, token);
}

async function confirmarEntregaPorToken(refs, state, token) {
    const tokenLimpio = extraerTokenEntrega(token);

    if (!tokenLimpio) {
        mostrarResultadoQrEntrega(refs, "error", "Token vacío", "Introduce o escanea un token válido para confirmar la entrega.");
        mostrarMensaje(refs, "Introduce un token válido.", "error");
        return;
    }

    if (state.confirmandoEntrega) {
        return;
    }

    try {
        state.confirmandoEntrega = true;
        bloquearBoton(refs.btnConfirmarTokenEntrega, "Confirmando...");
        mostrarResultadoQrEntrega(refs, "info", "Validando QR", "Comprobando el pedido asociado al código escaneado.");

        const response = await fetch(`${BASE_URL}/pedidos/admin/confirmar-entrega?token=${encodeURIComponent(tokenLimpio)}`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            let mensaje = "No se pudo confirmar la entrega.";
            try {
                const texto = await response.text();
                if (texto) mensaje = texto;
            } catch (_) {}
            throw new Error(mensaje);
        }

        const pedido = await response.json();

        if (refs.inputTokenEntrega) {
            refs.inputTokenEntrega.value = "";
        }

        state.ultimoTokenEntregaLeido = null;

        mostrarResultadoQrEntrega(
            refs,
            "ok",
            "Entrega confirmada",
            `El pedido #${pedido.id} se ha marcado como entregado correctamente.`,
            pedido
        );

        mostrarMensaje(refs, `Pedido #${pedido.id} entregado correctamente.`, "ok");

        await cargarPedidos(refs, state);
        await cargarMetricas(refs);
    } catch (error) {
        console.error("Error al confirmar entrega:", error);
        state.ultimoTokenEntregaLeido = null;
        mostrarResultadoQrEntrega(refs, "error", "No se pudo confirmar", error.message || "El código QR no es válido o el pedido no puede confirmarse.");
        mostrarMensaje(refs, error.message || "No se pudo confirmar la entrega.", "error");
    } finally {
        state.confirmandoEntrega = false;
        restaurarBoton(refs.btnConfirmarTokenEntrega, "Confirmar entrega");
    }
}

function extraerTokenEntrega(valor) {
    const texto = (valor || "").trim();

    if (!texto) {
        return "";
    }

    try {
        const url = new URL(texto);
        const token = url.searchParams.get("token");

        if (token && token.trim()) {
            return token.trim();
        }
    } catch (_) {}

    const match = texto.match(/[?&]token=([^&]+)/);

    if (match && match[1]) {
        return decodeURIComponent(match[1]).trim();
    }

    return texto;
}

function mostrarResultadoQrEntrega(refs, tipo, titulo, texto, pedido) {
    if (!refs.resultadoQrEntrega) return;

    refs.resultadoQrEntrega.className = "resultado-qr-entrega";
    refs.resultadoQrEntrega.classList.add(`resultado-qr-entrega-${tipo}`);

    limpiarContenedor(refs.resultadoQrEntrega);

    refs.resultadoQrEntrega.appendChild(el("h4", { text: titulo }));
    refs.resultadoQrEntrega.appendChild(el("p", { text: texto }));

    if (pedido) {
        const resumen = el("div", { className: "resultado-qr-resumen" });

        resumen.appendChild(crearParrafoDetalle(`Pedido: #${pedido.id}`));
        resumen.appendChild(crearParrafoDetalle(`Estado: ${formatearEstadoPedidoTexto(pedido.estado)}`));
        resumen.appendChild(crearParrafoDetalle(`Cliente: ${pedido.usuario?.nombre || "Sin nombre"}`));
        resumen.appendChild(crearParrafoDetalle(`Total: ${formatearPrecio(pedido.total)}`));

        refs.resultadoQrEntrega.appendChild(resumen);
    }
}

/* =========================
   HELPERS PRODUCTOS PAGINADOS
========================= */

function normalizarPaginaProductos(data, state) {
    if (Array.isArray(data)) {
        return {
            productos: data,
            totalElementos: data.length,
            paginaActual: 0,
            ultimaPagina: true
        };
    }

    const productos = Array.isArray(data.productos)
        ? data.productos
        : Array.isArray(data.content)
            ? data.content
            : Array.isArray(data.items)
                ? data.items
                : [];

    const totalElementos = obtenerTotalElementos(data, productos.length);

    const paginaActual =
        data.paginaActual ??
        data.number ??
        data.page ??
        state.paginaProductos;

    const ultimaPagina =
        data.ultimaPagina ??
        data.last ??
        data.esUltimaPagina ??
        productos.length < state.sizeProductosAdmin;

    return {
        productos,
        totalElementos,
        paginaActual,
        ultimaPagina
    };
}

function obtenerTotalElementos(data, fallback = 0) {
    if (!data || Array.isArray(data)) {
        return Array.isArray(data) ? data.length : fallback;
    }

    return data.totalElementos ??
        data.totalElements ??
        data.total ??
        data.totalProductos ??
        fallback;
}

function unirProductosSinDuplicados(actuales, nuevos) {
    const mapa = new Map();

    actuales.forEach((producto) => {
        if (producto && producto.id != null) {
            mapa.set(producto.id, producto);
        }
    });

    nuevos.forEach((producto) => {
        if (producto && producto.id != null) {
            mapa.set(producto.id, producto);
        }
    });

    return Array.from(mapa.values());
}

/* =========================
   HELPERS UI
========================= */

function el(tag, options = {}) {
    const node = document.createElement(tag);

    if (options.className) node.className = options.className;
    if (options.text !== undefined) node.textContent = options.text;
    if (options.html !== undefined) node.innerHTML = options.html;

    const attrs = ["id", "type", "value", "placeholder", "src", "alt"];

    attrs.forEach((attr) => {
        if (options[attr] !== undefined) node[attr] = options[attr];
    });

    return node;
}

function limpiarContenedor(contenedor) {
    if (!contenedor) return;

    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
}

function renderizarEstadoVacio(contenedor, titulo, texto) {
    limpiarContenedor(contenedor);

    const box = el("div", { className: "empty-admin-state" });

    box.append(
        el("h3", { text: titulo }),
        el("p", { text: texto })
    );

    contenedor.appendChild(box);
}

function crearBadge(texto) {
    return el("span", {
        className: "item-admin-badge",
        text: texto
    });
}

function crearBadgeEstado(estado) {
    return el("span", {
        className: `item-admin-badge item-admin-badge-estado ${obtenerClaseEstado(estado)}`,
        text: formatearEstadoPedidoTexto(estado)
    });
}

function crearBoton(texto, className, onClick) {
    const btn = el("button", {
        className,
        type: "button",
        text: texto
    });

    btn.addEventListener("click", onClick);
    return btn;
}

function crearTextoVacio(className, texto) {
    return el("p", { className, text: texto });
}

function crearDetalleCard(titulo, filas) {
    const card = el("div", { className: "detalle-card" });
    card.appendChild(el("h4", { text: titulo }));

    filas.forEach(([label, valor]) => {
        card.appendChild(crearParrafoDetalle(`${label}: ${valor}`));
    });

    return card;
}

function crearBloqueListaDetalle(titulo, lista, renderItem, textoVacio) {
    const card = el("div", { className: "detalle-card" });
    card.appendChild(el("h4", { text: titulo }));

    const listaWrap = el("div", { className: "detalle-lista" });

    if (Array.isArray(lista) && lista.length > 0) {
        lista.forEach((item) => listaWrap.appendChild(renderItem(item)));
    } else {
        listaWrap.appendChild(el("div", { className: "detalle-vacio", text: textoVacio }));
    }

    card.appendChild(listaWrap);
    return card;
}

function crearDetalleItem(titulo) {
    const item = el("div", { className: "detalle-item" });
    item.appendChild(el("h5", { text: titulo }));
    return item;
}

function crearParrafoDetalle(texto) {
    return el("p", { text: texto });
}

/* =========================
   HELPERS GENERALES
========================= */

function abrirModal(modal) {
    if (modal) modal.style.display = "flex";
}

function cerrarModal(modal, onClose) {
    if (typeof onClose === "function") onClose();
    if (modal) modal.style.display = "none";
}

function mostrarMensaje(refs, texto, tipo = "info") {
    if (!refs.mensajeAdmin) return;

    refs.mensajeAdmin.textContent = texto;
    refs.mensajeAdmin.className = "mensaje-admin";
    refs.mensajeAdmin.classList.add(`mensaje-admin-${tipo}`);
    refs.mensajeAdmin.style.display = "block";

    if (refs.mensajeAdmin.timeoutId) {
        clearTimeout(refs.mensajeAdmin.timeoutId);
    }

    refs.mensajeAdmin.timeoutId = setTimeout(() => {
        refs.mensajeAdmin.style.display = "none";
    }, 3000);
}

function mostrarEstado(elemento, texto, tipo = "info") {
    if (!elemento) return;

    elemento.textContent = texto;
    elemento.className = "estado-panel-admin";
    elemento.classList.add(tipo);
    elemento.style.display = "block";
}

function ocultarEstado(elemento) {
    if (!elemento) return;

    elemento.textContent = "";
    elemento.className = "estado-panel-admin";
    elemento.style.display = "none";
}

function bloquearBoton(boton, texto) {
    if (!boton) return;

    boton.disabled = true;
    boton.dataset.originalText = boton.textContent;
    boton.textContent = texto;
}

function restaurarBoton(boton, fallback) {
    if (!boton) return;

    boton.disabled = false;
    boton.textContent = boton.dataset.originalText || fallback;
}

function recortarTexto(texto, maximo) {
    if (!texto) return "";
    return texto.length > maximo ? `${texto.slice(0, maximo)}...` : texto;
}

function inicialNombre(nombre) {
    if (!nombre || !nombre.trim()) return "U";
    return nombre.trim().charAt(0).toUpperCase();
}

function formatearFecha(fecha) {
    if (!fecha) return "Sin fecha";

    const d = new Date(fecha);

    if (Number.isNaN(d.getTime())) return fecha;

    return d.toLocaleString("es-ES", {
        dateStyle: "short",
        timeStyle: "short"
    });
}

function formatearPrecio(valor) {
    if (valor === null || valor === undefined || valor === "") return "0 €";
    return `${valor} €`;
}

function obtenerClaseEstado(estado) {
    switch (estado) {
        case "PENDIENTE": return "estado-pendiente";
        case "CONFIRMADO": return "estado-confirmado";
        case "PREPARANDO": return "estado-preparando";
        case "ENVIADO": return "estado-enviado";
        case "LISTO_PARA_RECOGER": return "estado-preparando";
        case "PENDIENTE_CONFIRMACION_ENTREGA": return "estado-enviado";
        case "ENTREGADO": return "estado-entregado";
        case "CANCELADO": return "estado-cancelado";
        default: return "estado-confirmado";
    }
}

function formatearEstadoPedidoTexto(estado) {
    switch (estado) {
        case "PENDIENTE": return "Pendiente";
        case "CONFIRMADO": return "Confirmado";
        case "PREPARANDO": return "Preparando";
        case "ENVIADO": return "Enviado";
        case "LISTO_PARA_RECOGER": return "Listo para recoger";
        case "PENDIENTE_CONFIRMACION_ENTREGA": return "Pendiente confirmación entrega";
        case "ENTREGADO": return "Entregado";
        case "CANCELADO": return "Cancelado";
        default: return estado || "Sin estado";
    }
}

function formatearMetodoEntregaTexto(metodoEntrega) {
    switch (metodoEntrega) {
        case "DOMICILIO": return "Domicilio";
        case "RECOGIDA_TIENDA": return "Recogida en tienda";
        case "PUNTO_RECOGIDA": return "Punto de recogida";
        default: return "Sin método de entrega";
    }
}
