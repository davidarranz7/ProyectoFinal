fetch("menu.html")
    .then(response => response.text())
    .then(data => {
        const temp = document.createElement("div");
        temp.innerHTML = data;

        const nav = temp.querySelector(".navbar");
        const authOverlay = temp.querySelector("#auth-overlay");
        const overlayCarrito = temp.querySelector("#overlay-carrito");
        const carritoLateral = temp.querySelector("#carrito-lateral");

        const menuContainer = document.getElementById("menu-container");

        if (menuContainer && nav) {
            menuContainer.innerHTML = "";
            menuContainer.appendChild(nav);
        }

        if (authOverlay && !document.getElementById("auth-overlay")) {
            document.body.appendChild(authOverlay);
        }

        if (overlayCarrito && !document.getElementById("overlay-carrito")) {
            document.body.appendChild(overlayCarrito);
        }

        if (carritoLateral && !document.getElementById("carrito-lateral")) {
            document.body.appendChild(carritoLateral);
        }

        inicializarMenu();
    })
    .catch(error => console.error("Error al cargar el menú:", error));

function inicializarMenu() {
    const loginLink = document.getElementById("login-link");
    const profileMenu = document.getElementById("profile-menu");
    const profileName = document.getElementById("profile-name");
    const profileBtn = document.getElementById("profile-btn");
    const profileDropdown = document.getElementById("profile-dropdown");
    const logoutBtn = document.getElementById("logout-btn");

    const authOverlay = document.getElementById("auth-overlay");
    const authBackdrop = document.getElementById("auth-backdrop");
    const authClose = document.getElementById("auth-close");
    const authFrame = document.getElementById("auth-frame");

    const btnAbrirCarritoMenu = document.getElementById("btn-carrito-menu");
    const carritoLateral = document.getElementById("carrito-lateral");
    const overlayCarrito = document.getElementById("overlay-carrito");
    const btnCerrarCarrito = document.getElementById("cerrar-carrito");
    const btnIrCarrito = document.getElementById("btn-ir-carrito");

    const buscadorGlobalMenu = document.getElementById("buscador-global-menu");

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

    async function actualizarEstadoUsuario() {
        const sesion = await obtenerSesionActual();

        if (sesion && sesion.nombre) {
            if (loginLink) loginLink.style.display = "none";
            if (profileMenu) profileMenu.style.display = "block";
            if (profileName) profileName.textContent = sesion.nombre;
            if (btnAbrirCarritoMenu) btnAbrirCarritoMenu.style.display = "inline-flex";
        } else {
            if (loginLink) loginLink.style.display = "inline-flex";
            if (profileMenu) profileMenu.style.display = "none";
            if (btnAbrirCarritoMenu) btnAbrirCarritoMenu.style.display = "none";
        }
    }

    function ejecutarBusquedaGlobal() {
        const textoBusqueda = buscadorGlobalMenu?.value.trim();

        if (!textoBusqueda) {
            return;
        }

        const busquedaCodificada = encodeURIComponent(textoBusqueda);
        window.location.href = `resultadosBusqueda.html?busqueda=${busquedaCodificada}`;
    }

    function abrirLogin() {
        if (authFrame) authFrame.src = "login.html";
        if (authOverlay) authOverlay.classList.add("activo");
        document.body.style.overflow = "hidden";
    }

    function abrirRegistro() {
        if (authFrame) authFrame.src = "registro.html";
        if (authOverlay) authOverlay.classList.add("activo");
        document.body.style.overflow = "hidden";
    }

    function cerrarOverlay() {
        if (authOverlay) authOverlay.classList.remove("activo");
        if (authFrame) authFrame.src = "";
        document.body.style.overflow = "";
    }

    async function actualizarContadorCarrito() {
        const contador = document.getElementById("contador-carrito");

        if (!contador) return;

        const sesion = await obtenerSesionActual();

        if (!sesion || !sesion.id) {
            contador.style.display = "none";
            contador.textContent = "0";
            return;
        }

        try {
            const response = await fetch(`${BASE_URL}/carrito/usuario/${sesion.id}`, {
                method: "GET",
                credentials: "include"
            });

            if (!response.ok) {
                throw new Error("No se pudo cargar el contador del carrito");
            }

            const items = await response.json();
            const totalUnidades = items.reduce((acc, item) => acc + item.cantidad, 0);

            if (totalUnidades > 0) {
                contador.textContent = totalUnidades;
                contador.style.display = "block";
            } else {
                contador.textContent = "0";
                contador.style.display = "none";
            }
        } catch (error) {
            console.error("Error al actualizar contador del carrito:", error);
            contador.style.display = "none";
            contador.textContent = "0";
        }
    }

    async function cargarMiniCarrito() {
        const contenedorItems = document.getElementById("carrito-items");
        const totalElemento = document.getElementById("carrito-total");

        if (!contenedorItems || !totalElemento) return;

        const sesion = await obtenerSesionActual();

        if (!sesion || !sesion.id) {
            contenedorItems.innerHTML = `<p class="carrito-vacio">Inicia sesión para ver tu carrito.</p>`;
            totalElemento.textContent = "0 €";
            actualizarContadorCarrito();
            return;
        }

        try {
            const [itemsResponse, totalResponse] = await Promise.all([
                fetch(`${BASE_URL}/carrito/usuario/${sesion.id}`, {
                    method: "GET",
                    credentials: "include"
                }),
                fetch(`${BASE_URL}/carrito/total/${sesion.id}`, {
                    method: "GET",
                    credentials: "include"
                })
            ]);

            if (!itemsResponse.ok || !totalResponse.ok) {
                throw new Error("No se pudo cargar el carrito");
            }

            const items = await itemsResponse.json();
            const total = await totalResponse.json();

            if (!items || items.length === 0) {
                contenedorItems.innerHTML = `<p class="carrito-vacio">Tu carrito está vacío.</p>`;
                totalElemento.textContent = "0 €";
                actualizarContadorCarrito();
                return;
            }

            contenedorItems.innerHTML = "";

            items.forEach(item => {
                const producto = item.producto;
                const subtotal = Number(producto.precio) * Number(item.cantidad);

                const itemHtml = document.createElement("article");
                itemHtml.className = "carrito-item";
                itemHtml.innerHTML = `
                    <img class="carrito-item-img" src="${producto.urlImagen}" alt="${producto.nombre}">
                    <div class="carrito-item-info">
                        <h3 class="carrito-item-nombre">${producto.nombre}</h3>
                        <p class="carrito-item-detalle">Talla: ${item.talla}</p>

                        <div class="carrito-cantidad-fila">
                            <button class="btn-cantidad btn-restar" type="button">-</button>
                            <span class="carrito-cantidad-valor">${item.cantidad}</span>
                            <button class="btn-cantidad btn-sumar" type="button">+</button>
                        </div>

                        <p class="carrito-item-precio">${subtotal.toFixed(2)} €</p>

                        <button class="carrito-item-eliminar" type="button" aria-label="Eliminar producto del carrito" title="Eliminar">
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path d="M3 6h18"></path>
                                <path d="M8 6V4h8v2"></path>
                                <path d="M19 6l-1 14H6L5 6"></path>
                                <path d="M10 11v6"></path>
                                <path d="M14 11v6"></path>
                            </svg>
                        </button>
                    </div>
                `;

                const btnEliminar = itemHtml.querySelector(".carrito-item-eliminar");
                const btnRestar = itemHtml.querySelector(".btn-restar");
                const btnSumar = itemHtml.querySelector(".btn-sumar");

                btnEliminar.addEventListener("click", async () => {
                    try {
                        const response = await fetch(
                            `${BASE_URL}/carrito/eliminar?usuarioId=${sesion.id}&productoId=${producto.id}&talla=${item.talla}`,
                            {
                                method: "DELETE",
                                credentials: "include"
                            }
                        );

                        if (!response.ok) {
                            throw new Error("No se pudo eliminar el producto");
                        }

                        await cargarMiniCarrito();
                        await actualizarContadorCarrito();
                    } catch (error) {
                        console.error("Error al eliminar producto del carrito:", error);
                    }
                });

                btnSumar.addEventListener("click", async () => {
                    try {
                        const response = await fetch(
                            `${BASE_URL}/carrito/actualizar-cantidad?usuarioId=${sesion.id}&productoId=${producto.id}&talla=${item.talla}&nuevaCantidad=${item.cantidad + 1}`,
                            {
                                method: "PUT",
                                credentials: "include"
                            }
                        );

                        if (!response.ok) {
                            throw new Error("No se pudo aumentar la cantidad");
                        }

                        await cargarMiniCarrito();
                        await actualizarContadorCarrito();
                    } catch (error) {
                        console.error("Error al aumentar cantidad:", error);
                    }
                });

                btnRestar.addEventListener("click", async () => {
                    try {
                        const response = await fetch(
                            `${BASE_URL}/carrito/actualizar-cantidad?usuarioId=${sesion.id}&productoId=${producto.id}&talla=${item.talla}&nuevaCantidad=${item.cantidad - 1}`,
                            {
                                method: "PUT",
                                credentials: "include"
                            }
                        );

                        if (!response.ok) {
                            throw new Error("No se pudo reducir la cantidad");
                        }

                        await cargarMiniCarrito();
                        await actualizarContadorCarrito();
                    } catch (error) {
                        console.error("Error al reducir cantidad:", error);
                    }
                });

                contenedorItems.appendChild(itemHtml);
            });

            totalElemento.textContent = `${Number(total).toFixed(2)} €`;
            actualizarContadorCarrito();
        } catch (error) {
            console.error("Error al cargar mini carrito:", error);
            contenedorItems.innerHTML = `<p class="carrito-vacio">No se pudo cargar el carrito.</p>`;
            totalElemento.textContent = "0 €";
            actualizarContadorCarrito();
        }
    }

    function abrirCarrito() {
        if (carritoLateral) carritoLateral.classList.add("activo");
        if (overlayCarrito) overlayCarrito.classList.add("activo");
        document.body.style.overflow = "hidden";

        if (profileDropdown) {
            profileDropdown.classList.remove("activo");
        }

        cargarMiniCarrito();
    }

    function cerrarCarrito() {
        if (carritoLateral) carritoLateral.classList.remove("activo");
        if (overlayCarrito) overlayCarrito.classList.remove("activo");

        const authAbierto = authOverlay && authOverlay.classList.contains("activo");
        if (!authAbierto) {
            document.body.style.overflow = "";
        }
    }

    async function cerrarSesion() {
        try {
            await fetch(`${BASE_URL}/auth/logout`, {
                method: "POST",
                credentials: "include"
            });
        } catch (error) {
            console.error("Error al cerrar sesión:", error);
        }

        await actualizarEstadoUsuario();
        await actualizarContadorCarrito();
        cerrarOverlay();
        cerrarCarrito();
        window.location.href = "index.html";
    }

    actualizarEstadoUsuario();
    actualizarContadorCarrito();

    if (buscadorGlobalMenu) {
        buscadorGlobalMenu.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                ejecutarBusquedaGlobal();
            }
        });
    }

    if (loginLink) {
        loginLink.addEventListener("click", abrirLogin);
    }

    if (authClose) {
        authClose.addEventListener("click", cerrarOverlay);
    }

    if (authBackdrop) {
        authBackdrop.addEventListener("click", cerrarOverlay);
    }

    if (profileBtn) {
        profileBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            if (profileDropdown) {
                profileDropdown.classList.toggle("activo");
            }
        });
    }

    if (btnAbrirCarritoMenu) {
        btnAbrirCarritoMenu.addEventListener("click", abrirCarrito);
    }

    if (btnCerrarCarrito) {
        btnCerrarCarrito.addEventListener("click", cerrarCarrito);
    }

    if (overlayCarrito) {
        overlayCarrito.addEventListener("click", cerrarCarrito);
    }

    if (btnIrCarrito) {
        btnIrCarrito.addEventListener("click", () => {
            window.location.href = "carrito.html";
        });
    }

    document.addEventListener("click", (e) => {
        if (profileMenu && !profileMenu.contains(e.target)) {
            if (profileDropdown) {
                profileDropdown.classList.remove("activo");
            }
        }
    });

    if (logoutBtn) {
        logoutBtn.addEventListener("click", async (e) => {
            e.preventDefault();
            await cerrarSesion();
        });
    }

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            if (authOverlay && authOverlay.classList.contains("activo")) {
                cerrarOverlay();
            }

            if (carritoLateral && carritoLateral.classList.contains("activo")) {
                cerrarCarrito();
            }
        }
    });

    window.abrirRegistro = abrirRegistro;
    window.abrirLogin = abrirLogin;
    window.cerrarAuthOverlay = cerrarOverlay;
    window.actualizarMenuUsuario = actualizarEstadoUsuario;
    window.abrirCarritoLateral = abrirCarrito;
    window.cerrarCarritoLateral = cerrarCarrito;
    window.cargarMiniCarrito = cargarMiniCarrito;
    window.actualizarContadorCarrito = actualizarContadorCarrito;
}