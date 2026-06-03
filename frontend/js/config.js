const FRONTEND_HOST = window.location.hostname;
const API_HOST = !FRONTEND_HOST || FRONTEND_HOST === "localhost" || FRONTEND_HOST === "127.0.0.1"
    ? "localhost"
    : FRONTEND_HOST;

const BASE_URL = `http://${API_HOST}:8080`;

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
