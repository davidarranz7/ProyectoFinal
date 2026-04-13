document.addEventListener("DOMContentLoaded", () => {
    const botonesMenu = document.querySelectorAll(".menu-lateral-btn-admin");
    const secciones = document.querySelectorAll(".seccion-admin");
    const mensajeAdmin = document.getElementById("mensaje-admin");

    const btnGestionarProductos = document.getElementById("btn-gestionar-productos");
    const btnGestionarUsuarios = document.getElementById("btn-gestionar-usuarios");
    const btnGestionarPedidos = document.getElementById("btn-gestionar-pedidos");

    const btnScrapingZara = document.getElementById("btn-scraping-zara");
    const btnScrapingBershka = document.getElementById("btn-scraping-bershka");
    const btnScrapingPull = document.getElementById("btn-scraping-pull");
    const btnScrapingTodo = document.getElementById("btn-scraping-todo");
    const estadoScraping = document.getElementById("estado-scraping");

    const BASE_URL = "http://localhost:8080";

    function activarBoton(idSeccion) {
        botonesMenu.forEach((boton) => {
            boton.classList.remove("menu-lateral-btn-admin-activo");

            if (boton.dataset.seccion === idSeccion) {
                boton.classList.add("menu-lateral-btn-admin-activo");
            }
        });
    }

    function mostrarMensaje(texto, tipo = "info") {
        if (!mensajeAdmin) return;

        mensajeAdmin.textContent = texto;
        mensajeAdmin.className = "mensaje-admin";
        mensajeAdmin.classList.add(`mensaje-admin-${tipo}`);
        mensajeAdmin.style.display = "block";

        if (mensajeAdmin.timeoutId) {
            clearTimeout(mensajeAdmin.timeoutId);
        }

        mensajeAdmin.timeoutId = setTimeout(() => {
            mensajeAdmin.style.display = "none";
        }, 3000);
    }

    botonesMenu.forEach((boton) => {
        boton.addEventListener("click", () => {
            const idSeccion = boton.dataset.seccion;
            const seccionDestino = document.getElementById(idSeccion);

            if (!seccionDestino) return;

            seccionDestino.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });

            activarBoton(idSeccion);
        });
    });

    const observer = new IntersectionObserver((entries) => {
        let seccionActiva = null;

        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                seccionActiva = entry.target.id;
            }
        });

        if (seccionActiva) {
            activarBoton(seccionActiva);
        }
    }, {
        root: null,
        rootMargin: "-140px 0px -55% 0px",
        threshold: 0.15
    });

    secciones.forEach((seccion) => observer.observe(seccion));

    if (btnGestionarProductos) {
        btnGestionarProductos.addEventListener("click", () => {
            document.getElementById("productos").scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
            activarBoton("productos");
        });
    }

    if (btnGestionarUsuarios) {
        btnGestionarUsuarios.addEventListener("click", () => {
            document.getElementById("usuarios").scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
            activarBoton("usuarios");
        });
    }

    if (btnGestionarPedidos) {
        btnGestionarPedidos.addEventListener("click", () => {
            document.getElementById("pedidos").scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
            activarBoton("pedidos");
        });
    }

    function bloquearBoton(boton, texto) {
        if (!boton) return;
        boton.disabled = true;
        boton.textContent = texto;
    }

    function restaurarBoton(boton, texto) {
        if (!boton) return;
        boton.disabled = false;
        boton.textContent = texto;
    }

    async function ejecutarScraping(url, nombre, boton, textoOriginal) {
        try {
            if (estadoScraping) {
                estadoScraping.textContent = "Ejecutando...";
            }

            bloquearBoton(boton, "Ejecutando...");
            mostrarMensaje(`Iniciando scraping de ${nombre}...`, "info");

            const response = await fetch(`${BASE_URL}${url}`, {
                method: "POST"
            });

            if (!response.ok) {
                throw new Error(`Error HTTP ${response.status}`);
            }

            const productos = await response.json();

            if (estadoScraping) {
                estadoScraping.textContent = "Listo";
            }

            mostrarMensaje(
                `Scraping de ${nombre} completado correctamente. Productos procesados: ${productos.length}.`,
                "ok"
            );

        } catch (error) {
            console.error(`Error en scraping de ${nombre}:`, error);

            if (estadoScraping) {
                estadoScraping.textContent = "Error";
            }

            mostrarMensaje(`Error al ejecutar el scraping de ${nombre}.`, "error");
        } finally {
            restaurarBoton(boton, textoOriginal);
        }
    }

    if (btnScrapingZara) {
        btnScrapingZara.addEventListener("click", () => {
            ejecutarScraping(
                "/productos/scrapear/zara",
                "Zara",
                btnScrapingZara,
                "Ejecutar scraping"
            );
        });
    }

    if (btnScrapingBershka) {
        btnScrapingBershka.addEventListener("click", () => {
            ejecutarScraping(
                "/productos/scrapear/bershka",
                "Bershka",
                btnScrapingBershka,
                "Ejecutar scraping"
            );
        });
    }

    if (btnScrapingPull) {
        btnScrapingPull.addEventListener("click", () => {
            ejecutarScraping(
                "/productos/scrapear/pullandbear",
                "Pull&Bear",
                btnScrapingPull,
                "Ejecutar scraping"
            );
        });
    }

    if (btnScrapingTodo) {
        btnScrapingTodo.addEventListener("click", () => {
            ejecutarScraping(
                "/productos/scrapear/total",
                "todos los scrapers",
                btnScrapingTodo,
                "Ejecutar todo"
            );
        });
    }
});