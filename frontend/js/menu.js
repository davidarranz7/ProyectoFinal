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
    const btnAbrirCarritoDropdown = document.getElementById("abrir-carrito-dropdown");
    const carritoLateral = document.getElementById("carrito-lateral");
    const overlayCarrito = document.getElementById("overlay-carrito");
    const btnCerrarCarrito = document.getElementById("cerrar-carrito");
    const btnIrCarrito = document.getElementById("btn-ir-carrito");

    function actualizarEstadoUsuario() {
        const usuarioLogueado = sessionStorage.getItem("usuarioLogueado") === "true";
        const nombreUsuario = sessionStorage.getItem("nombreUsuario");

        if (usuarioLogueado && nombreUsuario) {
            if (loginLink) loginLink.style.display = "none";
            if (profileMenu) profileMenu.style.display = "block";
            if (profileName) profileName.textContent = nombreUsuario;
        } else {
            if (loginLink) loginLink.style.display = "inline-flex";
            if (profileMenu) profileMenu.style.display = "none";
        }
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

    async function cargarMiniCarrito() {
        const usuarioId = sessionStorage.getItem("usuarioId");
        const contenedorItems = document.getElementById("carrito-items");
        const totalElemento = document.getElementById("carrito-total");

        if (!contenedorItems || !totalElemento) return;

        if (!usuarioId) {
            contenedorItems.innerHTML = `<p class="carrito-vacio">Inicia sesión para ver tu carrito.</p>`;
            totalElemento.textContent = "0 €";
            return;
        }

        try {
            const [itemsResponse, totalResponse] = await Promise.all([
                fetch(`http://localhost:8080/carrito/usuario/${usuarioId}`),
                fetch(`http://localhost:8080/carrito/total/${usuarioId}`)
            ]);

            if (!itemsResponse.ok || !totalResponse.ok) {
                throw new Error("No se pudo cargar el carrito");
            }

            const items = await itemsResponse.json();
            const total = await totalResponse.json();

            if (!items || items.length === 0) {
                contenedorItems.innerHTML = `<p class="carrito-vacio">Tu carrito está vacío.</p>`;
                totalElemento.textContent = "0 €";
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
                        <p class="carrito-item-detalle">Cantidad: ${item.cantidad}</p>
                        <p class="carrito-item-precio">${subtotal.toFixed(2)} €</p>
                        <button class="carrito-item-eliminar" type="button">Eliminar</button>
                    </div>
                `;

                const btnEliminar = itemHtml.querySelector(".carrito-item-eliminar");

                btnEliminar.addEventListener("click", async () => {
                    try {
                        const response = await fetch(
                            `http://localhost:8080/carrito/eliminar?usuarioId=${usuarioId}&productoId=${producto.id}&talla=${item.talla}`,
                            {
                                method: "DELETE"
                            }
                        );

                        if (!response.ok) {
                            throw new Error("No se pudo eliminar el producto");
                        }

                        cargarMiniCarrito();
                    } catch (error) {
                        console.error("Error al eliminar producto del carrito:", error);
                    }
                });

                contenedorItems.appendChild(itemHtml);
            });

            totalElemento.textContent = `${Number(total).toFixed(2)} €`;
        } catch (error) {
            console.error("Error al cargar mini carrito:", error);
            contenedorItems.innerHTML = `<p class="carrito-vacio">No se pudo cargar el carrito.</p>`;
            totalElemento.textContent = "0 €";
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

    actualizarEstadoUsuario();

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

    if (btnAbrirCarritoDropdown) {
        btnAbrirCarritoDropdown.addEventListener("click", (e) => {
            e.preventDefault();
            abrirCarrito();
        });
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
        logoutBtn.addEventListener("click", (e) => {
            e.preventDefault();
            sessionStorage.removeItem("usuarioLogueado");
            sessionStorage.removeItem("nombreUsuario");
            sessionStorage.removeItem("usuarioId");
            actualizarEstadoUsuario();
            cerrarOverlay();
            cerrarCarrito();
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
}