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
    const botonesMenu = document.querySelectorAll(".menu-lateral-btn-admin");
    const secciones = document.querySelectorAll(".seccion-admin");
    const mensajeAdmin = document.getElementById("mensaje-admin");

    const btnGestionarProductos = document.getElementById("btn-gestionar-productos");
    const btnRecargarProductos = document.getElementById("btn-recargar-productos");
    const btnModoSeleccionProductos = document.getElementById("btn-modo-seleccion-productos");
    const btnStockSeleccionados = document.getElementById("btn-stock-seleccionados");

    const btnGestionarUsuarios = document.getElementById("btn-gestionar-usuarios");
    const btnRecargarUsuarios = document.getElementById("btn-recargar-usuarios");
    const btnGestionarPedidos = document.getElementById("btn-gestionar-pedidos");

    const btnScrapingZara = document.getElementById("btn-scraping-zara");
    const btnScrapingBershka = document.getElementById("btn-scraping-bershka");
    const btnScrapingPull = document.getElementById("btn-scraping-pull");
    const btnScrapingTodo = document.getElementById("btn-scraping-todo");
    const estadoScraping = document.getElementById("estado-scraping");

    const contenedorProductos = document.getElementById("contenedor-productos-admin");
    const productosAdminEstado = document.getElementById("productos-admin-estado");

    const contenedorUsuarios = document.getElementById("contenedor-usuarios-admin");
    const usuariosAdminEstado = document.getElementById("usuarios-admin-estado");

    const modalEditarProducto = document.getElementById("modal-editar-producto");
    const cerrarModalEditarProducto = document.getElementById("cerrar-modal-editar-producto");
    const cancelarModalEditarProducto = document.getElementById("cancelar-modal-editar-producto");
    const formEditarProducto = document.getElementById("form-editar-producto");
    const guardarCambiosProducto = document.getElementById("guardar-cambios-producto");

    const modalEliminarProducto = document.getElementById("modal-eliminar-producto");
    const cerrarModalEliminarProducto = document.getElementById("cerrar-modal-eliminar-producto");
    const cancelarModalEliminarProducto = document.getElementById("cancelar-modal-eliminar-producto");
    const confirmarEliminarProducto = document.getElementById("confirmar-eliminar-producto");

    const modalStockProductos = document.getElementById("modal-stock-productos");
    const cerrarModalStockProductos = document.getElementById("cerrar-modal-stock-productos");
    const cancelarModalStockProductos = document.getElementById("cancelar-modal-stock-productos");
    const formStockProductos = document.getElementById("form-stock-productos");
    const guardarStockProductos = document.getElementById("guardar-stock-productos");
    const resumenStockProductos = document.getElementById("stock-resumen-productos");
    const stockActualProducto = document.getElementById("stock-actual-producto");
    const stockCantidadProductos = document.getElementById("stock-cantidad-productos");

    const modalEditarUsuario = document.getElementById("modal-editar-usuario");
    const cerrarModalEditarUsuario = document.getElementById("cerrar-modal-editar-usuario");
    const cancelarModalEditarUsuario = document.getElementById("cancelar-modal-editar-usuario");
    const formEditarUsuario = document.getElementById("form-editar-usuario");
    const guardarCambiosUsuario = document.getElementById("guardar-cambios-usuario");

    const modalEliminarUsuario = document.getElementById("modal-eliminar-usuario");
    const cerrarModalEliminarUsuario = document.getElementById("cerrar-modal-eliminar-usuario");
    const cancelarModalEliminarUsuario = document.getElementById("cancelar-modal-eliminar-usuario");
    const confirmarEliminarUsuario = document.getElementById("confirmar-eliminar-usuario");

    let productoIdPendienteEliminar = null;
    let usuarioIdPendienteEliminar = null;

    let modoSeleccionProductos = false;
    let productosSeleccionados = new Set();
    let productosCache = [];
    let stockTargetProducts = [];

    function activarBoton(idSeccion) {
        botonesMenu.forEach((boton) => {
            boton.classList.remove("menu-lateral-btn-admin-activo");
            if (boton.dataset.seccion === idSeccion) {
                boton.classList.add("menu-lateral-btn-admin-activo");
            }
        });
    }

    function mostrarMensaje(texto, tipo = "info") {
        if (!mensajeAdmin) return;

        mensajeAdmin.textContent = texto;
        mensajeAdmin.className = "mensaje-admin";
        mensajeAdmin.classList.add(`mensaje-admin-${tipo}`);
        mensajeAdmin.style.display = "block";

        if (mensajeAdmin.timeoutId) {
            clearTimeout(mensajeAdmin.timeoutId);
        }

        mensajeAdmin.timeoutId = setTimeout(() => {
            mensajeAdmin.style.display = "none";
        }, 3000);
    }

    function mostrarEstado(elemento, texto, tipo = "info") {
        if (!elemento) return;
        elemento.textContent = texto;
        elemento.className = "gestion-estado-admin";
        elemento.classList.add(tipo);
        elemento.style.display = "block";
    }

    function ocultarEstado(elemento) {
        if (!elemento) return;
        elemento.textContent = "";
        elemento.className = "gestion-estado-admin";
        elemento.style.display = "none";
    }

    function recortarTexto(texto, maximo) {
        if (!texto) return "";
        return texto.length > maximo ? `${texto.slice(0, maximo)}...` : texto;
    }

    function escaparHTML(texto) {
        if (texto === null || texto === undefined) return "";
        return String(texto)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function inicialNombre(nombre) {
        if (!nombre || !nombre.trim()) return "U";
        return nombre.trim().charAt(0).toUpperCase();
    }

    function bloquearBoton(boton, texto) {
        if (!boton) return;
        boton.disabled = true;
        boton.textContent = texto;
    }

    function restaurarBoton(boton, texto) {
        if (!boton) return;
        boton.disabled = false;
        boton.textContent = texto;
    }

    function actualizarBotonStockSeleccionados() {
        if (!btnStockSeleccionados) return;
        btnStockSeleccionados.disabled = productosSeleccionados.size === 0;
    }

    function resetearSeleccionProductos() {
        productosSeleccionados = new Set();
        actualizarBotonStockSeleccionados();
    }

    botonesMenu.forEach((boton) => {
        boton.addEventListener("click", () => {
            const idSeccion = boton.dataset.seccion;
            const seccionDestino = document.getElementById(idSeccion);

            if (!seccionDestino) return;

            seccionDestino.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });

            activarBoton(idSeccion);
        });
    });

    const observer = new IntersectionObserver((entries) => {
        let seccionActiva = null;

        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                seccionActiva = entry.target.id;
            }
        });

        if (seccionActiva) {
            activarBoton(seccionActiva);
        }
    }, {
        root: null,
        rootMargin: "-140px 0px -55% 0px",
        threshold: 0.15
    });

    secciones.forEach((seccion) => observer.observe(seccion));

    if (btnGestionarProductos) {
        btnGestionarProductos.addEventListener("click", async () => {
            document.getElementById("productos").scrollIntoView({ behavior: "smooth", block: "start" });
            activarBoton("productos");
            await cargarProductosAdmin();
        });
    }

    if (btnRecargarProductos) {
        btnRecargarProductos.addEventListener("click", async () => {
            await cargarProductosAdmin();
        });
    }

    if (btnModoSeleccionProductos) {
        btnModoSeleccionProductos.addEventListener("click", () => {
            modoSeleccionProductos = !modoSeleccionProductos;
            btnModoSeleccionProductos.textContent = modoSeleccionProductos ? "Salir selección" : "Modo selección";
            resetearSeleccionProductos();
            renderizarProductosAdmin(productosCache);
        });
    }

    if (btnStockSeleccionados) {
        btnStockSeleccionados.addEventListener("click", () => {
            const seleccionados = productosCache.filter((p) => productosSeleccionados.has(p.id));
            if (seleccionados.length === 0) {
                mostrarMensaje("Selecciona al menos un producto.", "error");
                return;
            }
            abrirModalStock(seleccionados);
        });
    }

    if (btnGestionarUsuarios) {
        btnGestionarUsuarios.addEventListener("click", async () => {
            document.getElementById("usuarios").scrollIntoView({ behavior: "smooth", block: "start" });
            activarBoton("usuarios");
            await cargarUsuariosAdmin();
        });
    }

    if (btnRecargarUsuarios) {
        btnRecargarUsuarios.addEventListener("click", async () => {
            await cargarUsuariosAdmin();
        });
    }

    if (btnGestionarPedidos) {
        btnGestionarPedidos.addEventListener("click", () => {
            document.getElementById("pedidos").scrollIntoView({ behavior: "smooth", block: "start" });
            activarBoton("pedidos");
        });
    }

    async function ejecutarScraping(url, nombre, boton, textoOriginal) {
        try {
            if (estadoScraping) estadoScraping.textContent = "Ejecutando...";

            bloquearBoton(boton, "Ejecutando...");
            mostrarMensaje(`Iniciando scraping de ${nombre}...`, "info");

            const response = await fetch(`${BASE_URL}${url}`, {
                method: "POST",
                credentials: "include"
            });

            if (!response.ok) {
                throw new Error(`Error HTTP ${response.status}`);
            }

            const productos = await response.json();

            if (estadoScraping) estadoScraping.textContent = "Listo";

            mostrarMensaje(
                `Scraping de ${nombre} completado correctamente. Productos procesados: ${productos.length}.`,
                "ok"
            );

            await cargarMetricasAdmin();
            await cargarProductosAdmin();
        } catch (error) {
            console.error(`Error en scraping de ${nombre}:`, error);
            if (estadoScraping) estadoScraping.textContent = "Error";
            mostrarMensaje(`Error al ejecutar el scraping de ${nombre}.`, "error");
        } finally {
            restaurarBoton(boton, textoOriginal);
        }
    }

    if (btnScrapingZara) btnScrapingZara.addEventListener("click", () => ejecutarScraping("/productos/scrapear/zara", "Zara", btnScrapingZara, "Ejecutar scraping"));
    if (btnScrapingBershka) btnScrapingBershka.addEventListener("click", () => ejecutarScraping("/productos/scrapear/bershka", "Bershka", btnScrapingBershka, "Ejecutar scraping"));
    if (btnScrapingPull) btnScrapingPull.addEventListener("click", () => ejecutarScraping("/productos/scrapear/pullandbear", "Pull&Bear", btnScrapingPull, "Ejecutar scraping"));
    if (btnScrapingTodo) btnScrapingTodo.addEventListener("click", () => ejecutarScraping("/productos/scrapear/total", "todos los scrapers", btnScrapingTodo, "Ejecutar todo"));

    async function cargarMetricasAdmin() {
        const totalProductos = document.getElementById("total-productos");
        const totalUsuarios = document.getElementById("total-usuarios");

        try {
            const responseProductos = await fetch(`${BASE_URL}/productos`, {
                method: "GET",
                credentials: "include"
            });
            if (responseProductos.ok) {
                const productos = await responseProductos.json();
                if (totalProductos) totalProductos.textContent = Array.isArray(productos) ? productos.length : 0;
            }
        } catch (error) {
            console.error("Error cargando total de productos:", error);
        }

        try {
            const responseUsuarios = await fetch(`${BASE_URL}/usuarios`, {
                method: "GET",
                credentials: "include"
            });
            if (responseUsuarios.ok) {
                const usuarios = await responseUsuarios.json();
                if (totalUsuarios) totalUsuarios.textContent = Array.isArray(usuarios) ? usuarios.length : 0;
            }
        } catch (error) {
            console.error("Error cargando total de usuarios:", error);
        }
    }

    async function cargarProductosAdmin() {
        if (!contenedorProductos) return;

        try {
            mostrarEstado(productosAdminEstado, "Cargando productos...", "info");
            contenedorProductos.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>Cargando catálogo</h3>
                    <p>Espera un momento mientras se obtienen los productos.</p>
                </div>
            `;

            const response = await fetch(`${BASE_URL}/productos`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) {
                throw new Error("No se pudieron cargar los productos");
            }

            const productos = await response.json();
            productosCache = Array.isArray(productos) ? productos : [];
            renderizarProductosAdmin(productosCache);
            ocultarEstado(productosAdminEstado);
            await cargarMetricasAdmin();
        } catch (error) {
            console.error("Error al cargar productos admin:", error);
            mostrarEstado(productosAdminEstado, "No se pudieron cargar los productos.", "error");
            contenedorProductos.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>Error al cargar</h3>
                    <p>No se pudo obtener el catálogo.</p>
                </div>
            `;
        }
    }

    function renderizarProductosAdmin(productos) {
        if (!contenedorProductos) return;

        if (!Array.isArray(productos) || productos.length === 0) {
            contenedorProductos.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>No hay productos</h3>
                    <p>No se encontraron productos registrados en la plataforma.</p>
                </div>
            `;
            return;
        }

        contenedorProductos.innerHTML = productos.map((producto) => {
            const nombre = escaparHTML(producto.nombre || "Sin nombre");
            const descripcion = escaparHTML(recortarTexto(producto.descripcion || "Sin descripción", 140));
            const precio = producto.precio !== null && producto.precio !== undefined ? `${producto.precio} €` : "Sin precio";
            const tienda = escaparHTML(producto.tienda?.nombre || "Sin tienda");
            const categoria = escaparHTML(producto.categoria?.nombre || "Sin categoría");
            const urlImagen = producto.urlImagen ? escaparHTML(producto.urlImagen) : "";
            const checked = productosSeleccionados.has(producto.id) ? "checked" : "";

            return `
                <article class="item-admin-card ${modoSeleccionProductos ? "seleccionable" : ""}">
                    ${
                        modoSeleccionProductos
                            ? `<div class="selector-item-admin">
                                   <input type="checkbox" class="check-seleccion-producto" data-id="${producto.id}" ${checked} />
                               </div>`
                            : ``
                    }

                    <div class="item-admin-media">
                        ${
                            urlImagen
                                ? `<img src="${urlImagen}" alt="${nombre}" onerror="this.style.display='none'; this.parentElement.innerHTML='<span>Sin imagen</span>';">`
                                : `<span>Sin imagen</span>`
                        }
                    </div>

                    <div class="item-admin-body">
                        <h3>${nombre}</h3>

                        <div class="item-admin-meta">
                            <span class="item-admin-badge">${tienda}</span>
                            <span class="item-admin-badge">${categoria}</span>
                        </div>

                        <p class="item-admin-precio">${precio}</p>
                        <p class="item-admin-texto">${descripcion}</p>
                    </div>

                    <div class="item-admin-acciones">
                        <button class="btn btn-secundario btn-auto btn-stock-producto" type="button" data-id="${producto.id}">Stock</button>
                        <button class="btn btn-secundario btn-auto btn-editar-producto" type="button" data-id="${producto.id}">Editar</button>
                        <button class="btn btn-principal btn-auto btn-peligro btn-eliminar-producto" type="button" data-id="${producto.id}">Eliminar</button>
                    </div>
                </article>
            `;
        }).join("");

        document.querySelectorAll(".check-seleccion-producto").forEach((checkbox) => {
            checkbox.addEventListener("change", () => {
                const id = Number(checkbox.dataset.id);
                if (checkbox.checked) {
                    productosSeleccionados.add(id);
                } else {
                    productosSeleccionados.delete(id);
                }
                actualizarBotonStockSeleccionados();
            });
        });

        document.querySelectorAll(".btn-stock-producto").forEach((boton) => {
            boton.addEventListener("click", () => {
                const producto = productosCache.find((p) => p.id === Number(boton.dataset.id));
                if (producto) abrirModalStock([producto]);
            });
        });

        document.querySelectorAll(".btn-editar-producto").forEach((boton) => {
            boton.addEventListener("click", async () => {
                await abrirModalEditarProducto(boton.dataset.id);
            });
        });

        document.querySelectorAll(".btn-eliminar-producto").forEach((boton) => {
            boton.addEventListener("click", () => {
                abrirModalEliminarProducto(boton.dataset.id);
            });
        });

        actualizarBotonStockSeleccionados();
    }

    async function abrirModalEditarProducto(productoId) {
        try {
            const response = await fetch(`${BASE_URL}/productos/${productoId}`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) throw new Error("No se pudo cargar el producto");

            const producto = await response.json();

            document.getElementById("editar-producto-id").value = producto.id || "";
            document.getElementById("editar-producto-nombre").value = producto.nombre || "";
            document.getElementById("editar-producto-descripcion").value = producto.descripcion || "";
            document.getElementById("editar-producto-precio").value = producto.precio ?? "";
            document.getElementById("editar-producto-url-imagen").value = producto.urlImagen || "";
            document.getElementById("editar-producto-url-producto").value = producto.urlProducto || "";

            modalEditarProducto.style.display = "flex";
        } catch (error) {
            console.error("Error al abrir modal de edición:", error);
            mostrarMensaje("No se pudo cargar el producto para editar.", "error");
        }
    }

    function cerrarModalEditarProductoFn() {
        if (modalEditarProducto) modalEditarProducto.style.display = "none";
        if (formEditarProducto) formEditarProducto.reset();
    }

    function abrirModalEliminarProducto(productoId) {
        productoIdPendienteEliminar = productoId;
        if (modalEliminarProducto) modalEliminarProducto.style.display = "flex";
    }

    function cerrarModalEliminarProductoFn() {
        productoIdPendienteEliminar = null;
        if (modalEliminarProducto) modalEliminarProducto.style.display = "none";
    }

    if (cerrarModalEditarProducto) cerrarModalEditarProducto.addEventListener("click", cerrarModalEditarProductoFn);
    if (cancelarModalEditarProducto) cancelarModalEditarProducto.addEventListener("click", cerrarModalEditarProductoFn);
    if (modalEditarProducto) {
        modalEditarProducto.addEventListener("click", (e) => {
            if (e.target === modalEditarProducto) cerrarModalEditarProductoFn();
        });
    }

    if (formEditarProducto) {
        formEditarProducto.addEventListener("submit", async (e) => {
            e.preventDefault();

            const productoId = document.getElementById("editar-producto-id").value.trim();
            const nombre = document.getElementById("editar-producto-nombre").value.trim();
            const descripcion = document.getElementById("editar-producto-descripcion").value.trim();
            const precio = document.getElementById("editar-producto-precio").value.trim();
            const urlImagen = document.getElementById("editar-producto-url-imagen").value.trim();

            if (!productoId || !nombre || !precio) {
                mostrarMensaje("Nombre y precio son obligatorios.", "error");
                return;
            }

            try {
                bloquearBoton(guardarCambiosProducto, "Guardando...");

                const responseProducto = await fetch(`${BASE_URL}/productos/${productoId}`, {
                    method: "GET",
                    credentials: "include"
                });

                if (!responseProducto.ok) throw new Error("No se pudo recuperar el producto actual");

                const productoCompleto = await responseProducto.json();

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

                cerrarModalEditarProductoFn();
                mostrarMensaje("Producto actualizado correctamente.", "ok");
                await cargarProductosAdmin();
            } catch (error) {
                console.error("Error al actualizar producto:", error);
                mostrarMensaje("No se pudo actualizar el producto.", "error");
            } finally {
                restaurarBoton(guardarCambiosProducto, "Guardar cambios");
            }
        });
    }

    if (cerrarModalEliminarProducto) cerrarModalEliminarProducto.addEventListener("click", cerrarModalEliminarProductoFn);
    if (cancelarModalEliminarProducto) cancelarModalEliminarProducto.addEventListener("click", cerrarModalEliminarProductoFn);
    if (modalEliminarProducto) {
        modalEliminarProducto.addEventListener("click", (e) => {
            if (e.target === modalEliminarProducto) cerrarModalEliminarProductoFn();
        });
    }

    if (confirmarEliminarProducto) {
        confirmarEliminarProducto.addEventListener("click", async () => {
            if (!productoIdPendienteEliminar) return;

            try {
                bloquearBoton(confirmarEliminarProducto, "Eliminando...");

                const response = await fetch(`${BASE_URL}/productos/${productoIdPendienteEliminar}`, {
                    method: "DELETE",
                    credentials: "include"
                });

                if (!response.ok) throw new Error("No se pudo eliminar el producto");

                cerrarModalEliminarProductoFn();
                mostrarMensaje("Producto eliminado correctamente.", "ok");
                await cargarProductosAdmin();
            } catch (error) {
                console.error("Error al eliminar producto:", error);
                mostrarMensaje("No se pudo eliminar el producto.", "error");
            } finally {
                restaurarBoton(confirmarEliminarProducto, "Eliminar");
            }
        });
    }

    function limpiarChecksTallaStock() {
        document.querySelectorAll(".check-stock-talla").forEach((check) => {
            check.checked = false;
        });
    }

    async function abrirModalStock(productos) {
        stockTargetProducts = productos;

        if (resumenStockProductos) {
            resumenStockProductos.innerHTML = productos
                .map((p) => `<span class="resumen-stock-badge">${escaparHTML(p.nombre || `Producto ${p.id}`)}</span>`)
                .join("");
        }

        if (stockCantidadProductos) {
            stockCantidadProductos.value = "";
        }

        limpiarChecksTallaStock();

        if (productos.length === 1) {
            try {
                const response = await fetch(`${BASE_URL}/productos/${productos[0].id}/talla-stock`, {
                    method: "GET",
                    credentials: "include"
                });

                if (!response.ok) throw new Error("No se pudo cargar el stock actual");

                const tallas = await response.json();

                stockActualProducto.innerHTML = Array.isArray(tallas) && tallas.length > 0
                    ? tallas.map((item) => `<span class="stock-talla-badge">${escaparHTML(item.talla)}: ${item.stock}</span>`).join("")
                    : `<p class="texto-stock-vacio">No hay stock cargado todavía.</p>`;
            } catch (error) {
                console.error("Error cargando stock actual:", error);
                stockActualProducto.innerHTML = `<p class="texto-stock-vacio">No se pudo cargar el stock actual.</p>`;
            }
        } else {
            stockActualProducto.innerHTML = `<p class="texto-stock-vacio">Hay varios productos seleccionados. Se aplicará el mismo stock a todos los productos y tallas marcadas.</p>`;
        }

        modalStockProductos.style.display = "flex";
    }

    function cerrarModalStockFn() {
        stockTargetProducts = [];
        if (modalStockProductos) modalStockProductos.style.display = "none";
        if (formStockProductos) formStockProductos.reset();
        limpiarChecksTallaStock();
        if (stockActualProducto) {
            stockActualProducto.innerHTML = `<p class="texto-stock-vacio">Selecciona un producto para ver el stock actual por talla.</p>`;
        }
    }

    if (cerrarModalStockProductos) cerrarModalStockProductos.addEventListener("click", cerrarModalStockFn);
    if (cancelarModalStockProductos) cancelarModalStockProductos.addEventListener("click", cerrarModalStockFn);
    if (modalStockProductos) {
        modalStockProductos.addEventListener("click", (e) => {
            if (e.target === modalStockProductos) cerrarModalStockFn();
        });
    }

    if (formStockProductos) {
        formStockProductos.addEventListener("submit", async (e) => {
            e.preventDefault();

            const cantidad = Number(stockCantidadProductos.value);
            const tallasSeleccionadas = Array.from(document.querySelectorAll(".check-stock-talla:checked"))
                .map((check) => check.value);

            if (!stockTargetProducts.length) {
                mostrarMensaje("No hay productos seleccionados para aplicar stock.", "error");
                return;
            }

            if (!tallasSeleccionadas.length) {
                mostrarMensaje("Selecciona al menos una talla.", "error");
                return;
            }

            if (Number.isNaN(cantidad) || cantidad < 0) {
                mostrarMensaje("Introduce una cantidad válida.", "error");
                return;
            }

            try {
                bloquearBoton(guardarStockProductos, "Guardando...");

                for (const producto of stockTargetProducts) {
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
                            throw new Error(`No se pudo guardar el stock para ${producto.nombre} (${talla})`);
                        }
                    }
                }

                cerrarModalStockFn();
                mostrarMensaje("Stock guardado correctamente.", "ok");
            } catch (error) {
                console.error("Error guardando stock:", error);
                mostrarMensaje("No se pudo guardar el stock.", "error");
            } finally {
                restaurarBoton(guardarStockProductos, "Guardar stock");
            }
        });
    }

    async function cargarUsuariosAdmin() {
        if (!contenedorUsuarios) return;

        try {
            mostrarEstado(usuariosAdminEstado, "Cargando usuarios...", "info");
            contenedorUsuarios.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>Cargando usuarios</h3>
                    <p>Espera un momento mientras se obtienen las cuentas registradas.</p>
                </div>
            `;

            const response = await fetch(`${BASE_URL}/usuarios`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) throw new Error("No se pudieron cargar los usuarios");

            const usuarios = await response.json();
            renderizarUsuariosAdmin(usuarios);
            ocultarEstado(usuariosAdminEstado);
            await cargarMetricasAdmin();
        } catch (error) {
            console.error("Error al cargar usuarios admin:", error);
            mostrarEstado(usuariosAdminEstado, "No se pudieron cargar los usuarios.", "error");
            contenedorUsuarios.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>Error al cargar</h3>
                    <p>No se pudo obtener la lista de usuarios.</p>
                </div>
            `;
        }
    }

    function renderizarUsuariosAdmin(usuarios) {
        if (!contenedorUsuarios) return;

        if (!Array.isArray(usuarios) || usuarios.length === 0) {
            contenedorUsuarios.innerHTML = `
                <div class="placeholder-admin-cards">
                    <h3>No hay usuarios</h3>
                    <p>No se encontraron usuarios registrados.</p>
                </div>
            `;
            return;
        }

        contenedorUsuarios.innerHTML = usuarios.map((usuario) => {
            const nombre = escaparHTML(usuario.nombre || "Sin nombre");
            const email = escaparHTML(usuario.email || "Sin email");
            const rol = usuario.rol === "ADMIN" ? "ADMIN" : "USUARIO";

            return `
                <article class="item-admin-card">
                    <div class="item-admin-avatar">${inicialNombre(usuario.nombre)}</div>

                    <div class="item-admin-body">
                        <h3>${nombre}</h3>

                        <div class="item-admin-meta">
                            <span class="item-admin-badge">${rol}</span>
                        </div>

                        <p class="item-admin-texto">${email}</p>
                        <p class="item-admin-texto">ID usuario: ${usuario.id}</p>
                    </div>

                    <div class="item-admin-acciones">
                        <button class="btn btn-secundario btn-auto btn-editar-usuario" type="button" data-id="${usuario.id}">Editar</button>
                        <button class="btn btn-principal btn-auto btn-peligro btn-eliminar-usuario" type="button" data-id="${usuario.id}">Eliminar</button>
                    </div>
                </article>
            `;
        }).join("");

        document.querySelectorAll(".btn-editar-usuario").forEach((boton) => {
            boton.addEventListener("click", async () => {
                await abrirModalEditarUsuario(boton.dataset.id);
            });
        });

        document.querySelectorAll(".btn-eliminar-usuario").forEach((boton) => {
            boton.addEventListener("click", () => {
                abrirModalEliminarUsuario(boton.dataset.id);
            });
        });
    }

    async function abrirModalEditarUsuario(usuarioId) {
        try {
            const response = await fetch(`${BASE_URL}/usuarios/${usuarioId}`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) throw new Error("No se pudo cargar el usuario");

            const usuario = await response.json();

            document.getElementById("editar-usuario-id").value = usuario.id || "";
            document.getElementById("editar-usuario-nombre").value = usuario.nombre || "";
            document.getElementById("editar-usuario-email").value = usuario.email || "";
            document.getElementById("editar-usuario-rol").value = usuario.rol || "USER";

            modalEditarUsuario.style.display = "flex";
        } catch (error) {
            console.error("Error al abrir modal de usuario:", error);
            mostrarMensaje("No se pudo cargar el usuario para editar.", "error");
        }
    }

    function cerrarModalEditarUsuarioFn() {
        if (modalEditarUsuario) modalEditarUsuario.style.display = "none";
        if (formEditarUsuario) formEditarUsuario.reset();
    }

    function abrirModalEliminarUsuario(usuarioId) {
        usuarioIdPendienteEliminar = usuarioId;
        if (modalEliminarUsuario) modalEliminarUsuario.style.display = "flex";
    }

    function cerrarModalEliminarUsuarioFn() {
        usuarioIdPendienteEliminar = null;
        if (modalEliminarUsuario) modalEliminarUsuario.style.display = "none";
    }

    if (cerrarModalEditarUsuario) cerrarModalEditarUsuario.addEventListener("click", cerrarModalEditarUsuarioFn);
    if (cancelarModalEditarUsuario) cancelarModalEditarUsuario.addEventListener("click", cerrarModalEditarUsuarioFn);
    if (modalEditarUsuario) {
        modalEditarUsuario.addEventListener("click", (e) => {
            if (e.target === modalEditarUsuario) cerrarModalEditarUsuarioFn();
        });
    }

    if (formEditarUsuario) {
        formEditarUsuario.addEventListener("submit", async (e) => {
            e.preventDefault();

            const usuarioId = document.getElementById("editar-usuario-id").value.trim();
            const nombre = document.getElementById("editar-usuario-nombre").value.trim();
            const email = document.getElementById("editar-usuario-email").value.trim();
            const rol = document.getElementById("editar-usuario-rol").value;

            if (!usuarioId || !nombre || !email) {
                mostrarMensaje("Nombre y email son obligatorios.", "error");
                return;
            }

            try {
                bloquearBoton(guardarCambiosUsuario, "Guardando...");

                const responseUsuario = await fetch(`${BASE_URL}/usuarios/${usuarioId}`, {
                    method: "GET",
                    credentials: "include"
                });

                if (!responseUsuario.ok) throw new Error("No se pudo recuperar el usuario actual");

                const usuarioCompleto = await responseUsuario.json();

                const payload = {
                    id: usuarioCompleto.id,
                    nombre,
                    email,
                    rol
                };

                const responseActualizar = await fetch(`${BASE_URL}/usuarios/${usuarioId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    credentials: "include",
                    body: JSON.stringify(payload)
                });

                if (!responseActualizar.ok) throw new Error("No se pudo actualizar el usuario");

                cerrarModalEditarUsuarioFn();
                mostrarMensaje("Usuario actualizado correctamente.", "ok");
                await cargarUsuariosAdmin();
            } catch (error) {
                console.error("Error al actualizar usuario:", error);
                mostrarMensaje("No se pudo actualizar el usuario.", "error");
            } finally {
                restaurarBoton(guardarCambiosUsuario, "Guardar cambios");
            }
        });
    }

    if (cerrarModalEliminarUsuario) cerrarModalEliminarUsuario.addEventListener("click", cerrarModalEliminarUsuarioFn);
    if (cancelarModalEliminarUsuario) cancelarModalEliminarUsuario.addEventListener("click", cerrarModalEliminarUsuarioFn);
    if (modalEliminarUsuario) {
        modalEliminarUsuario.addEventListener("click", (e) => {
            if (e.target === modalEliminarUsuario) cerrarModalEliminarUsuarioFn();
        });
    }

    if (confirmarEliminarUsuario) {
        confirmarEliminarUsuario.addEventListener("click", async () => {
            if (!usuarioIdPendienteEliminar) return;

            try {
                bloquearBoton(confirmarEliminarUsuario, "Eliminando...");

                const response = await fetch(`${BASE_URL}/usuarios/${usuarioIdPendienteEliminar}`, {
                    method: "DELETE",
                    credentials: "include"
                });

                if (!response.ok) throw new Error("No se pudo eliminar el usuario");

                cerrarModalEliminarUsuarioFn();
                mostrarMensaje("Usuario eliminado correctamente.", "ok");
                await cargarUsuariosAdmin();
            } catch (error) {
                console.error("Error al eliminar usuario:", error);
                mostrarMensaje("No se pudo eliminar el usuario. Puede tener relaciones pendientes en backend.", "error");
            } finally {
                restaurarBoton(confirmarEliminarUsuario, "Eliminar");
            }
        });
    }

    cargarMetricasAdmin();
}