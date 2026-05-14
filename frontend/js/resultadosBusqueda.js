document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaResultadosBusqueda();
});

const estadoResultados = {
    busqueda: "",
    paginaActual: 0,
    size: 16,
    ultimaPagina: false,
    cargando: false,
    productos: [],
    observador: null
};

function iniciarPaginaResultadosBusqueda() {
    const refs = obtenerReferenciasResultados();

    estadoResultados.busqueda = obtenerBusquedaDesdeUrl();

    if (refs.textoBusqueda) {
        refs.textoBusqueda.textContent = estadoResultados.busqueda || "sin búsqueda";
    }

    configurarEventosResultados(refs);
    cargarResultadosBusqueda(refs, true);
}

function obtenerReferenciasResultados() {
    return {
        textoBusqueda: document.getElementById("texto-busqueda"),
        contadorResultados: document.getElementById("contador-resultados"),
        filtroTienda: document.getElementById("filtro-tienda-resultados"),
        filtroSeccion: document.getElementById("filtro-seccion-resultados"),
        ordenResultados: document.getElementById("orden-resultados"),
        estadoResultados: document.getElementById("estado-resultados"),
        gridResultados: document.getElementById("grid-resultados"),
        sentinelResultados: document.getElementById("sentinel-resultados")
    };
}

function obtenerBusquedaDesdeUrl() {
    const params = new URLSearchParams(window.location.search);
    return (params.get("busqueda") || "").trim();
}

function configurarEventosResultados(refs) {
    refs.filtroTienda?.addEventListener("change", () => {
        cargarResultadosBusqueda(refs, true);
    });

    refs.filtroSeccion?.addEventListener("change", () => {
        cargarResultadosBusqueda(refs, true);
    });

    refs.ordenResultados?.addEventListener("change", () => {
        cargarResultadosBusqueda(refs, true);
    });
}

