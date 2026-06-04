document.addEventListener("DOMContentLoaded", () => {
    iniciarPaginaResultadosBusqueda();
});

const estadoResultados = {
    busqueda: "",
    paginaActual: 0,
    size: 16,
    totalElementos: 0,
    ultimaPagina: false,
    cargando: false,
    productos: [],
    observador: null
};

function iniciarPaginaResultadosBusqueda() {
    const refs = obtenerReferenciasResultados();

    estadoResultados.busqueda = obtenerBusquedaDesdeUrl();

    pintarBusquedaInicial(refs);
    configurarEventosResultados(refs);
    actualizarResumenResultados(refs);
    cargarResultadosBusqueda(refs, true);
}

function obtenerReferenciasResultados() {
    return {
        textoBusqueda: document.getElementById("texto-busqueda"),
        resumenBusqueda: document.getElementById("resumen-busqueda"),
        resumenTotalResultados: document.getElementById("resumen-total-resultados"),
        resumenFiltrosResultados: document.getElementById("resumen-filtros-resultados"),
        contadorResultados: document.getElementById("contador-resultados"),
        resultadosHint: document.getElementById("resultados-hint"),
        filtroTienda: document.getElementById("filtro-tienda-resultados"),
        filtroSeccion: document.getElementById("filtro-seccion-resultados"),
        ordenResultados: document.getElementById("orden-resultados"),
        limpiarResultados: document.getElementById("limpiar-resultados"),
        estadoResultados: document.getElementById("estado-resultados"),
        gridResultados: document.getElementById("grid-resultados"),
        sentinelResultados: document.getElementById("sentinel-resultados")
    };
}

function obtenerBusquedaDesdeUrl() {
    const params = new URLSearchParams(window.location.search);
    return (params.get("busqueda") || "").trim();
}

function pintarBusquedaInicial(refs) {
    const busquedaVisible = estadoResultados.busqueda || "tu busqueda";

    if (refs.textoBusqueda) {
        refs.textoBusqueda.textContent = busquedaVisible;
    }

    if (refs.resumenBusqueda) {
        refs.resumenBusqueda.textContent = busquedaVisible;
    }

    document.title = estadoResultados.busqueda
        ? `Resultados para ${estadoResultados.busqueda}`
        : "Resultados de busqueda";
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

    refs.limpiarResultados?.addEventListener("click", () => {
        if (refs.filtroTienda) refs.filtroTienda.value = "";
        if (refs.filtroSeccion) refs.filtroSeccion.value = "";
        if (refs.ordenResultados) refs.ordenResultados.value = "recientes";

        cargarResultadosBusqueda(refs, true);
    });
}

