const params = new URLSearchParams(window.location.search);
const productoId = params.get("id");

const btnVolver = document.getElementById("btn-volver");
const btnCarrito = document.getElementById("btn-carrito");
const btnFavorito = document.getElementById("btn-favorito");
const tallasLista = document.getElementById("tallas-lista");

let tallaSeleccionada = null;
let productoActual = null;
let esFavorito = false;
let sesionActual = null;

if (btnVolver) {
    btnVolver.addEventListener("click", () => {
        if (window.history.length > 1) {
            window.history.back();
        } else {
            window.location.href = "zara.html";
        }
    });
}

const mensajeTalla = document.createElement("p");
mensajeTalla.id = "mensaje-talla";
mensajeTalla.classList.add("mensaje-validacion", "oculto");
mensajeTalla.textContent = "Selecciona una talla antes de continuar.";

if (tallasLista) {
    tallasLista.after(mensajeTalla);
}

function mostrarToast(mensaje = "Acción realizada") {
    const toast = document.getElementById("toast-carrito");
    if (!toast) return;

    toast.textContent = mensaje;
    toast.classList.add("activo");

    clearTimeout(toast._timeoutId);

    toast._timeoutId = setTimeout(() => {
        toast.classList.remove("activo");
    }, 2200);
}

function ocultarMensajeValidacion() {
    mensajeTalla.classList.add("oculto");
    mensajeTalla.classList.remove("mensaje-exito");
}

function mostrarMensajeError(texto) {
    mensajeTalla.textContent = texto;
    mensajeTalla.classList.remove("oculto");
    mensajeTalla.classList.remove("mensaje-exito");
}

function mostrarMensajeExito(texto) {
    mensajeTalla.textContent = texto;
    mensajeTalla.classList.remove("oculto");
    mensajeTalla.classList.add("mensaje-exito");
}

function refrescarContadorCarritoConEspera() {
    setTimeout(() => {
        if (typeof window.actualizarContadorCarrito === "function") {
            window.actualizarContadorCarrito();
        }
    }, 150);
}

function actualizarBotonFavorito() {
    if (!btnFavorito) return;

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
        const response = await fetch("http://localhost:8080/auth/session", {
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
        const response = await fetch(`http://localhost:8080/favoritos/usuario/${sesion.id}`, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar favoritos");
        }

        const favoritos = await response.json();

        esFavorito = Array.isArray(favoritos) && favoritos.some(fav =>
            fav.producto && Number(fav.producto.id) === Number(productoId)
        );

        actualizarBotonFavorito();
    } catch (error) {
        console.error("Error al comprobar favoritos:", error);
        esFavorito = false;
        actualizarBotonFavorito();
    }
}

function renderizarProducto(producto) {
    document.getElementById("producto-imagen").src = producto.urlImagen;
    document.getElementById("producto-imagen").alt = producto.nombre;
    document.getElementById("producto-nombre").textContent = producto.nombre;
    document.getElementById("producto-precio").textContent = producto.precio + " €";
    document.getElementById("producto-descripcion").textContent = producto.descripcion || "Sin descripción";
    document.getElementById("producto-tienda").textContent = producto.tienda?.nombre || "Tienda";
    document.getElementById("producto-categoria").textContent = producto.categoria?.nombre || "Categoría";
    document.getElementById("btn-tienda-original").href = producto.urlProducto;
}

function renderizarTallas(tallas) {
    tallasLista.innerHTML = "";

    tallas.forEach(item => {
        const boton = document.createElement("button");
        boton.type = "button";
        boton.textContent = item.talla;
        boton.classList.add("talla-btn");

        if (item.stock > 0) {
            boton.classList.add("disponible");

            if (item.stock <= 5) {
                boton.classList.add("poco-stock");
                boton.title = "Pocas unidades";
            }

            boton.addEventListener("click", () => {
                const yaSeleccionada = boton.classList.contains("seleccionada");

                document.querySelectorAll(".talla-btn").forEach(btn => {
                    btn.classList.remove("seleccionada");
                });

                if (yaSeleccionada) {
                    tallaSeleccionada = null;
                } else {
                    boton.classList.add("seleccionada");
                    tallaSeleccionada = item.talla;
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
        console.error("No se encontró id de producto en la URL");
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/productos/${productoId}`);

        if (!response.ok) {
            throw new Error("No se pudo cargar el producto");
        }

        const producto = await response.json();
        productoActual = producto;

        console.log("Producto:", producto);

        renderizarProducto(producto);
        await comprobarSiEsFavorito();
        configurarEventosProducto();

        await cargarTallas();
    } catch (error) {
        console.error("Error al cargar producto:", error);
    }
}

async function cargarTallas() {
    try {
        const response = await fetch(`http://localhost:8080/productos/${productoId}/talla-stock`);

        if (!response.ok) {
            throw new Error("No se pudieron cargar las tallas");
        }

        const tallas = await response.json();
        console.log("Tallas:", tallas);

        renderizarTallas(tallas);
    } catch (error) {
        console.error("Error al cargar tallas:", error);
    }
}

function configurarEventosProducto() {
    if (btnCarrito) {
        btnCarrito.onclick = async () => {
            const sesion = await obtenerSesionActual();

            if (!sesion || !sesion.id) {
                mostrarMensajeError("Debes iniciar sesión para añadir productos al carrito.");
                return;
            }

            if (!tallaSeleccionada) {
                mostrarMensajeError("Selecciona una talla antes de continuar.");
                return;
            }

            try {
                const response = await fetch(
                    `http://localhost:8080/carrito/agregar?usuarioId=${sesion.id}&productoId=${productoId}&talla=${tallaSeleccionada}&cantidad=1`,
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

                mostrarMensajeExito("Producto añadido al carrito correctamente.");
                mostrarToast("Añadido al carrito");
                refrescarContadorCarritoConEspera();
            } catch (error) {
                console.error("Error al añadir al carrito:", error);
                mostrarMensajeError("Hubo un error al añadir el producto al carrito.");
            }
        };
    }

    if (btnFavorito) {
        btnFavorito.onclick = async () => {
            const sesion = await obtenerSesionActual();

            if (!sesion || !sesion.id) {
                mostrarMensajeError("Debes iniciar sesión para gestionar favoritos.");
                return;
            }

            try {
                if (esFavorito) {
                    const response = await fetch(
                        `http://localhost:8080/favoritos?usuarioId=${sesion.id}&productoId=${productoId}`,
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
                    mostrarMensajeExito("Producto eliminado de favoritos.");
                    mostrarToast("Eliminado de favoritos");
                } else {
                    const response = await fetch(
                        `http://localhost:8080/favoritos?usuarioId=${sesion.id}&productoId=${productoId}`,
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
                    mostrarMensajeExito("Producto añadido a favoritos.");
                    mostrarToast("Añadido a favoritos");
                }
            } catch (error) {
                console.error("Error al actualizar favoritos:", error);
                mostrarMensajeError("Hubo un error al actualizar favoritos.");
            }
        };
    }
}

cargarProducto();