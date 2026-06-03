const params = new URLSearchParams(window.location.search);
const productoId = params.get("id");

const btnVolver = document.getElementById("btn-volver");
const btnCarrito = document.getElementById("btn-carrito");
const btnFavorito = document.getElementById("btn-favorito");

const tallasLista = document.getElementById("tallas-lista");
const mensajeTalla = document.getElementById("mensaje-talla");
const mensajeStock = document.getElementById("mensaje-stock");

const imagenPrincipal = document.getElementById("producto-imagen");
const btnImagenAnterior = document.getElementById("btn-imagen-anterior");
const btnImagenSiguiente = document.getElementById("btn-imagen-siguiente");
const contadorImagenes = document.getElementById("contador-imagenes");
const miniaturasImagenes = document.getElementById("miniaturas-imagenes");

const modalLogin = document.getElementById("modal-login");
const cerrarModal = document.getElementById("cerrar-modal");
const cerrarModalSecundario = document.getElementById("cerrar-modal-secundario");
const modalMensaje = document.getElementById("modal-mensaje");
const abrirLoginModal = document.getElementById("abrir-login-modal");

const modalFeedback = document.getElementById("modal-feedback");
const cerrarFeedback = document.getElementById("cerrar-feedback");
const feedbackAceptar = document.getElementById("feedback-aceptar");
const feedbackEtiqueta = document.getElementById("feedback-etiqueta");
const feedbackTitulo = document.getElementById("feedback-titulo");
const feedbackMensaje = document.getElementById("feedback-mensaje");

let tallaSeleccionada = null;
let productoActual = null;
let esFavorito = false;
let sesionActual = null;

let imagenesProducto = [];
let indiceImagenActual = 0;
let timeoutFeedback = null;

const formateadorEuro = new Intl.NumberFormat("es-ES", {
    style: "currency",
    currency: "EUR"
});

if (btnVolver) {
    btnVolver.addEventListener("click", () => {
        if (window.history.length > 1) {
            window.history.back();
        } else {
            window.location.href = "zara.html";
        }
    });
}

function formatearPrecioProducto(valor) {
    if (valor === null || valor === undefined || valor === "") {
        return formateadorEuro.format(0);
    }

    return formateadorEuro.format(Number(valor));
}

function esUrlImagenValida(url) {
    if (!url || typeof url !== "string") {
        return false;
    }

    return !url.includes(".m3u8")
        && !url.includes("master.m3u8")
        && !url.includes("meta.json");
}

function obtenerUrlDesdeImagenProducto(imagen) {
    if (!imagen) {
        return "";
    }

    if (typeof imagen === "string") {
        return imagen;
    }

    return imagen.urlImagen
        || imagen.url_imagen
        || imagen.url
        || imagen.src
        || "";
}

function obtenerImagenesProducto(producto) {
    const urls = [];

    if (producto && esUrlImagenValida(producto.urlImagen)) {
        urls.push(producto.urlImagen);
    }

    const posiblesListas = [
        producto?.imagenes,
        producto?.productoImagenes,
        producto?.imagenesProducto,
        producto?.imagenesUrl,
        producto?.urlsImagenes
    ];

    posiblesListas.forEach(lista => {
        if (!Array.isArray(lista)) {
            return;
        }

        const listaOrdenada = [...lista].sort((a, b) => {
            const ordenA = Number(a?.orden ?? 999);
            const ordenB = Number(b?.orden ?? 999);

            return ordenA - ordenB;
        });

        listaOrdenada.forEach(imagen => {
            const url = obtenerUrlDesdeImagenProducto(imagen);

            if (esUrlImagenValida(url)) {
                urls.push(url);
            }
        });
    });

    return [...new Set(urls)];
}

function actualizarControlesGaleria() {
    const hayVariasImagenes = imagenesProducto.length > 1;

    if (btnImagenAnterior) {
        btnImagenAnterior.style.display = hayVariasImagenes ? "" : "none";
    }

    if (btnImagenSiguiente) {
        btnImagenSiguiente.style.display = hayVariasImagenes ? "" : "none";
    }

    if (contadorImagenes) {
        contadorImagenes.style.display = hayVariasImagenes ? "" : "none";
        contadorImagenes.textContent = hayVariasImagenes
            ? `${indiceImagenActual + 1}/${imagenesProducto.length}`
            : "1/1";
    }
}

function marcarMiniaturaActiva() {
    if (!miniaturasImagenes) {
        return;
    }

    miniaturasImagenes.querySelectorAll(".miniatura-imagen").forEach((miniatura, index) => {
        miniatura.classList.toggle("activa", index === indiceImagenActual);
    });
}

