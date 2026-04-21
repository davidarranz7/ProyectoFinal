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
    configurarScraping(refs);
    configurarProductos(refs, state);
    configurarUsuarios(refs, state);
    configurarPedidos(refs, state);
    configurarModales(refs, state);

    actualizarEstadoScraping(refs, "Inactivo", "Sin procesos activos en este momento.", "neutral");

    cargarTodo(refs, state);
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

        btnScrapingZara: document.getElementById("btn-scraping-zara"),
        btnScrapingBershka: document.getElementById("btn-scraping-bershka"),
        btnScrapingPull: document.getElementById("btn-scraping-pull"),
        btnScrapingTodo: document.getElementById("btn-scraping-todo"),

        buscadorProductos: document.getElementById("buscador-productos-admin"),
        btnModoSeleccionProductos: document.getElementById("btn-modo-seleccion-productos"),
        btnSeleccionarTodosProductos: document.getElementById("btn-seleccionar-todos-productos"),
        btnStockSeleccionados: document.getElementById("btn-stock-seleccionados"),
        contenedorProductos: document.getElementById("contenedor-productos-admin"),
        estadoProductos: document.getElementById("productos-admin-estado"),

        contenedorUsuarios: document.getElementById("contenedor-usuarios-admin"),
        estadoUsuarios: document.getElementById("usuarios-admin-estado"),

        filtroEstadoPedidos: document.getElementById("filtro-estado-pedidos"),
        contenedorPedidos: document.getElementById("contenedor-pedidos-admin"),
        estadoPedidos: document.getElementById("pedidos-admin-estado"),

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
        guardarCambioEstadoPedido: document.getElementById("guardar-cambio-estado-pedido")
    };
}

