document.addEventListener("DOMContentLoaded", () => {
    cargarFavoritos();
});

async function cargarFavoritos() {
    const usuarioId = localStorage.getItem("usuarioId");
    const contenedor = document.getElementById("lista-favoritos");

    if (!contenedor) return;

    if (!usuarioId) {
        contenedor.innerHTML = "<p>Debes iniciar sesión para ver tus favoritos.</p>";
        return;
    }

    try {
        const response = await fetch(`http://localhost:8080/favoritos/usuario/${usuarioId}`);

        if (!response.ok) {
            throw new Error("No se pudieron cargar los favoritos");
        }

        const favoritos = await response.json();

        contenedor.innerHTML = "";

        if (!Array.isArray(favoritos) || favoritos.length === 0) {
            contenedor.innerHTML = "<p>No tienes productos en favoritos.</p>";
            return;
        }

        favoritos.forEach(fav => {
            const producto = fav.producto;

            const card = document.createElement("article");
            card.classList.add("favorito-card");

            card.innerHTML = `
                <img src="${producto.urlImagen}" alt="${producto.nombre}">
                <div class="favorito-info">
                    <h3>${producto.nombre}</h3>
                    <p>${producto.precio} €</p>
                    <button class="btn-eliminar" type="button">Eliminar</button>
                </div>
            `;

            card.addEventListener("click", () => {
                window.location.href = `fichaProducto.html?id=${producto.id}`;
            });

            const btnEliminar = card.querySelector(".btn-eliminar");

            btnEliminar.addEventListener("click", async (event) => {
                event.stopPropagation();

                try {
                    const responseEliminar = await fetch(
                        `http://localhost:8080/favoritos?usuarioId=${usuarioId}&productoId=${producto.id}`,
                        {
                            method: "DELETE"
                        }
                    );

                    if (!responseEliminar.ok) {
                        throw new Error("No se pudo eliminar el favorito");
                    }

                    await cargarFavoritos();

                } catch (error) {
                    console.error("Error al eliminar favorito:", error);
                }
            });

            contenedor.appendChild(card);
        });

    } catch (error) {
        console.error("Error al cargar favoritos:", error);
        contenedor.innerHTML = "<p>Error al cargar favoritos.</p>";
    }
}