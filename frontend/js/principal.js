document.addEventListener("DOMContentLoaded", cargarProductosPopulares);

function cargarProductosPopulares() {
    fetch(`${BASE_URL}/productos/populares`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Error al cargar los productos populares");
            }
            return response.json();
        })
        .then(productos => {
            renderizarProductosPopulares(productos);
        })
        .catch(error => {
            console.error("Error:", error);
            mostrarMensajeSinProductos();
        });
}

function renderizarProductosPopulares(productos) {
    const contenedor = document.getElementById("productos-populares");

    if (!contenedor) return;

    contenedor.innerHTML = "";

    if (!productos || productos.length === 0) {
        mostrarMensajeSinProductos();
        return;
    }

    productos.forEach(producto => {
        const tarjeta = document.createElement("article");
        tarjeta.classList.add("tarjeta-home");

        tarjeta.innerHTML = `
            <a href="fichaProducto.html?id=${producto.id}">
                <div class="img-wrapper">
                    <img src="${producto.urlImagen}" alt="${producto.nombre}">
                </div>
                <div class="info-producto">
                    <span class="marca-tag">${producto.tienda?.nombre || "TIENDA"}</span>
                    <h3 class="nombre-prenda">${producto.nombre}</h3>
                    <div class="precios">
                        <span class="p-final">${formatearPrecio(producto.precio)}</span>
                    </div>
                </div>
            </a>
        `;

        contenedor.appendChild(tarjeta);
    });
}

function mostrarMensajeSinProductos() {
    const contenedor = document.getElementById("productos-populares");

    if (!contenedor) return;

    contenedor.innerHTML = `
        <p class="mensaje-vacio">Todavía no hay productos populares para mostrar.</p>
    `;
}

function formatearPrecio(precio) {
    if (precio == null) return "";

    return Number(precio).toFixed(2).replace(".", ",") + " €";
}