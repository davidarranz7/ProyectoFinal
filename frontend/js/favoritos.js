document.addEventListener("DOMContentLoaded", () => {
    cargarFavoritos();
});

async function obtenerSesionActual() {
    try {
        const response = await fetch(`${BASE_URL}/auth/session`, {
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

async function cargarFavoritos() {
    const contenedor = document.getElementById("lista-favoritos");

    if (!contenedor) return;

    const sesion = await obtenerSesionActual();

    if (!sesion || !sesion.id) {
        contenedor.innerHTML = "<p>Debes iniciar sesión para ver tus favoritos.</p>";
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
                        `${BASE_URL}/favoritos?usuarioId=${sesion.id}&productoId=${producto.id}`,
                        {
                            method: "DELETE",
                            credentials: "include"
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