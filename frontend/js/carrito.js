document.addEventListener("DOMContentLoaded", () => {
    const btnComprar = document.getElementById("btn-comprar");

    if (btnComprar) {
        btnComprar.addEventListener("click", () => {
            window.location.href = "checkout.html";
        });
    }

    cargarCarrito();
});

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

async function cargarCarrito() {
    const sesion = await obtenerSesionActual();

    const listaCarrito = document.getElementById("lista-carrito");
    const carritoVacio = document.getElementById("carrito-vacio");
    const subtotal = document.getElementById("subtotal");
    const total = document.getElementById("total");

    if (!sesion || !sesion.id) {
        mostrarCarritoVacio();
        return;
    }

    try {
        const url = `http://localhost:8080/carrito/usuario/${sesion.id}`;
        const response = await fetch(url, {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("Error al obtener carrito");
        }

        const items = await response.json();

        listaCarrito.innerHTML = "";

        if (!items || items.length === 0) {
            mostrarCarritoVacio();
            return;
        }

        carritoVacio.style.display = "none";

        let totalCalculado = 0;

        items.forEach(item => {
            const producto = item.producto;
            const cantidad = item.cantidad;
            const talla = item.talla;
            const precio = Number(producto.precio);
            const subtotalProducto = precio * cantidad;

            console.log("Producto carrito:", producto);
            console.log("Tallas carrito:", producto.tallaStocks);

            totalCalculado += subtotalProducto;

            const article = document.createElement("article");
            article.classList.add("item-carrito");

            article.innerHTML = `
                <img src="${producto.urlImagen}" alt="${producto.nombre}" class="item-imagen">

                <div class="item-info">
                    <h3 class="item-nombre">${producto.nombre}</h3>

                    <p class="item-talla">
                        <strong>Talla:</strong>
                        <select class="select-talla">
                            ${generarOpcionesTalla(producto.tallaStocks, talla)}
                        </select>
                    </p>

                    <p class="item-precio"><strong>Precio:</strong> ${precio.toFixed(2)} €</p>
                </div>

                <div class="item-acciones">
                    <div class="cantidad-control">
                        <button class="btn-restar">-</button>
                        <span>${cantidad}</span>
                        <button class="btn-sumar">+</button>
                    </div>
                    <button class="btn-eliminar">Eliminar</button>
                </div>
            `;

            const btnRestar = article.querySelector(".btn-restar");
            const btnSumar = article.querySelector(".btn-sumar");
            const btnEliminar = article.querySelector(".btn-eliminar");
            const selectTalla = article.querySelector(".select-talla");

            btnRestar.addEventListener("click", async () => {
                if (cantidad > 1) {
                    await actualizarCantidad(sesion.id, producto.id, talla, cantidad - 1);
                }
            });

            btnSumar.addEventListener("click", async () => {
                await actualizarCantidad(sesion.id, producto.id, talla, cantidad + 1);
            });

            btnEliminar.addEventListener("click", async () => {
                await eliminarProducto(sesion.id, producto.id, talla);
            });

            selectTalla.addEventListener("change", async (e) => {
                const nuevaTalla = e.target.value;

                if (nuevaTalla === talla) {
                    return;
                }

                await cambiarTalla(sesion.id, producto.id, talla, nuevaTalla);
            });

            listaCarrito.appendChild(article);
        });

        subtotal.textContent = `${totalCalculado.toFixed(2)} €`;
        total.textContent = `${totalCalculado.toFixed(2)} €`;

    } catch (error) {
        console.error("Error al cargar carrito:", error);
        mostrarCarritoVacio();
    }
}

async function actualizarCantidad(usuarioId, productoId, talla, nuevaCantidad) {
    try {
        const url = `http://localhost:8080/carrito/actualizar-cantidad?usuarioId=${usuarioId}&productoId=${productoId}&talla=${encodeURIComponent(talla)}&nuevaCantidad=${nuevaCantidad}`;

        const response = await fetch(url, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo actualizar la cantidad");
        }

        await cargarCarrito();

    } catch (error) {
        console.error("Error al actualizar cantidad:", error);
        alert("No se pudo actualizar la cantidad del producto");
    }
}

async function eliminarProducto(usuarioId, productoId, talla) {
    try {
        const url = `http://localhost:8080/carrito/eliminar?usuarioId=${usuarioId}&productoId=${productoId}&talla=${encodeURIComponent(talla)}`;

        const response = await fetch(url, {
            method: "DELETE",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo eliminar el producto");
        }

        await cargarCarrito();

    } catch (error) {
        console.error("Error al eliminar producto:", error);
        alert("No se pudo eliminar el producto");
    }
}

async function cambiarTalla(usuarioId, productoId, tallaActual, nuevaTalla) {
    try {
        const url = `http://localhost:8080/carrito/cambiar-talla?usuarioId=${usuarioId}&productoId=${productoId}&tallaActual=${encodeURIComponent(tallaActual)}&nuevaTalla=${encodeURIComponent(nuevaTalla)}`;

        const response = await fetch(url, {
            method: "PUT",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudo cambiar la talla");
        }

        await cargarCarrito();

    } catch (error) {
        console.error("Error al cambiar talla:", error);
        alert("No se pudo cambiar la talla. Puede que no haya stock disponible.");
        await cargarCarrito();
    }
}

function generarOpcionesTalla(tallaStocks, tallaSeleccionada) {
    if (!tallaStocks || tallaStocks.length === 0) {
        return `<option selected>${tallaSeleccionada}</option>`;
    }

    return tallaStocks.map(ts => {
        const selected = ts.talla === tallaSeleccionada ? "selected" : "";
        const disabled = ts.stock <= 0 ? "disabled" : "";
        const texto = ts.stock <= 0 ? `${ts.talla} (sin stock)` : ts.talla;

        return `<option value="${ts.talla}" ${selected} ${disabled}>${texto}</option>`;
    }).join("");
}

function mostrarCarritoVacio() {
    document.getElementById("lista-carrito").innerHTML = "";
    document.getElementById("carrito-vacio").style.display = "block";
    document.getElementById("subtotal").textContent = "0.00 €";
    document.getElementById("total").textContent = "0.00 €";
}