async function cargarResultadosBusqueda(refs, reiniciar = true) {
    if (estadoResultados.cargando) return;
    if (estadoResultados.ultimaPagina && !reiniciar) return;

    if (!estadoResultados.busqueda) {
        mostrarEstadoResultados(refs, "Escribe una búsqueda desde el menú para ver resultados.", "vacio");
        actualizarContadorResultados(refs, 0);
        limpiarContenedor(refs.gridResultados);
        return;
    }

    if (reiniciar) {
        estadoResultados.paginaActual = 0;
        estadoResultados.ultimaPagina = false;
        estadoResultados.productos = [];

        limpiarContenedor(refs.gridResultados);
        mostrarEstadoResultados(refs, "Cargando resultados...", "info");
        actualizarContadorResultados(refs, "Buscando productos...");
    }

    try {
        estadoResultados.cargando = true;

        const response = await fetch(construirUrlResultados(refs), {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No se pudieron cargar los resultados.");
        }

        const data = await response.json();
        const pagina = normalizarPaginaResultados(data);

        estadoResultados.ultimaPagina = pagina.ultimaPagina;
        estadoResultados.paginaActual = pagina.paginaActual;

        if (reiniciar) {
            estadoResultados.productos = pagina.productos;
        } else {
            estadoResultados.productos = unirProductosSinDuplicados(
                estadoResultados.productos,
                pagina.productos
            );
        }

        ocultarEstadoResultados(refs);
        renderizarResultados(refs, pagina.totalElementos);

    } catch (error) {
        console.error("Error cargando resultados:", error);
        mostrarEstadoResultados(refs, "No se pudieron cargar los resultados.", "error");
    } finally {
        estadoResultados.cargando = false;
    }
}

function construirUrlResultados(refs) {
    const params = new URLSearchParams();

    params.append("page", estadoResultados.paginaActual);
    params.append("size", estadoResultados.size);
    params.append("busqueda", estadoResultados.busqueda);

    const tienda = refs.filtroTienda?.value || "";
    const seccion = refs.filtroSeccion?.value || "";
    const orden = refs.ordenResultados?.value || "recientes";

    if (tienda) params.append("tienda", tienda);
    if (seccion) params.append("seccion", seccion);
    if (orden) params.append("orden", orden);

    return `${BASE_URL}/productos/catalogo?${params.toString()}`;
}

function normalizarPaginaResultados(data) {
    if (Array.isArray(data)) {
        return {
            productos: data,
            totalElementos: data.length,
            paginaActual: 0,
            ultimaPagina: true
        };
    }

    const productos = Array.isArray(data.productos)
        ? data.productos
        : Array.isArray(data.content)
            ? data.content
            : Array.isArray(data.items)
                ? data.items
                : [];

    const totalElementos =
        data.totalElementos ??
        data.totalElements ??
        data.total ??
        productos.length;

    const paginaActual =
        data.paginaActual ??
        data.number ??
        data.page ??
        estadoResultados.paginaActual;

    const ultimaPagina =
        data.ultimaPagina ??
        data.last ??
        data.esUltimaPagina ??
        productos.length < estadoResultados.size;

    return {
        productos,
        totalElementos,
        paginaActual,
        ultimaPagina
    };
}

function renderizarResultados(refs, totalElementos) {
    limpiarContenedor(refs.gridResultados);

    if (!estadoResultados.productos.length) {
        mostrarEstadoResultados(
            refs,
            `No se encontraron productos para "${estadoResultados.busqueda}".`,
            "vacio"
        );
        actualizarContadorResultados(refs, 0);
        limpiarSentinel(refs);
        return;
    }

    actualizarContadorResultados(refs, totalElementos);

    estadoResultados.productos.forEach((producto) => {
        refs.gridResultados.appendChild(crearCardProductoResultado(producto));
    });

    prepararScrollInfinito(refs);
    actualizarSentinel(refs);
}

function crearCardProductoResultado(producto) {
    const card = document.createElement("article");
    card.className = "producto-card";
    card.addEventListener("click", () => {
        if (producto.id) {
            window.location.href = `fichaProducto.html?id=${producto.id}`;
        }
    });

    const imagenWrap = document.createElement("div");
    imagenWrap.className = "producto-card-img-wrap";

    if (producto.urlImagen) {
        const img = document.createElement("img");
        img.src = producto.urlImagen;
        img.alt = producto.nombre || "Producto";
        img.loading = "lazy";

        img.onerror = () => {
            limpiarContenedor(imagenWrap);

            const sinImagen = document.createElement("div");
            sinImagen.className = "producto-card-sin-imagen";
            sinImagen.textContent = "Sin imagen";

            imagenWrap.appendChild(sinImagen);
        };

        imagenWrap.appendChild(img);
    } else {
        const sinImagen = document.createElement("div");
        sinImagen.className = "producto-card-sin-imagen";
        sinImagen.textContent = "Sin imagen";
        imagenWrap.appendChild(sinImagen);
    }

    const body = document.createElement("div");
    body.className = "producto-card-body";

    const meta = document.createElement("div");
    meta.className = "producto-card-meta";

    meta.appendChild(crearBadgeProducto(producto.tienda?.nombre || "Sin tienda"));
    meta.appendChild(crearBadgeProducto(producto.categoria?.nombre || "Sin categoría"));

    if (producto.seccion) {
        meta.appendChild(crearBadgeProducto(formatearSeccion(producto.seccion)));
    }

    const nombre = document.createElement("h3");
    nombre.textContent = producto.nombre || "Producto sin nombre";

    const precio = document.createElement("p");
    precio.className = "producto-card-precio";
    precio.textContent = formatearPrecio(producto.precio);

    const descripcion = document.createElement("p");
    descripcion.className = "producto-card-descripcion";
    descripcion.textContent = recortarTexto(producto.descripcion || "", 90);

    body.append(meta, nombre, precio);

    if (descripcion.textContent) {
        body.appendChild(descripcion);
    }

    card.append(imagenWrap, body);

    return card;
}

function crearBadgeProducto(texto) {
    const badge = document.createElement("span");
    badge.className = "producto-badge";
    badge.textContent = texto;
    return badge;
}

function prepararScrollInfinito(refs) {
    if (!refs.sentinelResultados) return;

    if (estadoResultados.observador) {
        estadoResultados.observador.disconnect();
    }

    estadoResultados.observador = new IntersectionObserver(async (entries) => {
        const entrada = entries[0];

        if (entrada.isIntersecting && !estadoResultados.cargando && !estadoResultados.ultimaPagina) {
            estadoResultados.paginaActual++;
            await cargarResultadosBusqueda(refs, false);
        }
    }, {
        root: null,
        rootMargin: "450px",
        threshold: 0.1
    });

    estadoResultados.observador.observe(refs.sentinelResultados);
}

function actualizarSentinel(refs) {
    if (!refs.sentinelResultados) return;

    refs.sentinelResultados.innerHTML = "";

    const mensaje = document.createElement("div");

    if (estadoResultados.ultimaPagina) {
        mensaje.className = "resultados-fin";
        mensaje.textContent = "No hay más resultados.";
    } else {
        mensaje.className = "resultados-loader";
        mensaje.textContent = "Baja un poco más para cargar más productos...";
    }

    refs.sentinelResultados.appendChild(mensaje);
}

function limpiarSentinel(refs) {
    if (refs.sentinelResultados) {
        refs.sentinelResultados.innerHTML = "";
    }
}

function unirProductosSinDuplicados(actuales, nuevos) {
    const mapa = new Map();

    actuales.forEach((producto) => {
        if (producto && producto.id != null) {
            mapa.set(producto.id, producto);
        }
    });

    nuevos.forEach((producto) => {
        if (producto && producto.id != null) {
            mapa.set(producto.id, producto);
        }
    });

    return Array.from(mapa.values());
}

function actualizarContadorResultados(refs, totalElementos) {
    if (!refs.contadorResultados) return;

    if (typeof totalElementos === "string") {
        refs.contadorResultados.textContent = totalElementos;
        return;
    }

    const total = Number(totalElementos) || 0;

    if (total === 1) {
        refs.contadorResultados.textContent = "1 producto encontrado";
    } else {
        refs.contadorResultados.textContent = `${total.toLocaleString("es-ES")} productos encontrados`;
    }
}

function mostrarEstadoResultados(refs, texto, tipo = "info") {
    if (!refs.estadoResultados) return;

    refs.estadoResultados.textContent = texto;
    refs.estadoResultados.className = "estado-resultados";
    refs.estadoResultados.classList.add(tipo);
}

function ocultarEstadoResultados(refs) {
    if (!refs.estadoResultados) return;

    refs.estadoResultados.textContent = "";
    refs.estadoResultados.className = "estado-resultados";
}

function limpiarContenedor(contenedor) {
    if (!contenedor) return;

    while (contenedor.firstChild) {
        contenedor.removeChild(contenedor.firstChild);
    }
}

function formatearPrecio(valor) {
    const numero = Number(valor);

    if (!Number.isFinite(numero)) {
        return "0,00 €";
    }

    return `${numero.toFixed(2).replace(".", ",")} €`;
}

function formatearSeccion(seccion) {
    switch (seccion) {
        case "HOMBRE":
            return "Hombre";
        case "MUJER":
            return "Mujer";
        case "UNISEX":
            return "Unisex";
        default:
            return seccion || "Sin sección";
    }
}

function recortarTexto(texto, maximo) {
    if (!texto) return "";
    return texto.length > maximo ? `${texto.slice(0, maximo)}...` : texto;
}