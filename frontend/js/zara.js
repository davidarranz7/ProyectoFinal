function mezclarArray(array) {
    return array.sort(() => Math.random() - 0.5);
}

let productosZara = [];
let favoritosIds = new Set();

let indiceActual = 0;
const PRODUCTOS_POR_CARGA = 8;

const modal = document.getElementById("modal-login");
const cerrarModal = document.getElementById("cerrar-modal");
const cerrarModalSecundario = document.getElementById("cerrar-modal-secundario");
const modalMensaje = document.getElementById("modal-mensaje");
const abrirLoginModal = document.getElementById("abrir-login-modal");

function mostrarToastCarrito(mensaje = "Añadido al carrito") {
    const toast = document.getElementById("toast-carrito");
    if (!toast) return;

    toast.textContent = mensaje;
    toast.classList.add("activo");

    clearTimeout(toast._timeoutId);

    toast._timeoutId = setTimeout(() => {
        toast.classList.remove("activo");
    }, 2200);
}

function mostrarToastFavorito(mensaje = "Favorito actualizado") {
    const toast = document.getElementById("toast-carrito");
    if (!toast) return;

    toast.textContent = mensaje;
    toast.classList.add("activo");

    clearTimeout(toast._timeoutId);

    toast._timeoutId = setTimeout(() => {
        toast.classList.remove("activo");
    }, 2200);
}

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
        cerrarTodosLosMenusTalla();
    }
});

abrirLoginModal.addEventListener("click", () => {
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

async function cargarFavoritosUsuario() {
    const usuarioLogueado = localStorage.getItem("usuarioLogueado");
    const usuarioId = localStorage.getItem("usuarioId");

    favoritosIds = new Set();

    if (usuarioLogueado !== "true" || !usuarioId) {
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/favoritos/usuario/${usuarioId}`);

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
        btnFav.style.color = "red";
    } else {
        btnFav.classList.remove("activo");
        btnFav.style.color = "";
    }
}

function renderizarProductos(productos, reiniciar = true) {
    const grid = document.getElementById("grid-productos");
    const contador = document.getElementById("contador-productos");

    if (reiniciar) {
        grid.innerHTML = "";
        indiceActual = 0;
    }

    contador.textContent = `NEW COLLECTION / ${productos.length} PRODUCTOS`;

    if (productos.length === 0) {
        grid.innerHTML = `<p class="sin-resultados">No hay productos que coincidan con los filtros seleccionados.</p>`;
        return;
    }

    const siguientes = productos.slice(indiceActual, indiceActual + PRODUCTOS_POR_CARGA);

    siguientes.forEach(producto => {
        const card = document.createElement("article");
        card.className = "tarjeta-zara";

        card.innerHTML = `
            <div class="img-wrapper">
                <button class="btn-fav" type="button">❤</button>

                <a href="fichaProducto.html?id=${producto.id}">
                    <img src="${producto.urlImagen}" alt="${producto.nombre}" loading="lazy">
                </a>
            </div>

            <div class="info-producto">
                <a href="fichaProducto.html?id=${producto.id}" class="link-producto">
                    <h3 class="nombre-prenda">${producto.nombre}</h3>
                </a>

                <div class="producto-footer">
                    <p class="p-final">${producto.precio} €</p>
                    <button class="btn-carrito" type="button">Añadir</button>
                </div>

                <div class="mini-menu-talla">
                    <div class="lista-tallas"></div>
                    <p class="mensaje-stock">Selecciona una talla</p>
                    <button class="btn-confirmar-carrito" type="button">Confirmar</button>
                </div>
            </div>
        `;

        grid.appendChild(card);

        const btnFav = card.querySelector(".btn-fav");
        const btnCarrito = card.querySelector(".btn-carrito");
        const miniMenuTalla = card.querySelector(".mini-menu-talla");
        const listaTallas = card.querySelector(".lista-tallas");
        const mensajeStock = card.querySelector(".mensaje-stock");
        const btnConfirmarCarrito = card.querySelector(".btn-confirmar-carrito");

        let tallaSeleccionada = null;

        actualizarEstadoVisualFavorito(btnFav, producto.id);

        if (producto.tallaStocks && producto.tallaStocks.length > 0) {
            producto.tallaStocks.forEach(tallaStock => {
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
                    if (tallaStock.stock <= 0) {
                        mensajeStock.textContent = "Agotado";
                    } else if (tallaStock.stock <= 5) {
                        mensajeStock.textContent = "Pocas unidades";
                    } else {
                        mensajeStock.textContent = "Disponible";
                    }
                });

                botonTalla.addEventListener("mouseleave", () => {
                    const botonSeleccionado = listaTallas.querySelector(".btn-talla.seleccionada");

                    if (!botonSeleccionado) {
                        mensajeStock.textContent = "Selecciona una talla";
                        return;
                    }

                    const stockSeleccionado = Number(botonSeleccionado.dataset.stock);

                    if (stockSeleccionado <= 0) {
                        mensajeStock.textContent = "Agotado";
                    } else if (stockSeleccionado <= 5) {
                        mensajeStock.textContent = "Pocas unidades";
                    } else {
                        mensajeStock.textContent = "Disponible";
                    }
                });

                if (tallaStock.stock > 0) {
                    botonTalla.addEventListener("click", () => {
                        const yaSeleccionada = botonTalla.classList.contains("seleccionada");

                        listaTallas.querySelectorAll(".btn-talla").forEach(btn => {
                            btn.classList.remove("seleccionada");
                        });

                        if (yaSeleccionada) {
                            tallaSeleccionada = null;
                            mensajeStock.textContent = "Selecciona una talla";
                        } else {
                            botonTalla.classList.add("seleccionada");
                            tallaSeleccionada = tallaStock.talla;

                            if (tallaStock.stock <= 5) {
                                mensajeStock.textContent = "Pocas unidades";
                            } else {
                                mensajeStock.textContent = "Disponible";
                            }
                        }
                    });
                }

                listaTallas.appendChild(botonTalla);
            });
        } else {
            mensajeStock.textContent = "No hay tallas disponibles";
        }

        miniMenuTalla.addEventListener("click", (event) => {
            event.stopPropagation();
        });

        btnFav.addEventListener("click", async (event) => {
            event.preventDefault();
            event.stopPropagation();

            const usuarioLogueado = localStorage.getItem("usuarioLogueado");
            const usuarioId = localStorage.getItem("usuarioId");

            if (usuarioLogueado !== "true") {
                modalMensaje.textContent = "Debes iniciar sesión para añadir productos a favoritos.";
                modal.style.display = "flex";
                return;
            }

            if (!usuarioId) {
                modalMensaje.textContent = "No se encontró el usuario logueado.";
                modal.style.display = "flex";
                return;
            }

            try {
                const yaEsFavorito = favoritosIds.has(Number(producto.id));

                if (yaEsFavorito) {
                    const response = await fetch(
                        `http://localhost:8080/favoritos?usuarioId=${usuarioId}&productoId=${producto.id}`,
                        {
                            method: "DELETE"
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
                        `http://localhost:8080/favoritos?usuarioId=${usuarioId}&productoId=${producto.id}`,
                        {
                            method: "POST"
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
            }
        });

        btnCarrito.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();

            if (localStorage.getItem("usuarioLogueado") !== "true") {
                modalMensaje.textContent = "Debes iniciar sesión para añadir productos al carrito.";
                modal.style.display = "flex";
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

            const usuarioId = localStorage.getItem("usuarioId");

            if (!usuarioId) {
                mensajeStock.textContent = "Debes iniciar sesión";
                return;
            }

            if (!tallaSeleccionada) {
                mensajeStock.textContent = "Selecciona una talla";
                return;
            }

            try {
                const response = await fetch(
                    `http://localhost:8080/carrito/agregar?usuarioId=${usuarioId}&productoId=${producto.id}&talla=${tallaSeleccionada}&cantidad=1`,
                    {
                        method: "POST"
                    }
                );

                if (!response.ok) {
                    throw new Error("No se pudo añadir al carrito");
                }

                miniMenuTalla.classList.remove("activo");
                listaTallas.querySelectorAll(".btn-talla").forEach(btn => {
                    btn.classList.remove("seleccionada");
                });
                tallaSeleccionada = null;
                mensajeStock.textContent = "Selecciona una talla";
                mostrarToastCarrito("Añadido al carrito");

                if (typeof window.actualizarContadorCarrito === "function") {
                    window.actualizarContadorCarrito();
                }
            } catch (error) {
                console.error("Error al añadir al carrito:", error);
                mensajeStock.textContent = "Error al añadir";
            }
        });
    });

    indiceActual += PRODUCTOS_POR_CARGA;
}

