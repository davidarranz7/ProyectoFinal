const params = new URLSearchParams(window.location.search);
const productoId = params.get("id");

let tallaSeleccionada = null;

const mensajeTalla = document.createElement("p");
mensajeTalla.id = "mensaje-talla";
mensajeTalla.classList.add("mensaje-validacion", "oculto");
mensajeTalla.textContent = "Selecciona una talla antes de continuar.";

document.getElementById("tallas-lista").after(mensajeTalla);

fetch(`http://localhost:8080/productos/${productoId}`)
    .then(res => res.json())
    .then(producto => {
        console.log("Producto:", producto);

        document.getElementById("producto-imagen").src = producto.urlImagen;
        document.getElementById("producto-nombre").textContent = producto.nombre;
        document.getElementById("producto-precio").textContent = producto.precio + " €";
        document.getElementById("producto-descripcion").textContent = producto.descripcion || "Sin descripción";
        document.getElementById("producto-tienda").textContent = producto.tienda?.nombre || "Tienda";
        document.getElementById("producto-categoria").textContent = producto.categoria?.nombre || "Categoría";
        document.getElementById("btn-tienda-original").href = producto.urlProducto;

        document.getElementById("btn-carrito").addEventListener("click", () => {
            if (!tallaSeleccionada) {
                mensajeTalla.classList.remove("oculto");
                return;
            }

            console.log("Añadir al carrito:");
            console.log("Producto ID:", productoId);
            console.log("Talla:", tallaSeleccionada);
        });

        fetch(`http://localhost:8080/productos/${productoId}/talla-stock`)
            .then(res => res.json())
            .then(tallas => {
                console.log("Tallas:", tallas);

                const contenedorTallas = document.getElementById("tallas-lista");
                contenedorTallas.innerHTML = "";

                tallas.forEach(item => {
                    const boton = document.createElement("button");
                    boton.textContent = item.talla;
                    boton.classList.add("talla-btn");

                    if (item.stock > 0) {
                        boton.classList.add("disponible");

                        if (item.stock <= 5) {
                            boton.classList.add("poco-stock");
                            boton.title = "Pocas unidades";
                        }

                        boton.addEventListener("click", () => {
                            const yaSeleccionada = boton.classList.contains("seleccionada");

                            document.querySelectorAll(".talla-btn").forEach(btn => {
                                btn.classList.remove("seleccionada");
                            });

                            if (yaSeleccionada) {
                                tallaSeleccionada = null;
                            } else {
                                boton.classList.add("seleccionada");
                                tallaSeleccionada = item.talla;
                                mensajeTalla.classList.add("oculto");
                            }

                            console.log("Talla seleccionada:", tallaSeleccionada);
                        });

                    } else {
                        boton.classList.add("agotada");
                        boton.disabled = true;
                        boton.title = "Agotado";
                    }

                    contenedorTallas.appendChild(boton);
                });
            })
            .catch(err => console.error("Error al cargar tallas:", err));

    })
    .catch(err => console.error("Error al cargar producto:", err));