function mostrarImagenActual() {
    if (!imagenPrincipal) {
        return;
    }

    if (imagenesProducto.length === 0) {
        imagenPrincipal.removeAttribute("src");
        imagenPrincipal.alt = "Sin imagen";
        actualizarControlesGaleria();
        return;
    }

    if (indiceImagenActual < 0) {
        indiceImagenActual = imagenesProducto.length - 1;
    }

    if (indiceImagenActual >= imagenesProducto.length) {
        indiceImagenActual = 0;
    }

    imagenPrincipal.src = imagenesProducto[indiceImagenActual];
    imagenPrincipal.alt = productoActual?.nombre || "Imagen del producto";

    actualizarControlesGaleria();
    marcarMiniaturaActiva();
}

function pasarImagenSiguiente() {
    if (imagenesProducto.length <= 1) {
        return;
    }

    indiceImagenActual++;
    mostrarImagenActual();
}

function pasarImagenAnterior() {
    if (imagenesProducto.length <= 1) {
        return;
    }

    indiceImagenActual--;
    mostrarImagenActual();
}

function renderizarMiniaturas() {
    if (!miniaturasImagenes) {
        return;
    }

    miniaturasImagenes.innerHTML = "";

    imagenesProducto.forEach((url, index) => {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.className = "miniatura-imagen";

        const img = document.createElement("img");
        img.src = url;
        img.alt = `Imagen ${index + 1}`;
        img.loading = "lazy";

        boton.appendChild(img);

        boton.addEventListener("click", () => {
            indiceImagenActual = index;
            mostrarImagenActual();
        });

        miniaturasImagenes.appendChild(boton);
    });

    marcarMiniaturaActiva();
}

function renderizarGaleria(producto) {
    imagenesProducto = obtenerImagenesProducto(producto);
    indiceImagenActual = 0;

    console.log("Imágenes detectadas para la ficha:", imagenesProducto);

    renderizarMiniaturas();
    mostrarImagenActual();
}

if (btnImagenAnterior) {
    btnImagenAnterior.addEventListener("click", pasarImagenAnterior);
}

if (btnImagenSiguiente) {
    btnImagenSiguiente.addEventListener("click", pasarImagenSiguiente);
}

if (imagenPrincipal) {
    imagenPrincipal.addEventListener("error", () => {
        const urlFallida = imagenesProducto[indiceImagenActual];

        console.warn("Imagen fallida eliminada de la ficha:", urlFallida);

        imagenesProducto = imagenesProducto.filter(url => url !== urlFallida);

        if (indiceImagenActual >= imagenesProducto.length) {
            indiceImagenActual = 0;
        }

        renderizarMiniaturas();
        mostrarImagenActual();
    });
}

function cerrarModalLogin() {
    modalLogin?.classList.add("oculto");
}

function mostrarModalLogin(mensaje) {
    if (modalMensaje) {
        modalMensaje.textContent = mensaje;
    }

    modalLogin?.classList.remove("oculto");
}

function cerrarModalFeedback() {
    modalFeedback?.classList.add("oculto");
    clearTimeout(timeoutFeedback);
}

function mostrarFeedback(etiqueta, titulo, mensaje, cerrarAutomaticamente = true) {
    if (feedbackEtiqueta) {
        feedbackEtiqueta.textContent = etiqueta;
    }

    if (feedbackTitulo) {
        feedbackTitulo.textContent = titulo;
    }

    if (feedbackMensaje) {
        feedbackMensaje.textContent = mensaje;
    }

    modalFeedback?.classList.remove("oculto");

    clearTimeout(timeoutFeedback);

    if (cerrarAutomaticamente) {
        timeoutFeedback = setTimeout(() => {
            cerrarModalFeedback();
        }, 2300);
    }
}

cerrarModal?.addEventListener("click", cerrarModalLogin);
cerrarModalSecundario?.addEventListener("click", cerrarModalLogin);

modalLogin?.addEventListener("click", (event) => {
    if (event.target === modalLogin) {
        cerrarModalLogin();
    }
});

abrirLoginModal?.addEventListener("click", () => {
    cerrarModalLogin();

    if (typeof window.abrirLogin === "function") {
        window.abrirLogin();
        return;
    }

    window.location.href = "login.html";
});

cerrarFeedback?.addEventListener("click", cerrarModalFeedback);
feedbackAceptar?.addEventListener("click", cerrarModalFeedback);

modalFeedback?.addEventListener("click", (event) => {
    if (event.target === modalFeedback) {
        cerrarModalFeedback();
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") {
        return;
    }

    cerrarModalLogin();
    cerrarModalFeedback();
});

