function mezclarArray(array) {
    return array.sort(() => Math.random() - 0.5);
}

let productosZara = [];
let usuarioLogueado = localStorage.getItem("usuarioLogueado") === "true";

let indiceActual = 0;
const PRODUCTOS_POR_CARGA = 8;

const modal = document.getElementById("modal-login");
const cerrarModal = document.getElementById("cerrar-modal");
const cerrarModalSecundario = document.getElementById("cerrar-modal-secundario");
const modalMensaje = document.getElementById("modal-mensaje");


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
    }
});

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
            <a href="${producto.urlProducto}" target="_blank">
                <div class="img-wrapper">
                    <button class="btn-fav" type="button">❤</button>
                    <img src="${producto.urlImagen}" alt="${producto.nombre}" loading="lazy">
                </div>

                <div class="info-producto">
                    <h3 class="nombre-prenda">${producto.nombre}</h3>

                    <div class="producto-footer">
                        <p class="p-final">${producto.precio} €</p>
                        <button class="btn-carrito" type="button">Añadir</button>
                    </div>
                </div>
            </a>
        `;

        grid.appendChild(card);

        const btnFav = card.querySelector(".btn-fav")
        const btnCarrito = card.querySelector(".btn-carrito");

        btnFav.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();

            if (!usuarioLogueado) {
                modalMensaje.textContent = "Debes iniciar sesión para añadir productos a favoritos.";
                modal.style.display = "flex";
                return;
            }
        });

        btnCarrito.addEventListener("click", (event) => {
            event.preventDefault();
            event.stopPropagation();

            if (!usuarioLogueado) {
                modalMensaje.textContent = "Debes iniciar sesión para añadir productos al carrito.";
                modal.style.display = "flex";
                return;
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
    const filtrados = obtenerProductosFiltrados();
    renderizarProductos(filtrados, true);
}

fetch("http://localhost:8080/productos/tienda/Zara")
    .then(res => res.json())
    .then(productos => {
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
    })
    .catch(err => console.error("Error:", err));