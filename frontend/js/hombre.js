let productosHombre = [];
let favoritosIds = new Set();
let sesionActual = null;

let paginaActual = 0;
let ultimaPagina = false;
let cargandoProductos = false;
let totalProductosCatalogo = 0;
let ordenCatalogo = "recientes";

const PRODUCTOS_POR_CARGA = 8;
const SECCION_ACTUAL = "HOMBRE";

const modal = document.getElementById("modal-login");
const cerrarModal = document.getElementById("cerrar-modal");
const cerrarModalSecundario = document.getElementById("cerrar-modal-secundario");
const modalMensaje = document.getElementById("modal-mensaje");
const abrirLoginModal = document.getElementById("abrir-login-modal");
const selectOrdenCatalogo = document.getElementById("orden-catalogo");

const formateadorEuro = new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR"
});

function mostrarToastCarrito(mensaje = "Añadido al carrito") {
    mostrarToast(mensaje);
}

function mostrarToastFavorito(mensaje = "Favorito actualizado") {
    mostrarToast(mensaje);
}

function mostrarToast(mensaje) {
    const toast = document.getElementById("toast-carrito");
    if (!toast) return;

    toast.textContent = mensaje;
    toast.classList.add("activo");

    clearTimeout(toast._timeoutId);

    toast._timeoutId = setTimeout(() => {
        toast.classList.remove("activo");
    }, 2200);
}

cerrarModal?.addEventListener("click", () => {
    modal.style.display = "none";
});

cerrarModalSecundario?.addEventListener("click", () => {
    modal.style.display = "none";
});

modal?.addEventListener("click", (event) => {
    if (event.target.classList.contains("modal-overlay")) {
        modal.style.display = "none";
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && modal?.style.display === "flex") {
        modal.style.display = "none";
        cerrarTodosLosMenusTalla();
    }
});

abrirLoginModal?.addEventListener("click", () => {
    modal.style.display = "none";

    if (typeof window.abrirLogin === "function") {
        window.abrirLogin();
    }
});

function cerrarTodosLosMenusTalla() {
    document.querySelectorAll(".mini-menu-talla.activo").forEach(menu => {
        menu.classList.remove("activo");
    });
}

document.addEventListener("click", () => {
    cerrarTodosLosMenusTalla();
});

async function obtenerSesionActual() {
    try {
        const response = await fetch(`${BASE_URL}/auth/session`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            sesionActual = null;
            return null;
        }

        const data = await response.json();
        sesionActual = data;
        return data;
    } catch (error) {
        console.error("Error al comprobar sesión:", error);
        sesionActual = null;
        return null;
    }
}

async function cargarFavoritosUsuario() {
    favoritosIds = new Set();

    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/favoritos/usuario/${sesion.id}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los favoritos");
        }

        const favoritos = await response.json();

        if (Array.isArray(favoritos)) {
            favoritos.forEach(favorito => {
                if (favorito.producto && favorito.producto.id != null) {
                    favoritosIds.add(Number(favorito.producto.id));
                }
            });
        }
    } catch (error) {
        console.error("Error al cargar favoritos del usuario:", error);
    }
}

function actualizarEstadoVisualFavorito(btnFav, productoId) {
    if (favoritosIds.has(Number(productoId))) {
        btnFav.classList.add("activo");
        btnFav.style.color = "#4c6a92";
    } else {
        btnFav.classList.remove("activo");
        btnFav.style.color = "";
    }
}

function obtenerFiltrosCatalogo() {
    const categorias = Array.from(
        document.querySelectorAll('input[data-tipo="categoria"]:checked')
    ).map(input => input.value);

    return {
        categorias
    };
}

function construirUrlCatalogo() {
    const filtros = obtenerFiltrosCatalogo();

    const params = new URLSearchParams();
    params.append("seccion", SECCION_ACTUAL);
    params.append("page", paginaActual);
    params.append("size", PRODUCTOS_POR_CARGA);
    params.append("orden", ordenCatalogo);

    filtros.categorias.forEach(categoria => {
        params.append("categoria", categoria);
    });

    return `${BASE_URL}/productos/catalogo?${params.toString()}`;
}