async function cargarResultadosBusqueda(refs, reiniciar = true) {
    if (estadoResultados.cargando) return;
    if (estadoResultados.ultimaPagina && !reiniciar) return;

    if (!estadoResultados.busqueda) {
        estadoResultados.paginaActual = 0;
        estadoResultados.totalElementos = 0;
        estadoResultados.ultimaPagina = true;
        estadoResultados.productos = [];

        limpiarContenedor(refs.gridResultados);
        mostrarEstadoResultados(refs, "Haz una busqueda desde el menu.", "vacio");
        actualizarContadorResultados(refs, 0);
        actualizarResumenResultados(refs);
        limpiarSentinel(refs);
        return;
    }

    if (reiniciar) {
        estadoResultados.paginaActual = 0;
        estadoResultados.totalElementos = 0;
        estadoResultados.ultimaPagina = false;
        estadoResultados.productos = [];

        limpiarContenedor(refs.gridResultados);
        mostrarEstadoResultados(refs, "Cargando resultados...", "info");
        actualizarContadorResultados(refs, "Buscando productos...");
        actualizarResumenResultados(refs);
    }

    try {
        estadoResultados.cargando = true;
        marcarGridCargando(refs, true);
        actualizarResumenResultados(refs);

        const response = await fetch(construirUrlResultados(refs), {
            method: "GET",
            credentials: "include"
        });

        if (!response.ok) {
            throw new Error("No hemos podido cargar los resultados.");
        }

        const data = await response.json();
        const pagina = normalizarPaginaResultados(data);

        estadoResultados.totalElementos = Number(pagina.totalElementos) || 0;
        estadoResultados.ultimaPagina = Boolean(pagina.ultimaPagina);
        estadoResultados.paginaActual = Number(pagina.paginaActual) || 0;

        if (reiniciar) {
            estadoResultados.productos = pagina.productos;
        } else {
            estadoResultados.productos = unirProductosSinDuplicados(
                estadoResultados.productos,
                pagina.productos
            );
        }

        ocultarEstadoResultados(refs);
        renderizarResultados(refs);
    } catch (error) {
        console.error("Error cargando resultados:", error);

        if (!estadoResultados.productos.length) {
            mostrarEstadoResultados(refs, "No hemos podido cargar los resultados.", "error");
        }

        actualizarResumenResultados(refs);
    } finally {
        estadoResultados.cargando = false;
        marcarGridCargando(refs, false);
        actualizarResumenResultados(refs);
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

function renderizarResultados(refs) {
    limpiarContenedor(refs.gridResultados);

    if (!estadoResultados.productos.length) {
        mostrarEstadoResultados(
            refs,
            `No hemos encontrado productos para "${estadoResultados.busqueda}".`,
            "vacio"
        );
        actualizarContadorResultados(refs, 0);
        actualizarResumenResultados(refs);
        limpiarSentinel(refs);
        return;
    }

    estadoResultados.productos.forEach((producto, indice) => {
        refs.gridResultados.appendChild(crearCardProductoResultado(producto, indice));
    });

    actualizarContadorResultados(refs, estadoResultados.totalElementos);
    actualizarResumenResultados(refs);
    prepararScrollInfinito(refs);
    actualizarSentinel(refs);
}

function crearCardProductoResultado(producto, indice) {
    const card = document.createElement("article");
    card.className = "producto-card";
    card.style.setProperty("--card-delay", `${Math.min(indice, 12) * 35}ms`);
    card.tabIndex = 0;
    card.setAttribute("role", "link");

    const abrirDetalle = () => {
        if (producto?.id != null) {
            window.location.href = `fichaProducto.html?id=${producto.id}`;
        }
    };

    card.addEventListener("click", abrirDetalle);
    card.addEventListener("keydown", (event) => {
        if (event.key === "Enter" || event.key === " ") {
            event.preventDefault();
            abrirDetalle();
        }
    });

    const imagenWrap = document.createElement("div");
    imagenWrap.className = "producto-card-img-wrap";

    if (tieneOfertaActiva(producto)) {
        const offer = document.createElement("span");
        offer.className = "producto-card-offer";
        offer.textContent = obtenerTextoOferta(producto);
        imagenWrap.appendChild(offer);
    }

    if (esUrlImagenValida(producto?.urlImagen)) {
        const img = document.createElement("img");
        img.src = producto.urlImagen;
        img.alt = producto.nombre || "Producto";
        img.loading = "lazy";

        img.onerror = () => {
            pintarSinImagen(imagenWrap);
        };

        imagenWrap.appendChild(img);
    } else {
        pintarSinImagen(imagenWrap);
    }

    const body = document.createElement("div");
    body.className = "producto-card-body";

    const meta = document.createElement("div");
    meta.className = "producto-card-meta";
    meta.appendChild(crearBadgeProducto(formatearNombreTienda(producto?.tienda?.nombre || "Tienda")));

    if (producto?.categoria?.nombre) {
        meta.appendChild(crearBadgeProducto(producto.categoria.nombre));
    }

    if (producto?.seccion) {
        meta.appendChild(crearBadgeProducto(formatearSeccion(producto.seccion)));
    }

    const nombre = document.createElement("h3");
    nombre.textContent = producto?.nombre || "Producto sin nombre";

    const subtitulo = document.createElement("p");
    subtitulo.className = "producto-card-category";
    subtitulo.textContent = construirSubtituloProducto(producto);

    const priceRow = document.createElement("div");
    priceRow.className = "producto-card-price-row";

    const priceStack = document.createElement("div");
    priceStack.className = "producto-card-price-stack";

    const precio = document.createElement("p");
    precio.className = "producto-card-precio";
    precio.textContent = formatearPrecio(producto?.precio);
    priceStack.appendChild(precio);

    if (mostrarPrecioOriginal(producto)) {
        const precioOriginal = document.createElement("p");
        precioOriginal.className = "producto-card-precio-original";
        precioOriginal.textContent = formatearPrecio(producto.precioOriginal);
        priceStack.appendChild(precioOriginal);
    }

    priceRow.appendChild(priceStack);

    if (producto?.porcentajeDescuento != null && Number(producto.porcentajeDescuento) > 0) {
        const discount = document.createElement("span");
        discount.className = "producto-card-discount";
        discount.textContent = `-${producto.porcentajeDescuento}%`;
        priceRow.appendChild(discount);
    }

    const descripcion = document.createElement("p");
    descripcion.className = "producto-card-descripcion";
    descripcion.textContent = recortarTexto(producto?.descripcion || "", 110);

    const footer = document.createElement("div");
    footer.className = "producto-card-footer";

    const cta = document.createElement("span");
    cta.className = "producto-card-cta";
    cta.textContent = "Ver detalle";

    const arrow = document.createElement("span");
    arrow.className = "producto-card-arrow";
    arrow.setAttribute("aria-hidden", "true");
    arrow.textContent = ">";

    footer.append(cta, arrow);

    body.append(meta, nombre, subtitulo, priceRow);

    if (descripcion.textContent) {
        body.appendChild(descripcion);
    }

    body.appendChild(footer);
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

        if (!entrada?.isIntersecting || estadoResultados.cargando || estadoResultados.ultimaPagina) {
            return;
        }

        estadoResultados.paginaActual += 1;
        await cargarResultadosBusqueda(refs, false);
    }, {
        root: null,
        rootMargin: "420px",
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
        mensaje.textContent = "No hay mas resultados.";
    } else {
        mensaje.className = "resultados-loader";
        mensaje.textContent = "Desplazate para ver mas productos.";
    }

    refs.sentinelResultados.appendChild(mensaje);
}

function limpiarSentinel(refs) {
    if (!refs.sentinelResultados) return;

    refs.sentinelResultados.innerHTML = "";

    if (estadoResultados.observador) {
        estadoResultados.observador.disconnect();
        estadoResultados.observador = null;
    }
}

function unirProductosSinDuplicados(actuales, nuevos) {
    const mapa = new Map();

    actuales.forEach((producto) => {
        if (producto?.id != null) {
            mapa.set(producto.id, producto);
        }
    });

    nuevos.forEach((producto) => {
        if (producto?.id != null) {
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
        return;
    }

    refs.contadorResultados.textContent = `${formatearNumero(total)} productos encontrados`;
}

function actualizarResumenResultados(refs) {
    if (refs.resumenBusqueda) {
        refs.resumenBusqueda.textContent = estadoResultados.busqueda || "tu busqueda";
    }

    if (refs.resumenTotalResultados) {
        if (estadoResultados.cargando && !estadoResultados.productos.length) {
            refs.resumenTotalResultados.textContent = "Buscando...";
        } else if (estadoResultados.totalElementos === 1) {
            refs.resumenTotalResultados.textContent = "1 producto";
        } else {
            refs.resumenTotalResultados.textContent = `${formatearNumero(estadoResultados.totalElementos)} productos`;
        }
    }

    const filtrosActivos = obtenerFiltrosActivos(refs);

    if (refs.resumenFiltrosResultados) {
        refs.resumenFiltrosResultados.textContent = filtrosActivos.length
            ? filtrosActivos.join(" | ")
            : "Sin filtros";
    }

    if (refs.resultadosHint) {
        refs.resultadosHint.textContent = construirHintResultados(filtrosActivos);
    }
}

function obtenerFiltrosActivos(refs) {
    const filtros = [];

    if (refs.filtroTienda?.value) {
        filtros.push(formatearNombreTienda(refs.filtroTienda.value));
    }

    if (refs.filtroSeccion?.value) {
        filtros.push(formatearSeccion(refs.filtroSeccion.value));
    }

    const orden = refs.ordenResultados?.value || "recientes";
    if (orden !== "recientes") {
        filtros.push(obtenerTextoOrden(orden));
    }

    return filtros;
}

function construirHintResultados(filtrosActivos) {
    if (!estadoResultados.busqueda) {
        return "Haz una busqueda desde el menu para empezar.";
    }

    if (estadoResultados.cargando && !estadoResultados.productos.length) {
        return "Estamos buscando productos.";
    }

    if (!estadoResultados.productos.length) {
        return "Prueba con otra busqueda o ajusta los filtros.";
    }

    if (estadoResultados.ultimaPagina) {
        return "No hay mas resultados por ahora.";
    }

    if (filtrosActivos.length) {
        return "Puedes ajustar los filtros cuando quieras.";
    }

    return "Desplazate para seguir viendo productos.";
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

function marcarGridCargando(refs, cargando) {
    if (!refs.gridResultados) return;
    refs.gridResultados.setAttribute("aria-busy", cargando ? "true" : "false");
}

function pintarSinImagen(contenedor) {
    limpiarContenedor(contenedor);

    const sinImagen = document.createElement("div");
    sinImagen.className = "producto-card-sin-imagen";
    sinImagen.textContent = "Sin imagen";
    contenedor.appendChild(sinImagen);
}

function esUrlImagenValida(url) {
    return typeof url === "string" && url.trim().length > 0;
}

function construirSubtituloProducto(producto) {
    const piezas = [];

    if (producto?.categoria?.nombre) {
        piezas.push(producto.categoria.nombre);
    }

    if (producto?.seccion) {
        piezas.push(formatearSeccion(producto.seccion));
    }

    return piezas.length ? piezas.join(" | ") : "Producto";
}

function mostrarPrecioOriginal(producto) {
    const precio = Number(producto?.precio);
    const precioOriginal = Number(producto?.precioOriginal);

    return Number.isFinite(precioOriginal)
        && Number.isFinite(precio)
        && precioOriginal > precio;
}

function tieneOfertaActiva(producto) {
    return mostrarPrecioOriginal(producto)
        || (producto?.porcentajeDescuento != null && Number(producto.porcentajeDescuento) > 0)
        || Boolean(producto?.enOferta);
}

function obtenerTextoOferta(producto) {
    if (producto?.porcentajeDescuento != null && Number(producto.porcentajeDescuento) > 0) {
        return `-${producto.porcentajeDescuento}%`;
    }

    return "Oferta";
}

function formatearPrecio(valor) {
    const numero = Number(valor);

    if (!Number.isFinite(numero)) {
        return "0,00 EUR";
    }

    return new Intl.NumberFormat("es-ES", {
        style: "currency",
        currency: "EUR"
    }).format(numero);
}

function formatearNumero(valor) {
    return new Intl.NumberFormat("es-ES").format(Number(valor) || 0);
}

function formatearSeccion(seccion) {
    switch ((seccion || "").toUpperCase()) {
        case "HOMBRE":
            return "Hombre";
        case "MUJER":
            return "Mujer";
        case "UNISEX":
            return "Unisex";
        default:
            return seccion || "Sin seccion";
    }
}

function formatearNombreTienda(nombre) {
    if (!nombre) return "Tienda";

    const tiendaNormalizada = String(nombre).trim().toUpperCase();

    if (tiendaNormalizada === "PULLANDBEAR") {
        return "Pull&Bear";
    }

    if (tiendaNormalizada === "BERSHKA") {
        return "Bershka";
    }

    if (tiendaNormalizada === "ZARA") {
        return "Zara";
    }

    return nombre;
}

function obtenerTextoOrden(valor) {
    switch (valor) {
        case "precio-asc":
            return "Precio ascendente";
        case "precio-desc":
            return "Precio descendente";
        case "nombre-asc":
            return "Nombre A-Z";
        default:
            return "Mas recientes";
    }
}

function recortarTexto(texto, maximo) {
    if (!texto) return "";

    return texto.length > maximo
        ? `${texto.slice(0, maximo).trim()}...`
        : texto;
}