function crearEstadoInicial() {
    return {
        productos: [],
        productosFiltrados: [],
        usuarios: [],
        pedidos: [],
        modoSeleccionProductos: false,
        productosSeleccionados: new Set(),
        productoIdPendienteEliminar: null,
        usuarioIdPendienteEliminar: null,
        productosStockObjetivo: [],
        pedidoCambioEstado: null
    };
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

function configurarScraping(refs) {
    refs.btnScrapingZara?.addEventListener("click", () =>
        ejecutarScraping(refs, "/productos/scrapear/zara", "Zara", refs.btnScrapingZara, "Ejecutar")
    );
    refs.btnScrapingBershka?.addEventListener("click", () =>
        ejecutarScraping(refs, "/productos/scrapear/bershka", "Bershka", refs.btnScrapingBershka, "Ejecutar")
    );
    refs.btnScrapingPull?.addEventListener("click", () =>
        ejecutarScraping(refs, "/productos/scrapear/pullandbear", "Pull&Bear", refs.btnScrapingPull, "Ejecutar")
    );
    refs.btnScrapingTodo?.addEventListener("click", () =>
        ejecutarScraping(refs, "/productos/scrapear/total", "Scraping completo", refs.btnScrapingTodo, "Ejecutar todo")
    );
}

function configurarProductos(refs, state) {
    refs.buscadorProductos?.addEventListener("input", () => {
        aplicarFiltroProductos(refs, state);
    });

    refs.btnModoSeleccionProductos?.addEventListener("click", () => {
        state.modoSeleccionProductos = !state.modoSeleccionProductos;
        refs.btnModoSeleccionProductos.textContent = state.modoSeleccionProductos ? "Salir selección" : "Modo selección";

        if (!state.modoSeleccionProductos) {
            state.productosSeleccionados.clear();
        }

        actualizarBotonStockSeleccionados(refs, state);
        renderizarProductos(refs, state);
    });

    refs.btnSeleccionarTodosProductos?.addEventListener("click", () => {
        if (!state.modoSeleccionProductos) {
            mostrarMensaje(refs, "Activa primero el modo selección.", "info");
            return;
        }

        const visibles = state.productosFiltrados;
        const todosSeleccionados = visibles.length > 0 && visibles.every((p) => state.productosSeleccionados.has(p.id));

        if (todosSeleccionados) {
            visibles.forEach((p) => state.productosSeleccionados.delete(p.id));
        } else {
            visibles.forEach((p) => state.productosSeleccionados.add(p.id));
        }

        actualizarBotonStockSeleccionados(refs, state);
        renderizarProductos(refs, state);
    });

    refs.btnStockSeleccionados?.addEventListener("click", () => {
        const seleccionados = state.productos.filter((p) => state.productosSeleccionados.has(p.id));
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

function configurarModales(refs, state) {
    configurarCerrarModal(refs.modalEditarProducto, refs.cerrarModalEditarProducto, refs.cancelarModalEditarProducto, () => {
        refs.formEditarProducto?.reset();
    });

    configurarCerrarModal(refs.modalEliminarProducto, refs.cerrarModalEliminarProducto, refs.cancelarModalEliminarProducto, () => {
        state.productoIdPendienteEliminar = null;
    });

    configurarCerrarModal(refs.modalStockProductos, refs.cerrarModalStockProductos, refs.cancelarModalStockProductos, () => {
        state.productosStockObjetivo = [];
        refs.formStockProductos?.reset();
        limpiarContenedor(refs.stockResumenProductos);
        limpiarContenedor(refs.stockActualProducto);
        refs.stockActualProducto?.appendChild(crearTextoVacio("texto-box-vacio", "Selecciona un producto para ver el stock actual por talla."));
    });

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

    configurarCerrarModal(refs.modalCambiarEstadoPedido, refs.cerrarModalCambiarEstadoPedido, refs.cancelarModalCambiarEstadoPedido, () => {
        refs.formCambiarEstadoPedido?.reset();
        state.pedidoCambioEstado = null;
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
        cargarProductos(refs, state),
        cargarUsuarios(refs, state),
        cargarPedidos(refs, state)
    ]);
}

async function cargarMetricas(refs) {
    try {
        const [resProductos, resUsuarios, resPedidos] = await Promise.all([
            fetch(`${BASE_URL}/productos`, { method: "GET", credentials: "include" }),
            fetch(`${BASE_URL}/usuarios`, { method: "GET", credentials: "include" }),
            fetch(`${BASE_URL}/pedidos`, { method: "GET", credentials: "include" })
        ]);

        if (resProductos.ok) {
            const productos = await resProductos.json();
            refs.totalProductos.textContent = Array.isArray(productos) ? productos.length : 0;
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

async function cargarProductos(refs, state) {
    try {
        mostrarEstado(refs.estadoProductos, "Cargando productos...", "info");

        const response = await fetch(`${BASE_URL}/productos`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los productos");
        }

        state.productos = await response.json();
        state.productosFiltrados = [...state.productos];

        ocultarEstado(refs.estadoProductos);
        renderizarProductos(refs, state);
        await cargarMetricas(refs);
    } catch (error) {
        console.error("Error cargando productos:", error);
        mostrarEstado(refs.estadoProductos, "No se pudieron cargar los productos.", "error");
        renderizarEstadoVacio(refs.contenedorProductos, "Error al cargar", "No se pudo obtener el catálogo.");
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
        await cargarMetricas(refs);
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
        await cargarMetricas(refs);
    } catch (error) {
        console.error("Error cargando pedidos:", error);
        mostrarEstado(refs.estadoPedidos, "No se pudieron cargar los pedidos.", "error");
        renderizarEstadoVacio(refs.contenedorPedidos, "Error al cargar", "No se pudo obtener la lista de pedidos.");
    }
}

/* =========================
   SCRAPING
========================= */

async function ejecutarScraping(refs, url, nombre, boton, textoOriginal) {
    try {
        bloquearBoton(boton, "Ejecutando...");
        actualizarEstadoScraping(refs, "Ejecutando", `Lanzando ${nombre}...`, "info");
        mostrarMensaje(refs, `Iniciando ${nombre}...`, "info");

        const response = await fetch(`${BASE_URL}${url}`, {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error(`Error HTTP ${response.status}`);
        }

        const productos = await response.json();
        const total = Array.isArray(productos) ? productos.length : 0;

        actualizarEstadoScraping(refs, "Completado", `${nombre} finalizado. Productos procesados: ${total}`, "success");
        mostrarMensaje(refs, `${nombre} completado correctamente.`, "ok");

        await cargarProductos(refs, crearEstadoPassthrough(stateLikeFromProducts(refs)));
        await cargarMetricas(refs);
    } catch (error) {
        console.error(`Error en ${nombre}:`, error);
        actualizarEstadoScraping(refs, "Error", `Falló ${nombre}`, "error");
        mostrarMensaje(refs, `Error al ejecutar ${nombre}.`, "error");
    } finally {
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

/* =========================
   PRODUCTOS
========================= */

function aplicarFiltroProductos(refs, state) {
    const termino = (refs.buscadorProductos?.value || "").trim().toLowerCase();

    if (!termino) {
        state.productosFiltrados = [...state.productos];
    } else {
        state.productosFiltrados = state.productos.filter((producto) => {
            const nombre = (producto.nombre || "").toLowerCase();
            const categoria = (producto.categoria?.nombre || "").toLowerCase();
            const tienda = (producto.tienda?.nombre || "").toLowerCase();

            return nombre.includes(termino) || categoria.includes(termino) || tienda.includes(termino);
        });
    }

    renderizarProductos(refs, state);
}

function renderizarProductos(refs, state) {
    limpiarContenedor(refs.contenedorProductos);

    if (!Array.isArray(state.productosFiltrados) || state.productosFiltrados.length === 0) {
        renderizarEstadoVacio(refs.contenedorProductos, "Sin resultados", "No se encontraron productos con los filtros actuales.");
        return;
    }

    state.productosFiltrados.forEach((producto) => {
        refs.contenedorProductos.appendChild(crearCardProducto(producto, refs, state));
    });

    actualizarBotonStockSeleccionados(refs, state);
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
            if (check.checked) state.productosSeleccionados.add(producto.id);
            else state.productosSeleccionados.delete(producto.id);

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
        const response = await fetch(`${BASE_URL}/productos/${productoId}`, {
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

        const responseActual = await fetch(`${BASE_URL}/productos/${productoId}`, {
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
        await cargarProductos(refs, state);
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
        await cargarProductos(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, "No se pudo eliminar el producto.", "error");
    } finally {
        restaurarBoton(refs.confirmarEliminarProducto, "Eliminar");
    }
}

async function abrirModalStock(refs, state, productos) {
    state.productosStockObjetivo = productos;

    limpiarContenedor(refs.stockResumenProductos);
    productos.forEach((producto) => {
        refs.stockResumenProductos.appendChild(el("span", {
            className: "resumen-stock-badge",
            text: producto.nombre || `Producto ${producto.id}`
        }));
    });

    refs.stockCantidadProductos.value = "";
    document.querySelectorAll(".check-stock-talla").forEach((check) => {
        check.checked = false;
    });

    limpiarContenedor(refs.stockActualProducto);

    if (productos.length === 1) {
        try {
            const response = await fetch(`${BASE_URL}/productos/${productos[0].id}/talla-stock`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) throw new Error("No se pudo cargar stock");

            const tallas = await response.json();

            if (Array.isArray(tallas) && tallas.length > 0) {
                tallas.forEach((item) => {
                    refs.stockActualProducto.appendChild(el("span", {
                        className: "stock-talla-badge",
                        text: `${item.talla}: ${item.stock}`
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
            crearTextoVacio("texto-box-vacio", "Se aplicará el mismo stock a todos los productos y tallas seleccionadas.")
        );
    }

    abrirModal(refs.modalStockProductos);
}

async function guardarStockProductos(refs, state) {
    const cantidad = Number(refs.stockCantidadProductos.value);
    const tallasSeleccionadas = Array.from(document.querySelectorAll(".check-stock-talla:checked"))
        .map((check) => check.value);

    if (!state.productosStockObjetivo.length) {
        mostrarMensaje(refs, "No hay productos seleccionados.", "error");
        return;
    }

    if (!tallasSeleccionadas.length) {
        mostrarMensaje(refs, "Selecciona al menos una talla.", "error");
        return;
    }

    if (Number.isNaN(cantidad) || cantidad < 0) {
        mostrarMensaje(refs, "Introduce una cantidad válida.", "error");
        return;
    }

    try {
        bloquearBoton(refs.guardarStockProductos, "Guardando...");

        for (const producto of state.productosStockObjetivo) {
            for (const talla of tallasSeleccionadas) {
                const response = await fetch(`${BASE_URL}/productos/talla-stock`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify({
                        productoId: producto.id,
                        talla,
                        stock: cantidad
                    })
                });

                if (!response.ok) {
                    throw new Error(`No se pudo guardar el stock para ${producto.nombre}`);
                }
            }
        }

        cerrarModal(refs.modalStockProductos, () => {
            refs.formStockProductos.reset();
            state.productosStockObjetivo = [];
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
        crearBoton("Cambiar estado", "btn btn-primary", () => {
            state.pedidoCambioEstado = pedido;
            refs.cambiarEstadoPedidoId.value = pedido.id;
            refs.nuevoEstadoPedido.value = pedido.estado || "CONFIRMADO";
            abrirModal(refs.modalCambiarEstadoPedido);
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
                ["Estado", pedido?.estado || "Sin estado"]
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
                        crearParrafoDetalle(`Talla: ${item.talla || "-"}`),
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
        });

        mostrarMensaje(refs, "Estado del pedido actualizado correctamente.", "ok");
        await cargarPedidos(refs, state);
    } catch (error) {
        console.error(error);
        mostrarMensaje(refs, error.message || "No se pudo cambiar el estado del pedido.", "error");
    } finally {
        restaurarBoton(refs.guardarCambioEstadoPedido, "Guardar cambio");
    }
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
    const badge = el("span", {
        className: `item-admin-badge item-admin-badge-estado ${obtenerClaseEstado(estado)}`,
        text: estado
    });
    return badge;
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
        case "ENTREGADO": return "estado-entregado";
        case "CANCELADO": return "estado-cancelado";
        default: return "estado-confirmado";
    }
}

/* =========================
   HELPERS INTERNO SCRAPING
========================= */

function stateLikeFromProducts(refs) {
    return {
        productos: [],
        productosFiltrados: [],
        usuarios: [],
        pedidos: [],
        modoSeleccionProductos: false,
        productosSeleccionados: new Set(),
        productoIdPendienteEliminar: null,
        usuarioIdPendienteEliminar: null,
        productosStockObjetivo: [],
        pedidoCambioEstado: null,
        contenedorProductos: refs.contenedorProductos
    };
}

function crearEstadoPassthrough(state) {
    return state;
}