async function cargarProductosCatalogo(reiniciar = false) {
    if (cargandoProductos) {
        return;
    }

    if (ultimaPagina && !reiniciar) {
        return;
    }

    if (reiniciar) {
        paginaActual = 0;
        ultimaPagina = false;
        productosHombre = [];
        mostrarCargandoProductos();
    }

    cargandoProductos = true;

    try {
        const response = await fetch(construirUrlCatalogo(), {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los productos");
        }

        const data = await response.json();
        const productos = Array.isArray(data.productos) ? data.productos : [];

        totalProductosCatalogo = data.totalElementos ?? productos.length;
        ultimaPagina = data.ultimaPagina === true;
        paginaActual = data.paginaActual ?? paginaActual;

        if (reiniciar) {
            productosHombre = [...productos];
        } else {
            productosHombre = [...productosHombre, ...productos];
        }

        renderizarProductos(productos, reiniciar);

    } catch (error) {
        console.error("Error al cargar productos:", error);

        if (!reiniciar && paginaActual > 0) {
            paginaActual--;
        }

        mostrarErrorProductos();
    } finally {
        cargandoProductos = false;
    }
}

function mostrarCargandoProductos() {
    const grid = document.getElementById("grid-productos");
    const contador = document.getElementById("contador-productos");

    if (contador) {
        contador.textContent = "MAN COLLECTION / CARGANDO...";
    }

    if (grid) {
        grid.innerHTML = `<p class="sin-resultados">Cargando productos...</p>`;
    }
}

function mostrarErrorProductos() {
    const grid = document.getElementById("grid-productos");
    const contador = document.getElementById("contador-productos");

    if (contador) {
        contador.textContent = "MAN COLLECTION / ERROR";
    }

    if (grid && productosHombre.length === 0) {
        grid.innerHTML = `<p class="sin-resultados">No se pudieron cargar los productos.</p>`;
    }
}

function renderizarProductos(productos, reiniciar = true) {
    const grid = document.getElementById("grid-productos");
    const contador = document.getElementById("contador-productos");

    if (!grid || !contador) {
        return;
    }

    if (reiniciar) {
        grid.innerHTML = "";
    }

    contador.textContent = `MAN COLLECTION / ${totalProductosCatalogo} PRODUCTOS`;

    if (productos.length === 0 && reiniciar) {
        grid.innerHTML = `<p class="sin-resultados">No hay productos que coincidan con los filtros seleccionados.</p>`;
        return;
    }

    productos.forEach(producto => {
        grid.appendChild(crearCardProducto(producto));
    });
}

function crearCardProducto(producto) {
    const card = document.createElement("article");
    card.className = "tarjeta-hombre";

    const imgWrapper = document.createElement("div");
    imgWrapper.className = "img-wrapper";

    const btnFav = document.createElement("button");
    btnFav.className = "btn-fav";
    btnFav.type = "button";
    btnFav.textContent = "❤";

    const linkImagen = document.createElement("a");
    linkImagen.href = `fichaProducto.html?id=${producto.id}`;

    if (esUrlImagenValida(producto.urlImagen)) {
        const img = document.createElement("img");
        img.src = producto.urlImagen;
        img.alt = producto.nombre || "Producto";
        img.loading = "lazy";

        img.onerror = () => {
            img.style.display = "none";
            const sinImagen = document.createElement("span");
            sinImagen.textContent = "Sin imagen";
            linkImagen.appendChild(sinImagen);
        };

        linkImagen.appendChild(img);
    } else {
        const sinImagen = document.createElement("span");
        sinImagen.textContent = "Sin imagen";
        linkImagen.appendChild(sinImagen);
    }

    imgWrapper.append(btnFav, linkImagen);

    const infoProducto = document.createElement("div");
    infoProducto.className = "info-producto";

    const linkProducto = document.createElement("a");
    linkProducto.href = `fichaProducto.html?id=${producto.id}`;
    linkProducto.className = "link-producto";

    const nombre = document.createElement("h3");
    nombre.className = "nombre-prenda";
    nombre.textContent = producto.nombre || "Producto sin nombre";

    linkProducto.appendChild(nombre);

    const productoFooter = document.createElement("div");
    productoFooter.className = "producto-footer";

    const precio = document.createElement("p");
    precio.className = "p-final";
    precio.textContent = formatearPrecioProducto(producto.precio);

    const btnCarrito = document.createElement("button");
    btnCarrito.className = "btn-carrito";
    btnCarrito.type = "button";
    btnCarrito.textContent = "Añadir";

    productoFooter.append(precio, btnCarrito);

    const miniMenuTalla = document.createElement("div");
    miniMenuTalla.className = "mini-menu-talla";

    const listaTallas = document.createElement("div");
    listaTallas.className = "lista-tallas";

    const mensajeStock = document.createElement("p");
    mensajeStock.className = "mensaje-stock";
    mensajeStock.textContent = "Selecciona una talla";

    const btnConfirmarCarrito = document.createElement("button");
    btnConfirmarCarrito.className = "btn-confirmar-carrito";
    btnConfirmarCarrito.type = "button";
    btnConfirmarCarrito.textContent = "Confirmar";

    miniMenuTalla.append(listaTallas, mensajeStock, btnConfirmarCarrito);
    infoProducto.append(linkProducto, productoFooter, miniMenuTalla);
    card.append(imgWrapper, infoProducto);

    let tallaSeleccionada = null;

    actualizarEstadoVisualFavorito(btnFav, producto.id);
    configurarTallasProducto(producto, listaTallas, mensajeStock, (talla) => {
        tallaSeleccionada = talla;
    });

    miniMenuTalla.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    btnFav.addEventListener("click", async (event) => {
        event.preventDefault();
        event.stopPropagation();
        await alternarFavorito(producto, btnFav);
    });

    btnCarrito.addEventListener("click", async (event) => {
        event.preventDefault();
        event.stopPropagation();

        const sesion = await obtenerSesionActual();

        if (!sesion || !sesion.id) {
            mostrarModalLogin("Debes iniciar sesión para añadir productos al carrito.");
            return;
        }

        const estabaAbierto = miniMenuTalla.classList.contains("activo");

        cerrarTodosLosMenusTalla();

        if (!estabaAbierto) {
            miniMenuTalla.classList.add("activo");
        }
    });

    btnConfirmarCarrito.addEventListener("click", async (event) => {
        event.preventDefault();
        event.stopPropagation();

        const sesion = await obtenerSesionActual();

        if (!sesion || !sesion.id) {
            mensajeStock.textContent = "Debes iniciar sesión";
            return;
        }

        if (!tallaSeleccionada) {
            mensajeStock.textContent = "Selecciona una talla";
            return;
        }

        await agregarProductoAlCarrito(producto, tallaSeleccionada, miniMenuTalla, listaTallas, mensajeStock, () => {
            tallaSeleccionada = null;
        });
    });

    return card;
}

function configurarTallasProducto(producto, listaTallas, mensajeStock, onSeleccionarTalla) {
    const tallas = Array.isArray(producto.tallaStocks) ? producto.tallaStocks : [];

    if (tallas.length === 0) {
        mensajeStock.textContent = "No hay tallas disponibles";
        return;
    }

    tallas.forEach(tallaStock => {
        const botonTalla = document.createElement("button");
        botonTalla.type = "button";
        botonTalla.className = "btn-talla";
        botonTalla.textContent = tallaStock.talla;
        botonTalla.dataset.talla = tallaStock.talla;
        botonTalla.dataset.stock = tallaStock.stock;

        if (tallaStock.stock <= 0) {
            botonTalla.classList.add("agotada");
            botonTalla.disabled = true;
            botonTalla.title = "Agotado";
        }

        botonTalla.addEventListener("mouseenter", () => {
            actualizarMensajeStock(mensajeStock, tallaStock.stock);
        });

        botonTalla.addEventListener("mouseleave", () => {
            const botonSeleccionado = listaTallas.querySelector(".btn-talla.seleccionada");

            if (!botonSeleccionado) {
                mensajeStock.textContent = "Selecciona una talla";
                return;
            }

            actualizarMensajeStock(mensajeStock, Number(botonSeleccionado.dataset.stock));
        });

        if (tallaStock.stock > 0) {
            botonTalla.addEventListener("click", () => {
                const yaSeleccionada = botonTalla.classList.contains("seleccionada");

                listaTallas.querySelectorAll(".btn-talla").forEach(btn => {
                    btn.classList.remove("seleccionada");
                });

                if (yaSeleccionada) {
                    onSeleccionarTalla(null);
                    mensajeStock.textContent = "Selecciona una talla";
                } else {
                    botonTalla.classList.add("seleccionada");
                    onSeleccionarTalla(tallaStock.talla);
                    actualizarMensajeStock(mensajeStock, tallaStock.stock);
                }
            });
        }

        listaTallas.appendChild(botonTalla);
    });
}

function actualizarMensajeStock(mensajeStock, stock) {
    if (stock <= 0) {
        mensajeStock.textContent = "Agotado";
    } else if (stock <= 5) {
        mensajeStock.textContent = "Pocas unidades";
    } else {
        mensajeStock.textContent = "Disponible";
    }
}

async function alternarFavorito(producto, btnFav) {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mostrarModalLogin("Debes iniciar sesión para añadir productos a favoritos.");
        return;
    }

    try {
        const yaEsFavorito = favoritosIds.has(Number(producto.id));

        if (yaEsFavorito) {
            const response = await fetch(
                `${BASE_URL}/favoritos?usuarioId=${sesion.id}&productoId=${producto.id}`,
                {
                    method: "DELETE",
                    credentials: "include"
                }
            );

            if (!response.ok) {
                throw new Error("No se pudo eliminar de favoritos");
            }

            favoritosIds.delete(Number(producto.id));
            actualizarEstadoVisualFavorito(btnFav, producto.id);
            mostrarToastFavorito("Eliminado de favoritos");
        } else {
            const response = await fetch(
                `${BASE_URL}/favoritos?usuarioId=${sesion.id}&productoId=${producto.id}`,
                {
                    method: "POST",
                    credentials: "include"
                }
            );

            if (!response.ok) {
                throw new Error("No se pudo añadir a favoritos");
            }

            favoritosIds.add(Number(producto.id));
            actualizarEstadoVisualFavorito(btnFav, producto.id);
            mostrarToastFavorito("Añadido a favoritos");
        }

    } catch (error) {
        console.error("Error al actualizar favoritos:", error);
        mostrarToastFavorito("Error al actualizar favoritos");
    }
}