function ocultarMensajeValidacion() {
    if (!mensajeTalla) {
        return;
    }

    mensajeTalla.classList.add("oculto");
}

function actualizarMensajeStock(stock) {
    if (!mensajeStock) {
        return;
    }

    if (stock <= 0) {
        mensajeStock.textContent = "Agotado";
    } else if (stock <= 5) {
        mensajeStock.textContent = "Pocas unidades";
    } else {
        mensajeStock.textContent = "Disponible";
    }
}

function refrescarContadorCarritoConEspera() {
    setTimeout(() => {
        if (typeof window.actualizarContadorCarrito === "function") {
            window.actualizarContadorCarrito();
        }
    }, 150);
}

function actualizarBotonFavorito() {
    if (!btnFavorito) {
        return;
    }

    if (esFavorito) {
        btnFavorito.textContent = "Quitar de favoritos";
        btnFavorito.classList.add("favorito-activo");
    } else {
        btnFavorito.textContent = "Añadir a favoritos";
        btnFavorito.classList.remove("favorito-activo");
    }
}

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

async function comprobarSiEsFavorito() {
    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id || !productoId) {
        esFavorito = false;
        actualizarBotonFavorito();
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/favoritos/usuario/${sesion.id}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar favoritos");
        }

        const favoritos = await response.json();

        esFavorito = Array.isArray(favoritos) && favoritos.some(favorito =>
            favorito.producto && Number(favorito.producto.id) === Number(productoId)
        );

        actualizarBotonFavorito();
    } catch (error) {
        console.error("Error al comprobar favoritos:", error);
        esFavorito = false;
        actualizarBotonFavorito();
    }
}

function renderizarProducto(producto) {
    document.getElementById("producto-nombre").textContent = producto.nombre || "Producto sin nombre";
    document.getElementById("producto-precio").textContent = formatearPrecioProducto(producto.precio);
    document.getElementById("producto-descripcion").textContent = producto.descripcion || "Sin descripción";
    document.getElementById("producto-tienda").textContent = producto.tienda?.nombre || "Tienda";
    document.getElementById("producto-categoria").textContent = producto.categoria?.nombre || "Categoría";

    const btnTiendaOriginal = document.getElementById("btn-tienda-original");

    if (btnTiendaOriginal) {
        btnTiendaOriginal.href = producto.urlProducto || "#";
    }

    renderizarGaleria(producto);
}

function renderizarTallas(tallas) {
    if (!tallasLista) {
        return;
    }

    tallasLista.innerHTML = "";
    tallaSeleccionada = null;
    const tallasFiltradas = window.TallasProducto
        ? window.TallasProducto.filtrarTallaStocks(productoActual, tallas)
        : tallas;

    if (mensajeStock) {
        mensajeStock.textContent = "Selecciona una talla";
    }

    if (!Array.isArray(tallasFiltradas) || tallasFiltradas.length === 0) {
        if (mensajeStock) {
            mensajeStock.textContent = "Sin tallas disponibles";
        }

        return;
    }

    tallasFiltradas.forEach(item => {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.textContent = window.TallasProducto
            ? window.TallasProducto.formatearTalla(item.talla)
            : item.talla;
        boton.classList.add("talla-btn");
        boton.dataset.talla = item.talla;
        boton.dataset.stock = item.stock;

        if (item.stock > 0) {
            boton.classList.add("disponible");

            if (item.stock <= 5) {
                boton.classList.add("poco-stock");
                boton.title = "Pocas unidades";
            }

            boton.addEventListener("mouseenter", () => {
                actualizarMensajeStock(Number(item.stock));
            });

            boton.addEventListener("mouseleave", () => {
                const botonSeleccionado = tallasLista.querySelector(".talla-btn.seleccionada");

                if (!botonSeleccionado) {
                    mensajeStock.textContent = "Selecciona una talla";
                    return;
                }

                actualizarMensajeStock(Number(botonSeleccionado.dataset.stock));
            });

            boton.addEventListener("click", () => {
                const yaSeleccionada = boton.classList.contains("seleccionada");

                tallasLista.querySelectorAll(".talla-btn").forEach(btn => {
                    btn.classList.remove("seleccionada");
                });

                if (yaSeleccionada) {
                    tallaSeleccionada = null;
                    mensajeStock.textContent = "Selecciona una talla";
                } else {
                    boton.classList.add("seleccionada");
                    tallaSeleccionada = item.talla;
                    actualizarMensajeStock(Number(item.stock));
                    ocultarMensajeValidacion();
                }

                console.log("Talla seleccionada:", tallaSeleccionada);
            });
        } else {
            boton.classList.add("agotada");
            boton.disabled = true;
            boton.title = "Agotado";
        }

        tallasLista.appendChild(boton);
    });
}

