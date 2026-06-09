const FRONTEND_PROTOCOL = window.location.protocol === "https:" ? "https" : "http";
const FRONTEND_HOST = window.location.hostname;
const FRONTEND_PORT = window.location.port;
const ES_ENTORNO_LOCAL = !FRONTEND_HOST || FRONTEND_HOST === "localhost" || FRONTEND_HOST === "127.0.0.1";
const API_HOST = ES_ENTORNO_LOCAL ? "localhost" : FRONTEND_HOST;
const API_PROTOCOL = ES_ENTORNO_LOCAL ? "http" : FRONTEND_PROTOCOL;
const FRONTEND_ORIGIN = `${FRONTEND_PROTOCOL}://${FRONTEND_HOST}${FRONTEND_PORT ? `:${FRONTEND_PORT}` : ""}`;

const BASE_URL = `${API_PROTOCOL}://${API_HOST}:8080`;

const TALLAS_ROPA = ["XS", "S", "M", "L", "XL"];
const TALLAS_ZAPATOS = ["TALLA_35", "TALLA_36", "TALLA_37", "TALLA_38", "TALLA_39", "TALLA_40", "TALLA_41", "TALLA_42"];
const TALLAS_ACCESORIOS = ["UNICA"];

function normalizarTextoTalla(valor) {
    return String(valor || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase();
}

function normalizarValorTalla(talla) {
    const valor = String(talla || "").trim();
    const valorNormalizado = normalizarTextoTalla(valor);

    if (/^\d+$/.test(valor)) {
        return `TALLA_${valor}`;
    }

    if (valorNormalizado === "unica" || valorNormalizado === "talla unica") {
        return "UNICA";
    }

    const valorMayusculas = valor.toUpperCase();
    return TALLAS_ROPA.includes(valorMayusculas) ? valorMayusculas : valor;
}

function obtenerTextoProductoTalla(producto) {
    return normalizarTextoTalla([
        producto?.categoria?.nombre,
        producto?.nombre
    ].filter(Boolean).join(" "));
}

function obtenerTextoCategoriaTalla(producto) {
    return normalizarTextoTalla(producto?.categoria?.nombre);
}

function contieneAlguno(texto, terminos) {
    return terminos.some((termino) => texto.includes(termino));
}

function esProductoZapato(producto) {
    const texto = obtenerTextoProductoTalla(producto);

    return contieneAlguno(texto, [
        "zapato",
        "zapatilla",
        "calzado",
        "bota",
        "botin",
        "sandalia",
        "chancla",
        "mocasin",
        "bailarina",
        "alpargata"
    ]);
}

function esProductoAccesorio(producto) {
    const texto = obtenerTextoProductoTalla(producto);

    return contieneAlguno(texto, [
        "accesorio",
        "bolso",
        "bolsa",
        "mochila",
        "cartera",
        "monedero",
        "cinturon",
        "gafa",
        "joya",
        "bisuteria",
        "collar",
        "pulsera",
        "anillo",
        "pendiente",
        "reloj",
        "gorra",
        "sombrero",
        "bufanda",
        "panuelo",
        "neceser",
        "llavero"
    ]);
}

function obtenerTipoTallaProducto(producto) {
    const categoria = obtenerTextoCategoriaTalla(producto);
    const terminosCalzado = [
        "zapato",
        "zapatilla",
        "calzado",
        "bota",
        "botin",
        "sandalia",
        "chancla",
        "mocasin",
        "bailarina",
        "alpargata"
    ];
    const terminosAccesorio = [
        "accesorio",
        "bolso",
        "bolsa",
        "mochila",
        "cartera",
        "monedero",
        "cinturon",
        "gafa",
        "joya",
        "bisuteria",
        "collar",
        "pulsera",
        "anillo",
        "pendiente",
        "reloj",
        "gorra",
        "sombrero",
        "bufanda",
        "panuelo",
        "neceser",
        "llavero"
    ];
    const terminosRopa = [
        "camiseta",
        "camisa",
        "pantalon",
        "vestido",
        "falda",
        "abrigo",
        "chaqueta",
        "jersey",
        "sudadera",
        "top",
        "blusa",
        "traje",
        "short",
        "bermuda",
        "vaquero",
        "leggin",
        "polo",
        "chaleco",
        "ropa",
        "lenceria",
        "banador",
        "bikini"
    ];

    if (categoria && contieneAlguno(categoria, terminosCalzado)) {
        return "calzado";
    }

    if (categoria && contieneAlguno(categoria, terminosAccesorio)) {
        return "accesorio";
    }

    if (categoria && contieneAlguno(categoria, terminosRopa)) {
        return "ropa";
    }

    if (esProductoZapato(producto)) {
        return "calzado";
    }

    if (esProductoAccesorio(producto)) {
        return "accesorio";
    }

    return "ropa";
}

function obtenerTallasPermitidasProducto(producto) {
    const tipo = obtenerTipoTallaProducto(producto);

    if (tipo === "calzado") {
        return [...TALLAS_ZAPATOS];
    }

    if (tipo === "accesorio") {
        return [...TALLAS_ACCESORIOS];
    }

    return [...TALLAS_ROPA];
}

function filtrarTallaStocksProducto(producto, tallaStocks) {
    const tallas = Array.isArray(tallaStocks) ? tallaStocks : [];
    const tallasPorValor = new Map();

    tallas.forEach((item) => {
        const tallaNormalizada = normalizarValorTalla(item?.talla);
        if (tallaNormalizada) {
            tallasPorValor.set(tallaNormalizada, item);
        }
    });

    return obtenerTallasPermitidasProducto(producto).map((talla) => {
        const item = tallasPorValor.get(talla) || {};

        return {
            ...item,
            talla,
            stock: Number(item.stock ?? 0)
        };
    });
}

function formatearTallaProducto(talla) {
    const valor = normalizarValorTalla(talla);

    if (valor === "UNICA") {
        return "Unica";
    }

    const tallaZapato = valor.match(/^TALLA_(\d+)$/);
    if (tallaZapato) {
        return tallaZapato[1];
    }

    return valor;
}

window.TallasProducto = {
    normalizarTalla: normalizarValorTalla,
    formatearTalla: formatearTallaProducto,
    obtenerTipo: obtenerTipoTallaProducto,
    obtenerTallasPermitidas: obtenerTallasPermitidasProducto,
    filtrarTallaStocks: filtrarTallaStocksProducto
};

window.ModaRuntimeConfig = {
    frontendOrigin: FRONTEND_ORIGIN,
    baseUrl: BASE_URL,
    esEntornoLocal: ES_ENTORNO_LOCAL
};

inyectarConfiguracionPwa();

function inyectarConfiguracionPwa() {
    if (!document || !document.head) {
        return;
    }

    asegurarMeta("theme-color", "#171411");
    asegurarMeta("apple-mobile-web-app-capable", "yes");
    asegurarMeta("apple-mobile-web-app-status-bar-style", "default");
    asegurarMeta("apple-mobile-web-app-title", "MODA");
    asegurarMeta("mobile-web-app-capable", "yes");
    asegurarLink("manifest", "manifest.json");
    asegurarIconoPrincipal();
}

function asegurarMeta(name, content) {
    let meta = document.head.querySelector(`meta[name="${name}"]`);

    if (!meta) {
        meta = document.createElement("meta");
        meta.setAttribute("name", name);
        document.head.appendChild(meta);
    }

    meta.setAttribute("content", content);
}

function asegurarLink(rel, href) {
    let link = document.head.querySelector(`link[rel="${rel}"]`);

    if (!link) {
        link = document.createElement("link");
        link.setAttribute("rel", rel);
        document.head.appendChild(link);
    }

    link.setAttribute("href", href);
}

function asegurarIconoPrincipal() {
    let icon = document.head.querySelector('link[rel="icon"]');

    if (!icon) {
        icon = document.createElement("link");
        icon.setAttribute("rel", "icon");
        document.head.appendChild(icon);
    }

    icon.setAttribute("href", "icon.svg");
    icon.setAttribute("type", "image/svg+xml");
}
