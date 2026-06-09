package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CambioPrecioProductoDTO;
import com.david.ProyectoFinal.model.HistorialPrecioProducto;
import com.david.ProyectoFinal.model.Producto;
import com.david.ProyectoFinal.model.ScrapingEjecucion;
import com.david.ProyectoFinal.model.TipoCambioPrecio;
import com.david.ProyectoFinal.repository.HistorialPrecioProductoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class HistorialPrecioProductoService {

    private final HistorialPrecioProductoRepository historialPrecioProductoRepository;

    public HistorialPrecioProductoService(HistorialPrecioProductoRepository historialPrecioProductoRepository) {
        this.historialPrecioProductoRepository = historialPrecioProductoRepository;
    }

    public Optional<CambioPrecioProductoDTO> registrarCambioSiCorresponde(Producto productoExistente,
                                                                          Producto productoScrapeado,
                                                                          ScrapingEjecucion scrapingEjecucion,
                                                                          LocalDateTime fechaCambio) {
        if (!haCambiadoElPrecio(productoExistente, productoScrapeado)) {
            return Optional.empty();
        }

        BigDecimal precioAnterior = productoExistente == null ? null : productoExistente.getPrecio();
        BigDecimal precioNuevo = productoScrapeado == null ? null : productoScrapeado.getPrecio();

        TipoCambioPrecio tipoCambio = determinarTipoCambio(precioAnterior, precioNuevo);
        BigDecimal porcentajeVariacion = calcularPorcentajeVariacion(precioAnterior, precioNuevo);
        boolean rebajaMayor = esRebajaMayor(productoExistente, productoScrapeado, tipoCambio);

        HistorialPrecioProducto historial = new HistorialPrecioProducto();
        historial.setProducto(productoExistente);
        historial.setScrapingEjecucion(scrapingEjecucion);
        historial.setTipoCambio(tipoCambio);
        historial.setFechaCambio(fechaCambio == null ? LocalDateTime.now() : fechaCambio);
        historial.setNombreProducto(obtenerNombreProducto(productoExistente, productoScrapeado));
        historial.setTienda(obtenerNombreTienda(productoExistente, productoScrapeado));
        historial.setUrlProducto(obtenerUrlProducto(productoExistente, productoScrapeado));
        historial.setPrecioAnterior(precioAnterior);
        historial.setPrecioNuevo(precioNuevo);
        historial.setPrecioOriginalAnterior(productoExistente == null ? null : productoExistente.getPrecioOriginal());
        historial.setPrecioOriginalNuevo(productoScrapeado == null ? null : productoScrapeado.getPrecioOriginal());
        historial.setPorcentajeDescuentoAnterior(productoExistente == null ? null : productoExistente.getPorcentajeDescuento());
        historial.setPorcentajeDescuentoNuevo(productoScrapeado == null ? null : productoScrapeado.getPorcentajeDescuento());
        historial.setPorcentajeVariacionPrecio(porcentajeVariacion);
        historial.setRebajaMayor(rebajaMayor);

        HistorialPrecioProducto historialGuardado = historialPrecioProductoRepository.save(historial);

        CambioPrecioProductoDTO dto = new CambioPrecioProductoDTO();
        dto.setProductoId(productoExistente == null ? null : productoExistente.getId());
        dto.setNombreProducto(historialGuardado.getNombreProducto());
        dto.setTienda(historialGuardado.getTienda());
        dto.setUrlProducto(historialGuardado.getUrlProducto());
        dto.setPrecioAnterior(historialGuardado.getPrecioAnterior());
        dto.setPrecioNuevo(historialGuardado.getPrecioNuevo());
        dto.setPrecioOriginalAnterior(historialGuardado.getPrecioOriginalAnterior());
        dto.setPrecioOriginalNuevo(historialGuardado.getPrecioOriginalNuevo());
        dto.setPorcentajeDescuentoAnterior(historialGuardado.getPorcentajeDescuentoAnterior());
        dto.setPorcentajeDescuentoNuevo(historialGuardado.getPorcentajeDescuentoNuevo());
        dto.setPorcentajeVariacionPrecio(historialGuardado.getPorcentajeVariacionPrecio());
        dto.setRebajaMayor(historialGuardado.getRebajaMayor());
        dto.setTipoCambio(historialGuardado.getTipoCambio());
        dto.setFechaCambio(historialGuardado.getFechaCambio());
        return Optional.of(dto);
    }

    private boolean haCambiadoElPrecio(Producto productoExistente, Producto productoScrapeado) {
        if (productoExistente == null || productoScrapeado == null) {
            return false;
        }

        return compararBigDecimal(productoExistente.getPrecio(), productoScrapeado.getPrecio()) != 0;
    }

    private TipoCambioPrecio determinarTipoCambio(BigDecimal precioAnterior, BigDecimal precioNuevo) {
        if (precioAnterior == null || precioNuevo == null) {
            return TipoCambioPrecio.CAMBIO;
        }

        int comparacion = precioNuevo.compareTo(precioAnterior);

        if (comparacion < 0) {
            return TipoCambioPrecio.BAJADA;
        }

        if (comparacion > 0) {
            return TipoCambioPrecio.SUBIDA;
        }

        return TipoCambioPrecio.CAMBIO;
    }

    private BigDecimal calcularPorcentajeVariacion(BigDecimal precioAnterior, BigDecimal precioNuevo) {
        if (precioAnterior == null || precioNuevo == null || precioAnterior.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return precioNuevo
                .subtract(precioAnterior)
                .multiply(BigDecimal.valueOf(100))
                .divide(precioAnterior, 2, RoundingMode.HALF_UP);
    }

    private boolean esRebajaMayor(Producto productoExistente,
                                  Producto productoScrapeado,
                                  TipoCambioPrecio tipoCambio) {
        if (tipoCambio != TipoCambioPrecio.BAJADA || productoExistente == null || productoScrapeado == null) {
            return false;
        }

        Integer descuentoAnterior = productoExistente.getPorcentajeDescuento();
        Integer descuentoNuevo = productoScrapeado.getPorcentajeDescuento();

        if (descuentoAnterior == null || descuentoNuevo == null) {
            return false;
        }

        return descuentoNuevo > descuentoAnterior;
    }

    private int compararBigDecimal(BigDecimal valor1, BigDecimal valor2) {
        if (valor1 == null && valor2 == null) {
            return 0;
        }

        if (valor1 == null) {
            return -1;
        }

        if (valor2 == null) {
            return 1;
        }

        return valor1.compareTo(valor2);
    }

    private String obtenerNombreProducto(Producto productoExistente, Producto productoScrapeado) {
        if (productoScrapeado != null && productoScrapeado.getNombre() != null && !productoScrapeado.getNombre().isBlank()) {
            return productoScrapeado.getNombre().trim();
        }

        if (productoExistente != null && productoExistente.getNombre() != null && !productoExistente.getNombre().isBlank()) {
            return productoExistente.getNombre().trim();
        }

        return "Producto";
    }

    private String obtenerNombreTienda(Producto productoExistente, Producto productoScrapeado) {
        if (productoScrapeado != null
                && productoScrapeado.getTienda() != null
                && productoScrapeado.getTienda().getNombre() != null
                && !productoScrapeado.getTienda().getNombre().isBlank()) {
            return productoScrapeado.getTienda().getNombre().trim();
        }

        if (productoExistente != null
                && productoExistente.getTienda() != null
                && productoExistente.getTienda().getNombre() != null
                && !productoExistente.getTienda().getNombre().isBlank()) {
            return productoExistente.getTienda().getNombre().trim();
        }

        return "Sin tienda";
    }

    private String obtenerUrlProducto(Producto productoExistente, Producto productoScrapeado) {
        if (productoScrapeado != null && productoScrapeado.getUrlProducto() != null && !productoScrapeado.getUrlProducto().isBlank()) {
            return productoScrapeado.getUrlProducto().trim();
        }

        if (productoExistente != null && productoExistente.getUrlProducto() != null && !productoExistente.getUrlProducto().isBlank()) {
            return productoExistente.getUrlProducto().trim();
        }

        return null;
    }
}