async function cargarProducto() {
    if (!productoId) {
        mostrarFeedback(
            "Producto no encontrado",
            "Falta el identificador",
            "No se encontró el ID del producto en la URL.",
            false
        );
        return;
    }

    try {
        const response = await fetch(`${BASE_URL}/productos/${productoId}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cargar el producto");
        }

        const producto = await response.json();
        productoActual = producto;

        console.log("Producto cargado en ficha:", producto);

        renderizarProducto(producto);
        await comprobarSiEsFavorito();
        configurarEventosProducto();
        await cargarTallas();

    } catch (error) {
        console.error("Error al cargar producto:", error);

        mostrarFeedback(
            "Error",
            "No se pudo cargar el producto",
            "Prueba a volver al catálogo e intentarlo de nuevo.",
            false
        );
    }
}

async function cargarTallas() {
    try {
        const response = await fetch(`${BASE_URL}/productos/${productoId}/talla-stock`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar las tallas");
        }

        const tallas = await response.json();

        console.log("Tallas cargadas:", tallas);

        renderizarTallas(tallas);
    } catch (error) {
        console.error("Error al cargar tallas:", error);

        if (mensajeStock) {
            mensajeStock.textContent = "No se pudieron cargar las tallas";
        }
    }
}

function configurarEventosProducto() {
    if (btnCarrito) {
        btnCarrito.onclick = async () => {
            const sesion = await obtenerSesionActual();

            if (!sesion || !sesion.id) {
                mostrarModalLogin("Debes iniciar sesión para añadir este producto al carrito.");
                return;
            }

            if (!tallaSeleccionada) {
                mostrarFeedback(
                    "Talla necesaria",
                    "Selecciona una talla",
                    "Antes de añadir el producto al carrito tienes que elegir una talla.",
                    true
                );
                return;
            }

            try {
                const response = await fetch(
                    `${BASE_URL}/carrito/agregar?usuarioId=${sesion.id}&productoId=${productoId}&talla=${tallaSeleccionada}&cantidad=1`,
                    {
                        method: "POST",
                        credentials: "include"
                    }
                );

                if (!response.ok) {
                    throw new Error("No se pudo añadir al carrito");
                }

                const itemCarrito = await response.json();

                console.log("Producto añadido al carrito:", itemCarrito);

                mostrarFeedback(
                    "Carrito actualizado",
                    "Producto añadido",
                    "El producto se ha añadido correctamente al carrito."
                );

                refrescarContadorCarritoConEspera();

            } catch (error) {
                console.error("Error al añadir al carrito:", error);

                mostrarFeedback(
                    "Error",
                    "No se pudo añadir",
                    "Hubo un problema al añadir el producto al carrito.",
                    false
                );
            }
        };
    }

    if (btnFavorito) {
        btnFavorito.onclick = async () => {
            const sesion = await obtenerSesionActual();

            if (!sesion || !sesion.id) {
                mostrarModalLogin("Debes iniciar sesión para gestionar tus favoritos.");
                return;
            }

            try {
                if (esFavorito) {
                    const response = await fetch(
                        `${BASE_URL}/favoritos?usuarioId=${sesion.id}&productoId=${productoId}`,
                        {
                            method: "DELETE",
                            credentials: "include"
                        }
                    );

                    if (!response.ok) {
                        throw new Error("No se pudo eliminar de favoritos");
                    }

                    esFavorito = false;
                    actualizarBotonFavorito();

                    mostrarFeedback(
                        "Favoritos actualizado",
                        "Producto eliminado",
                        "El producto se ha quitado de tus favoritos."
                    );

                } else {
                    const response = await fetch(
                        `${BASE_URL}/favoritos?usuarioId=${sesion.id}&productoId=${productoId}`,
                        {
                            method: "POST",
                            credentials: "include"
                        }
                    );

                    if (!response.ok) {
                        throw new Error("No se pudo añadir a favoritos");
                    }

                    esFavorito = true;
                    actualizarBotonFavorito();

                    mostrarFeedback(
                        "Favoritos actualizado",
                        "Producto guardado",
                        "El producto se ha añadido correctamente a tus favoritos."
                    );
                }

            } catch (error) {
                console.error("Error al actualizar favoritos:", error);

                mostrarFeedback(
                    "Error",
                    "No se pudo actualizar",
                    "Hubo un problema al actualizar tus favoritos.",
                    false
                );
            }
        };
    }
}

cargarProducto();