async function agregarProductoAlCarrito(producto, tallaSeleccionada, miniMenuTalla, listaTallas, mensajeStock, onResetTalla) {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        mensajeStock.textContent = "Debes iniciar sesión";
        return;
    }

    try {
        const response = await fetch(
            `${BASE_URL}/carrito/agregar?usuarioId=${sesion.id}&productoId=${producto.id}&talla=${tallaSeleccionada}&cantidad=1`,
            {
                method: "POST",
                credentials: "include"
            }
        );

        if (!response.ok) {
            throw new Error("No se pudo añadir al carrito");
        }

        miniMenuTalla.classList.remove("activo");

        listaTallas.querySelectorAll(".btn-talla").forEach(btn => {
            btn.classList.remove("seleccionada");
        });

        onResetTalla();
        mensajeStock.textContent = "Selecciona una talla";
        mostrarToastCarrito("Añadido al carrito");

        if (typeof window.actualizarContadorCarrito === "function") {
            window.actualizarContadorCarrito();
        }
    } catch (error) {
        console.error("Error al añadir al carrito:", error);
        mensajeStock.textContent = "Error al añadir";
    }
}

function mostrarModalLogin(mensaje) {
    if (modalMensaje) {
        modalMensaje.textContent = mensaje;
    }

    if (modal) {
        modal.style.display = "flex";
    }
}