function obtenerProductosFiltrados() {
    const hombreChecked = document.querySelector('input[data-tipo="genero"][value="hombre"]').checked;
    const mujerChecked = document.querySelector('input[data-tipo="genero"][value="mujer"]').checked;

    const categoriasSeleccionadas = Array.from(
        document.querySelectorAll('input[data-tipo="categoria"]:checked')
    ).map(c => c.value.toLowerCase());

    let filtrados = [...productosZara];

    if (hombreChecked && !mujerChecked) {
        filtrados = filtrados.filter(p =>
            p.seccion && p.seccion.toLowerCase().includes("hombre")
        );
    }

    if (mujerChecked && !hombreChecked) {
        filtrados = filtrados.filter(p =>
            p.seccion && p.seccion.toLowerCase().includes("mujer")
        );
    }

    if (categoriasSeleccionadas.length > 0) {
        filtrados = filtrados.filter(p =>
            p.categoria &&
            p.categoria.nombre &&
            categoriasSeleccionadas.some(cat =>
                p.categoria.nombre.toLowerCase().includes(cat)
            )
        );
    }

    return filtrados;
}

function aplicarFiltros() {
    cerrarTodosLosMenusTalla();
    const filtrados = obtenerProductosFiltrados();
    renderizarProductos(filtrados, true);
}

async function iniciarZara() {
    try {
        const [productosResponse] = await Promise.all([
            fetch("http://localhost:8080/productos/tienda/Zara"),
            cargarFavoritosUsuario()
        ]);

        if (!productosResponse.ok) {
            throw new Error("No se pudieron cargar los productos");
        }

        const productos = await productosResponse.json();

        productosZara = mezclarArray([...productos]);
        renderizarProductos(productosZara, true);

        document.querySelectorAll('input[data-tipo="genero"]').forEach(c =>
            c.addEventListener("change", aplicarFiltros)
        );

        document.querySelectorAll('input[data-tipo="categoria"]').forEach(c =>
            c.addEventListener("change", aplicarFiltros)
        );

        window.addEventListener("scroll", () => {
            const filtrados = obtenerProductosFiltrados();

            const cercaFinal =
                window.innerHeight + window.scrollY >= document.body.offsetHeight - 200;

            if (cercaFinal && indiceActual < filtrados.length) {
                renderizarProductos(filtrados, false);
            }
        });

    } catch (error) {
        console.error("Error:", error);
    }
}

iniciarZara();