function configurarFiltros() {
    document.querySelectorAll('input[data-tipo="categoria"]').forEach(input => {
        input.addEventListener("change", aplicarFiltros);
    });

    selectOrdenCatalogo?.addEventListener("change", () => {
        ordenCatalogo = selectOrdenCatalogo.value || "recientes";
        aplicarFiltros();
    });
}

function aplicarFiltros() {
    cerrarTodosLosMenusTalla();
    cargarProductosCatalogo(true);
}

function configurarScrollInfinito() {
    window.addEventListener("scroll", async () => {
        const cercaFinal =
            window.innerHeight + window.scrollY >= document.body.offsetHeight - 250;

        if (cercaFinal && !ultimaPagina && !cargandoProductos) {
            paginaActual++;
            await cargarProductosCatalogo(false);
        }
    });
}

function esUrlImagenValida(url) {
    if (!url) {
        return false;
    }

    return !url.includes(".m3u8") && !url.includes("master.m3u8");
}

function formatearPrecioProducto(valor) {
    if (valor === null || valor === undefined || valor === "") {
        return formateadorEuro.format(0);
    }

    return formateadorEuro.format(Number(valor));
}

async function iniciarHombre() {
    try {
        await cargarFavoritosUsuario();
        configurarFiltros();
        configurarScrollInfinito();
        await cargarProductosCatalogo(true);
    } catch (error) {
        console.error("Error:", error);
        mostrarErrorProductos();
    }
}

iniciarHombre();