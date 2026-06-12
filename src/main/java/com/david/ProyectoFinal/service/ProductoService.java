package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.*;
import com.david.ProyectoFinal.model.*;
import com.david.ProyectoFinal.repository.*;
import com.david.ProyectoFinal.scraper.gestor.GestorScraping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private static final int LIMITE_CAMBIOS_PRECIO_RESUMEN = 12;

    private final ProductoRepository productoRepository;
    private final GestorScraping gestorScraping;
    private final TiendaRepository tiendaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoTallaStockRepository productoTallaStockRepository;
    private final FavoritoRepository favoritoRepository;
    private final ProductoImagenRepository productoImagenRepository;
    private final ScrapingPendienteRepository scrapingPendienteRepository;
    private final ScrapingEjecucionRepository scrapingEjecucionRepository;
    private final HistorialPrecioProductoService historialPrecioProductoService;
    private final ScrapingResumenAdminService scrapingResumenAdminService;
    private final NotificacionUsuarioService notificacionUsuarioService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReentrantLock scrapingLock = new ReentrantLock();

    @Value("${app.scraping.relay.enabled:false}")
    private boolean scrapingRelayEnabled;

    @Value("${app.scraping.relay.receiver-url:http://127.0.0.1:8095/internal/mail-relay/scraping}")
    private String scrapingRelayReceiverUrl;

    @Value("${app.scraping.relay.token:}")
    private String scrapingRelayToken;

    @Value("${app.scraping.relay.connect-timeout-ms:2500}")
    private long scrapingRelayConnectTimeoutMs;

    @Value("${app.scraping.relay.read-timeout-ms:300000}")
    private long scrapingRelayReadTimeoutMs;

    @Value("${app.scraping.auto.enabled:false}")
    private boolean scrapingAutomaticoEnabled;

    @Value("${app.scraping.auto.fixed-delay-ms:21600000}")
    private long scrapingAutomaticoFixedDelayMs;

    @Value("${app.scraping.relay.retry-interval-ms:60000}")
    private long scrapingRelayRetryIntervalMs;

    public ProductoService(ProductoRepository productoRepository,
                           GestorScraping gestorScraping,
                           TiendaRepository tiendaRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoTallaStockRepository productoTallaStockRepository,
                           FavoritoRepository favoritoRepository,
                           ProductoImagenRepository productoImagenRepository,
                           ScrapingPendienteRepository scrapingPendienteRepository,
                           ScrapingEjecucionRepository scrapingEjecucionRepository,
                           HistorialPrecioProductoService historialPrecioProductoService,
                           ScrapingResumenAdminService scrapingResumenAdminService,
                           NotificacionUsuarioService notificacionUsuarioService) {
        this.productoRepository = productoRepository;
        this.gestorScraping = gestorScraping;
        this.tiendaRepository = tiendaRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoTallaStockRepository = productoTallaStockRepository;
        this.favoritoRepository = favoritoRepository;
        this.productoImagenRepository = productoImagenRepository;
        this.scrapingPendienteRepository = scrapingPendienteRepository;
        this.scrapingEjecucionRepository = scrapingEjecucionRepository;
        this.historialPrecioProductoService = historialPrecioProductoService;
        this.scrapingResumenAdminService = scrapingResumenAdminService;
        this.notificacionUsuarioService = notificacionUsuarioService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void inicializarDisponibilidadCatalogoProductos() {
        productoRepository.marcarDisponibilidadCatalogoNulaComoTrue();
    }

    public List<Producto> obtenerProductosMasFavoritos(int limite) {
        int tamanoConsulta = Math.max(limite * 3, limite);

        return favoritoRepository.findProductosMasFavoritos(PageRequest.of(0, tamanoConsulta))
                .stream()
                .filter(this::productoDisponibleEnCatalogo)
                .limit(limite)
                .toList();
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerTodosDisponiblesCatalogo() {
        return productoRepository.findAll()
                .stream()
                .filter(this::productoDisponibleEnCatalogo)
                .toList();
    }

    public Producto guardar(Producto producto) {
        if (producto != null && producto.getDisponibleCatalogo() == null) {
            producto.setDisponibleCatalogo(true);
        }
        return productoRepository.save(producto);
    }

    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto obtenerPorId(Long id, Boolean incluirNoDisponibles) {
        Producto producto = obtenerPorId(id);

        if (producto == null) {
            return null;
        }

        if (Boolean.TRUE.equals(incluirNoDisponibles) || productoDisponibleEnCatalogo(producto)) {
            return producto;
        }

        return null;
    }

    public void eliminar(Long id) {
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            return;
        }

        producto.setDisponibleCatalogo(false);
        producto.setFechaDesactivacion(LocalDateTime.now());
        producto.setMotivoDesactivacion("DESACTIVADO_MANUALMENTE");
        productoRepository.save(producto);
    }

    public Producto actualizar(Long id, Producto productoActualizado) {
        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto != null) {
            BigDecimal precioAnterior = producto.getPrecio();
            BigDecimal precioNuevo = productoActualizado.getPrecio();

            producto.setNombre(productoActualizado.getNombre());
            producto.setDescripcion(productoActualizado.getDescripcion());
            producto.setPrecio(precioNuevo);
            producto.setUrlImagen(productoActualizado.getUrlImagen());
            producto.setUrlProducto(productoActualizado.getUrlProducto());
            producto.setSeccion(productoActualizado.getSeccion());
            producto.setCategoria(productoActualizado.getCategoria());
            producto.setTienda(productoActualizado.getTienda());

            aplicarOfertaSiPrecioBaja(producto, precioAnterior, precioNuevo);

            return productoRepository.save(producto);
        }

        return null;
    }

    private void aplicarOfertaSiPrecioBaja(Producto producto, BigDecimal precioAnterior, BigDecimal precioNuevo) {
        if (producto == null || precioAnterior == null || precioNuevo == null) {
            return;
        }

        if (precioAnterior.compareTo(BigDecimal.ZERO) <= 0 || precioNuevo.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        if (precioNuevo.compareTo(precioAnterior) < 0) {
            producto.setPrecioOriginal(precioAnterior);
            producto.setEnOferta(true);

            BigDecimal descuento = precioAnterior
                    .subtract(precioNuevo)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(precioAnterior, 0, RoundingMode.HALF_UP);

            producto.setPorcentajeDescuento(descuento.intValue());
            producto.setNuevaColeccion(false);
            return;
        }

        if (Boolean.TRUE.equals(producto.getEnOferta())
                && producto.getPrecioOriginal() != null
                && precioNuevo.compareTo(producto.getPrecioOriginal()) >= 0) {

            producto.setPrecioOriginal(null);
            producto.setPorcentajeDescuento(null);
            producto.setEnOferta(false);
        }
    }

    public ResultadoScrapingDTO scrapearYGuardarConResultado() {
        return ejecutarScrapingConBloqueo(
                TipoScrapingPendiente.TOTAL,
                gestorScraping::scrapearTodo,
                OrigenScrapingEjecucion.MANUAL,
                false
        );
    }

    public ResultadoScrapingDTO scrapearZaraYGuardarConResultado() {
        return ejecutarScrapingConBloqueo(
                TipoScrapingPendiente.ZARA,
                gestorScraping::scrapearZara,
                OrigenScrapingEjecucion.MANUAL,
                false
        );
    }

    public ResultadoScrapingDTO scrapearBershkaYGuardarConResultado() {
        return ejecutarScrapingConBloqueo(
                TipoScrapingPendiente.BERSHKA,
                gestorScraping::scrapearBershka,
                OrigenScrapingEjecucion.MANUAL,
                false
        );
    }

    public ResultadoScrapingDTO scrapearPullAndBearYGuardarConResultado() {
        return ejecutarScrapingConBloqueo(
                TipoScrapingPendiente.PULL_AND_BEAR,
                gestorScraping::scrapearPullAndBear,
                OrigenScrapingEjecucion.MANUAL,
                false
        );
    }

    public EstadoScrapingAdminDTO obtenerEstadoScrapingAdmin() {
        EstadoScrapingAdminDTO estado = new EstadoScrapingAdminDTO();
        estado.setRelayHabilitado(scrapingRelayEnabled);
        estado.setAutomaticoHabilitado(scrapingAutomaticoEnabled);
        estado.setFrecuenciaAutomaticaMs(scrapingAutomaticoFixedDelayMs);
        estado.setIntervaloReintentoMs(scrapingRelayRetryIntervalMs);

        if (scrapingRelayEnabled) {
            actualizarEstadoRelayActual(estado);
        } else {
            estado.setRelayDisponible(true);
            estado.setRelayMensaje("El scraping se ejecuta directamente en el servidor.");
        }

        List<ScrapingPendiente> pendientes = scrapingPendienteRepository.findTop5ByEstadoOrderByFechaCreacionAsc(
                EstadoScrapingPendiente.PENDIENTE
        );
        estado.setTotalPendientes((int) scrapingPendienteRepository.countByEstado(EstadoScrapingPendiente.PENDIENTE));
        estado.setPendientes(
                pendientes.stream()
                        .map(this::mapearPendienteAdmin)
                        .toList()
        );

        Optional<ScrapingEjecucion> ultimaEjecucion = scrapingEjecucionRepository.findTopByOrderByFechaInicioDesc();
        ultimaEjecucion.ifPresent(ejecucion -> {
            estado.setUltimaEjecucion(mapearEjecucionAdmin(ejecucion));
            estado.setScrapingEnCurso(ejecucion.getEstado() == EstadoScrapingEjecucion.EN_CURSO);
        });

        Optional<ScrapingEjecucion> ultimaFinalizada = scrapingEjecucionRepository.findTop5ByOrderByFechaInicioDesc()
                .stream()
                .filter(ejecucion -> ejecucion.getEstado() != EstadoScrapingEjecucion.EN_CURSO)
                .findFirst();

        ultimaFinalizada.ifPresent(ejecucion -> estado.setUltimoResultado(mapearResultadoDesdeEjecucion(ejecucion)));

        return estado;
    }

    @Scheduled(
            initialDelayString = "${app.scraping.auto.initial-delay-ms:300000}",
            fixedDelayString = "${app.scraping.auto.fixed-delay-ms:21600000}"
    )
    public void ejecutarScrapingAutomaticoProgramado() {
        if (!scrapingAutomaticoEnabled) {
            return;
        }

        ejecutarScrapingConBloqueo(
                TipoScrapingPendiente.TOTAL,
                gestorScraping::scrapearTodo,
                OrigenScrapingEjecucion.AUTOMATICO,
                true
        );
    }

    @Scheduled(fixedDelayString = "${app.scraping.relay.retry-interval-ms:60000}")
    public void reintentarScrapingsPendientes() {
        if (!scrapingRelayEnabled) {
            return;
        }

        if (!scrapingLock.tryLock()) {
            return;
        }

        try {
            List<ScrapingPendiente> pendientes = scrapingPendienteRepository.findTop3ByEstadoOrderByFechaCreacionAsc(
                    EstadoScrapingPendiente.PENDIENTE
            );

            for (ScrapingPendiente scrapingPendiente : pendientes) {
                long inicio = System.currentTimeMillis();
                ScrapingEjecucion ejecucion = registrarInicioEjecucion(
                        scrapingPendiente.getTipo(),
                        OrigenScrapingEjecucion.REINTENTO_PENDIENTE
                );

                try {
                    ResultadoScrapingDTO resultado = procesarScrapingRelay(scrapingPendiente.getTipo(), ejecucion);
                    resultado.setPendiente(false);
                    resultado.setDuracionMs(System.currentTimeMillis() - inicio);

                    marcarScrapingComoProcesado(scrapingPendiente);
                    marcarPendientesDuplicadosComoProcesados(scrapingPendiente.getTipo());
                    finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.COMPLETADO, resultado, null);
                } catch (Exception e) {
                    registrarIntentoFallido(scrapingPendiente, e);

                    ResultadoScrapingDTO resultadoPendiente = new ResultadoScrapingDTO(
                            scrapingPendiente.getTipo().getNombreProceso()
                    );
                    resultadoPendiente.setPendiente(true);
                    resultadoPendiente.setMensajeEstado(
                            construirMensajePendienteScraping(scrapingPendiente.getTipo(), scrapingPendiente)
                    );
                    resultadoPendiente.setDuracionMs(System.currentTimeMillis() - inicio);

                    finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.PENDIENTE, resultadoPendiente, e);
                }
            }
        } finally {
            scrapingLock.unlock();
        }
    }

    private ResultadoScrapingDTO ejecutarScrapingConBloqueo(TipoScrapingPendiente tipoScraping,
                                                            Supplier<List<Producto>> scrapingLocal,
                                                            OrigenScrapingEjecucion origen,
                                                            boolean omitirSiYaHayScraping) {
        boolean lockAdquirido;

        if (omitirSiYaHayScraping) {
            lockAdquirido = scrapingLock.tryLock();

            if (!lockAdquirido) {
                registrarEjecucionOmitida(
                        tipoScraping,
                        origen,
                        "Se omitio la ejecucion porque ya hay otro scraping en curso."
                );
                return crearResultadoScrapingOmitido(
                        tipoScraping,
                        "Se omitio la ejecucion porque ya hay otro scraping en curso."
                );
            }
        } else {
            scrapingLock.lock();
            lockAdquirido = true;
        }

        try {
            return ejecutarScraping(tipoScraping, scrapingLocal, origen);
        } finally {
            if (lockAdquirido) {
                scrapingLock.unlock();
            }
        }
    }

    private ResultadoScrapingDTO ejecutarScraping(TipoScrapingPendiente tipoScraping,
                                                  Supplier<List<Producto>> scrapingLocal,
                                                  OrigenScrapingEjecucion origen) {
        long inicio = System.currentTimeMillis();
        ScrapingEjecucion ejecucion = registrarInicioEjecucion(tipoScraping, origen);

        try {
            ResultadoScrapingDTO resultado = scrapingRelayEnabled
                    ? procesarScrapingRelay(tipoScraping, ejecucion)
                    : procesarScrapingLocal(tipoScraping, scrapingLocal, ejecucion);

            resultado.setPendiente(false);
            resultado.setDuracionMs(System.currentTimeMillis() - inicio);

            if (scrapingRelayEnabled) {
                marcarPendientesDuplicadosComoProcesados(tipoScraping);
            }

            finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.COMPLETADO, resultado, null);
            return resultado;
        } catch (Exception e) {
            if (!scrapingRelayEnabled) {
                finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.ERROR, null, e);
                throw new RuntimeException("No se pudo completar el scraping de " + tipoScraping.getNombreProceso(), e);
            }

            ScrapingPendiente scrapingPendiente = guardarScrapingPendiente(tipoScraping, e);
            ResultadoScrapingDTO resultadoPendiente = new ResultadoScrapingDTO(tipoScraping.getNombreProceso());
            resultadoPendiente.setPendiente(true);
            resultadoPendiente.setMensajeEstado(
                    construirMensajePendienteScraping(tipoScraping, scrapingPendiente)
            );
            resultadoPendiente.setDuracionMs(System.currentTimeMillis() - inicio);
            finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.PENDIENTE, resultadoPendiente, e);
            return resultadoPendiente;
        }
    }

    private ResultadoScrapingDTO procesarScrapingLocal(TipoScrapingPendiente tipoScraping,
                                                       Supplier<List<Producto>> scrapingLocal,
                                                       ScrapingEjecucion ejecucion) {
        actualizarMensajeEjecucion(
                ejecucion,
                "El scraping se esta ejecutando en el servidor."
        );
        List<Producto> productosScrapeados = scrapingLocal.get();
        actualizarMensajeEjecucion(
                ejecucion,
                "El scraping ha terminado. Estamos insertando los productos en la base de datos."
        );
        return guardarProductosScrapeadosConResultado(
                tipoScraping,
                tipoScraping.getNombreProceso(),
                productosScrapeados,
                ejecucion
        );
    }

    private ResultadoScrapingDTO procesarScrapingRelay(TipoScrapingPendiente tipoScraping,
                                                       ScrapingEjecucion ejecucion)
            throws IOException, InterruptedException {
        actualizarMensajeEjecucion(
                ejecucion,
                "Conectando con el puente local de scraping."
        );

        List<Producto> productosScrapeados = solicitarProductosRelay(tipoScraping, ejecucion);

        actualizarMensajeEjecucion(
                ejecucion,
                "El scraping ha terminado en tu equipo local. Estamos insertando los productos en la base de datos."
        );

        return guardarProductosScrapeadosConResultado(
                tipoScraping,
                tipoScraping.getNombreProceso(),
                productosScrapeados,
                ejecucion
        );
    }

    private ResultadoScrapingDTO guardarProductosScrapeadosConResultado(TipoScrapingPendiente tipoScraping,
                                                                        String nombreProceso,
                                                                        List<Producto> productosScrapeados,
                                                                        ScrapingEjecucion ejecucion) {
        ResultadoScrapingDTO resultado = new ResultadoScrapingDTO(nombreProceso);
        Map<String, ResultadoScrapingTiendaDTO> resultadosPorTienda = new LinkedHashMap<>();
        Map<String, Set<String>> urlsEncontradasPorTienda = new LinkedHashMap<>();
        LocalDateTime fechaSincronizacion = LocalDateTime.now();

        if (productosScrapeados == null || productosScrapeados.isEmpty()) {
            resultado.setMensajeEstado(nombreProceso + " finalizado sin productos para insertar.");
            resultado.setResultadosPorTienda(new ArrayList<>(resultadosPorTienda.values()));
            return resultado;
        }

        for (Producto producto : productosScrapeados) {
            if (producto == null) {
                continue;
            }

            String nombreTienda = obtenerNombreTiendaScraping(producto);

            ResultadoScrapingTiendaDTO resultadoTienda = resultadosPorTienda.computeIfAbsent(
                    nombreTienda,
                    ResultadoScrapingTiendaDTO::new
            );

            resultado.sumarProductoEncontrado();
            resultadoTienda.sumarProductoEncontrado();

            if (productoSinImagen(producto)) {
                resultado.sumarProductoSinImagen();
                resultadoTienda.sumarProductoSinImagen();
            }

            if (productoSinPrecio(producto)) {
                resultado.sumarProductoSinPrecio();
                resultadoTienda.sumarProductoSinPrecio();
            }

            normalizarProductoScrapeado(producto);

            Tienda tiendaScrapeada = producto.getTienda();

            if (tiendaScrapeada != null) {
                Optional<Tienda> tiendaExistente = tiendaRepository.findByNombre(tiendaScrapeada.getNombre());

                if (tiendaExistente.isPresent()) {
                    producto.setTienda(tiendaExistente.get());
                } else {
                    producto.setTienda(tiendaRepository.save(tiendaScrapeada));
                }
            }

            Categoria categoriaScrapeada = producto.getCategoria();

            if (categoriaScrapeada != null) {
                Optional<Categoria> categoriaExistente = categoriaRepository.findByNombre(categoriaScrapeada.getNombre());

                if (categoriaExistente.isPresent()) {
                    producto.setCategoria(categoriaExistente.get());
                } else {
                    producto.setCategoria(categoriaRepository.save(categoriaScrapeada));
                }
            }

            activarProductoParaCatalogo(producto, fechaSincronizacion);

            if (producto.getUrlProducto() != null
                    && !producto.getUrlProducto().isBlank()
                    && producto.getTienda() != null
                    && producto.getTienda().getNombre() != null
                    && !producto.getTienda().getNombre().isBlank()) {
                urlsEncontradasPorTienda
                        .computeIfAbsent(producto.getTienda().getNombre(), clave -> new LinkedHashSet<>())
                        .add(producto.getUrlProducto());
            }

            Optional<Producto> existente = Optional.empty();

            if (producto.getUrlProducto() != null && !producto.getUrlProducto().isBlank()) {
                existente = productoRepository.findByUrlProducto(producto.getUrlProducto());
            }

            if (existente.isPresent()) {
                Producto productoExistente = existente.get();

                historialPrecioProductoService.registrarCambioSiCorresponde(
                        productoExistente,
                        producto,
                        ejecucion,
                        fechaSincronizacion
                ).ifPresent(cambioPrecio -> {
                    resultado.registrarCambioPrecio(cambioPrecio);
                    resultadoTienda.registrarCambioPrecio(cambioPrecio);
                    notificacionUsuarioService.crearNotificacionesPorCambioFavoritos(productoExistente, cambioPrecio);
                });

                productoExistente.setNombre(producto.getNombre());
                productoExistente.setDescripcion(producto.getDescripcion());
                productoExistente.setPrecio(producto.getPrecio());
                productoExistente.setPrecioOriginal(producto.getPrecioOriginal());
                productoExistente.setPorcentajeDescuento(producto.getPorcentajeDescuento());
                productoExistente.setEnOferta(producto.getEnOferta() != null ? producto.getEnOferta() : false);
                productoExistente.setNuevaColeccion(producto.getNuevaColeccion() != null ? producto.getNuevaColeccion() : false);
                productoExistente.setUrlImagen(producto.getUrlImagen());
                productoExistente.setUrlProducto(producto.getUrlProducto());
                productoExistente.setSeccion(producto.getSeccion());
                productoExistente.setCategoria(producto.getCategoria());
                productoExistente.setTienda(producto.getTienda());
                activarProductoParaCatalogo(productoExistente, fechaSincronizacion);

                productoRepository.save(productoExistente);

                resultado.sumarProductoActualizado();
                resultado.sumarProductoGuardado();

                resultadoTienda.sumarProductoActualizado();
                resultadoTienda.sumarProductoGuardado();
            } else {
                productoRepository.save(producto);

                resultado.sumarProductoNuevo();
                resultado.sumarProductoGuardado();

                resultadoTienda.sumarProductoNuevo();
                resultadoTienda.sumarProductoGuardado();
            }
        }

        desactivarProductosNoEncontrados(urlsEncontradasPorTienda, resultadosPorTienda, resultado);
        resultado.ordenarYLimitarCambiosPrecio(LIMITE_CAMBIOS_PRECIO_RESUMEN);
        resultado.setResultadosPorTienda(new ArrayList<>(resultadosPorTienda.values()));
        resultado.setMensajeEstado(nombreProceso + " finalizado correctamente.");

        return resultado;
    }

    private void normalizarProductoScrapeado(Producto producto) {
        if (producto == null) {
            return;
        }

        if (producto.getUrlProducto() != null) {
            producto.setUrlProducto(producto.getUrlProducto().trim());
        }

        if (producto.getUrlImagen() != null) {
            producto.setUrlImagen(producto.getUrlImagen().trim());
        }
    }

    private void activarProductoParaCatalogo(Producto producto, LocalDateTime fechaSincronizacion) {
        if (producto == null) {
            return;
        }

        producto.setDisponibleCatalogo(true);
        producto.setUltimaVezVistoEnScraping(fechaSincronizacion);
        producto.setFechaDesactivacion(null);
        producto.setMotivoDesactivacion(null);
    }

    private void desactivarProductosNoEncontrados(Map<String, Set<String>> urlsEncontradasPorTienda,
                                                  Map<String, ResultadoScrapingTiendaDTO> resultadosPorTienda,
                                                  ResultadoScrapingDTO resultado) {
        if (urlsEncontradasPorTienda == null || urlsEncontradasPorTienda.isEmpty()) {
            return;
        }

        LocalDateTime fechaDesactivacion = LocalDateTime.now();

        for (Map.Entry<String, Set<String>> entry : urlsEncontradasPorTienda.entrySet()) {
            String nombreTienda = entry.getKey();
            Set<String> urlsDetectadas = entry.getValue();

            if (nombreTienda == null || nombreTienda.isBlank() || urlsDetectadas == null || urlsDetectadas.isEmpty()) {
                continue;
            }

            ResultadoScrapingTiendaDTO resultadoTienda = resultadosPorTienda.computeIfAbsent(
                    nombreTienda,
                    ResultadoScrapingTiendaDTO::new
            );

            List<Producto> productosActivosDeTienda = productoRepository.findDisponiblesCatalogoPorTienda(nombreTienda);

            for (Producto productoExistente : productosActivosDeTienda) {
                String urlProductoExistente = productoExistente.getUrlProducto();

                if (urlProductoExistente == null || urlProductoExistente.isBlank()) {
                    continue;
                }

                if (urlsDetectadas.contains(urlProductoExistente.trim())) {
                    continue;
                }

                productoExistente.setDisponibleCatalogo(false);
                productoExistente.setFechaDesactivacion(fechaDesactivacion);
                productoExistente.setMotivoDesactivacion("NO_ENCONTRADO_EN_SCRAPING");
                productoRepository.save(productoExistente);

                resultado.sumarProductoDesactivado();
                resultadoTienda.sumarProductoDesactivado();
            }
        }
    }

    private String obtenerNombreTiendaScraping(Producto producto) {
        if (producto == null || producto.getTienda() == null || producto.getTienda().getNombre() == null) {
            return "Sin tienda";
        }

        return producto.getTienda().getNombre();
    }

    private boolean productoSinImagen(Producto producto) {
        return producto.getUrlImagen() == null || producto.getUrlImagen().isBlank();
    }

    private boolean productoSinPrecio(Producto producto) {
        return producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0;
    }

    private List<Producto> solicitarProductosRelay(TipoScrapingPendiente tipoScraping,
                                                   ScrapingEjecucion ejecucion)
            throws IOException, InterruptedException {
        validarConfiguracionScrapingRelay();

        ScrapingRelayRequestDTO solicitud = new ScrapingRelayRequestDTO(tipoScraping.name());
        String payload = objectMapper.writeValueAsString(solicitud);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(scrapingRelayReceiverUrl))
                .timeout(Duration.ofMillis(scrapingRelayReadTimeoutMs))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("X-Relay-Token", scrapingRelayToken)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        actualizarMensajeEjecucion(
                ejecucion,
                "El scraping se esta ejecutando en tu equipo local. Estamos esperando los datos."
        );

        HttpResponse<String> response = crearHttpClientScraping().send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Relay local de scraping no disponible. Codigo HTTP: " + response.statusCode());
        }

        ScrapingRelayResponseDTO respuesta = objectMapper.readValue(
                response.body(),
                ScrapingRelayResponseDTO.class
        );

        return respuesta.getProductos() == null ? List.of() : respuesta.getProductos();
    }

    private void validarConfiguracionScrapingRelay() {
        if (scrapingRelayReceiverUrl == null || scrapingRelayReceiverUrl.isBlank()) {
            throw new RuntimeException("Falta configurar app.scraping.relay.receiver-url");
        }

        if (scrapingRelayToken == null || scrapingRelayToken.isBlank()) {
            throw new RuntimeException("Falta configurar app.scraping.relay.token");
        }
    }

    private ScrapingPendiente guardarScrapingPendiente(TipoScrapingPendiente tipoScraping, Exception e) {
        Optional<ScrapingPendiente> existente = scrapingPendienteRepository.findFirstByTipoAndEstadoOrderByFechaCreacionAsc(
                tipoScraping,
                EstadoScrapingPendiente.PENDIENTE
        );

        ScrapingPendiente scrapingPendiente = existente.orElseGet(ScrapingPendiente::new);

        scrapingPendiente.setTipo(tipoScraping);
        scrapingPendiente.setEstado(EstadoScrapingPendiente.PENDIENTE);
        scrapingPendiente.setIntentos((scrapingPendiente.getIntentos() == null ? 0 : scrapingPendiente.getIntentos()) + 1);
        scrapingPendiente.setUltimoError(limpiarMensajeErrorScraping(e));
        scrapingPendiente.setFechaUltimoIntento(LocalDateTime.now());

        if (scrapingPendiente.getFechaCreacion() == null) {
            scrapingPendiente.setFechaCreacion(LocalDateTime.now());
        }

        return scrapingPendienteRepository.save(scrapingPendiente);
    }

    private void marcarScrapingComoProcesado(ScrapingPendiente scrapingPendiente) {
        scrapingPendiente.setEstado(EstadoScrapingPendiente.PROCESADO);
        scrapingPendiente.setFechaProcesado(LocalDateTime.now());
        scrapingPendiente.setFechaUltimoIntento(LocalDateTime.now());
        scrapingPendiente.setUltimoError(null);
        scrapingPendienteRepository.save(scrapingPendiente);
    }

    private void registrarIntentoFallido(ScrapingPendiente scrapingPendiente, Exception e) {
        scrapingPendiente.setIntentos((scrapingPendiente.getIntentos() == null ? 0 : scrapingPendiente.getIntentos()) + 1);
        scrapingPendiente.setFechaUltimoIntento(LocalDateTime.now());
        scrapingPendiente.setUltimoError(limpiarMensajeErrorScraping(e));
        scrapingPendienteRepository.save(scrapingPendiente);
    }

    private void marcarPendientesDuplicadosComoProcesados(TipoScrapingPendiente tipoScraping) {
        List<ScrapingPendiente> pendientesDuplicados = scrapingPendienteRepository.findByTipoAndEstado(
                tipoScraping,
                EstadoScrapingPendiente.PENDIENTE
        );

        if (pendientesDuplicados.isEmpty()) {
            return;
        }

        LocalDateTime fechaProcesado = LocalDateTime.now();

        for (ScrapingPendiente pendiente : pendientesDuplicados) {
            pendiente.setEstado(EstadoScrapingPendiente.PROCESADO);
            pendiente.setFechaProcesado(fechaProcesado);
            pendiente.setFechaUltimoIntento(fechaProcesado);
            pendiente.setUltimoError(null);
        }

        scrapingPendienteRepository.saveAll(pendientesDuplicados);
    }

    private String construirMensajePendienteScraping(TipoScrapingPendiente tipoScraping,
                                                     ScrapingPendiente scrapingPendiente) {
        boolean yaExistiaPendiente = scrapingPendiente.getIntentos() != null && scrapingPendiente.getIntentos() > 1;

        if (yaExistiaPendiente) {
            return tipoScraping.getNombreProceso()
                    + " sigue pendiente. Se lanzara automaticamente en cuanto tu equipo local vuelva a estar disponible.";
        }

        return tipoScraping.getNombreProceso()
                + " queda pendiente. Se ejecutara automaticamente en cuanto tu equipo local vuelva a estar disponible.";
    }

    private String limpiarMensajeErrorScraping(Exception e) {
        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            return "No se pudo contactar con el relay local de scraping.";
        }

        return e.getMessage();
    }

    private ScrapingEjecucion registrarInicioEjecucion(TipoScrapingPendiente tipoScraping,
                                                       OrigenScrapingEjecucion origen) {
        ScrapingEjecucion ejecucion = new ScrapingEjecucion();
        ejecucion.setTipo(tipoScraping);
        ejecucion.setOrigen(origen);
        ejecucion.setEstado(EstadoScrapingEjecucion.EN_CURSO);
        ejecucion.setRelayHabilitado(scrapingRelayEnabled);
        ejecucion.setFechaInicio(LocalDateTime.now());
        ejecucion.setMensajeEstado("Preparando solicitud de scraping.");
        return scrapingEjecucionRepository.save(ejecucion);
    }

    private void actualizarMensajeEjecucion(ScrapingEjecucion ejecucion, String mensajeEstado) {
        if (ejecucion == null || mensajeEstado == null || mensajeEstado.isBlank()) {
            return;
        }

        if (Objects.equals(ejecucion.getMensajeEstado(), mensajeEstado)) {
            return;
        }

        ejecucion.setMensajeEstado(mensajeEstado);
        scrapingEjecucionRepository.save(ejecucion);
    }

    private void registrarEjecucionOmitida(TipoScrapingPendiente tipoScraping,
                                           OrigenScrapingEjecucion origen,
                                           String mensaje) {
        ScrapingEjecucion ejecucion = registrarInicioEjecucion(tipoScraping, origen);
        ResultadoScrapingDTO resultado = crearResultadoScrapingOmitido(tipoScraping, mensaje);
        finalizarEjecucion(ejecucion, EstadoScrapingEjecucion.OMITIDO, resultado, null);
    }

    private ResultadoScrapingDTO crearResultadoScrapingOmitido(TipoScrapingPendiente tipoScraping,
                                                               String mensaje) {
        ResultadoScrapingDTO resultado = new ResultadoScrapingDTO(tipoScraping.getNombreProceso());
        resultado.setPendiente(false);
        resultado.setMensajeEstado(mensaje);
        resultado.setDuracionMs(0);
        return resultado;
    }

    private void finalizarEjecucion(ScrapingEjecucion ejecucion,
                                    EstadoScrapingEjecucion estado,
                                    ResultadoScrapingDTO resultado,
                                    Exception exception) {
        if (ejecucion == null) {
            return;
        }

        LocalDateTime fechaFin = LocalDateTime.now();
        ejecucion.setEstado(estado);
        ejecucion.setFechaFin(fechaFin);

        if (ejecucion.getFechaInicio() != null) {
            ejecucion.setDuracionMs(Duration.between(ejecucion.getFechaInicio(), fechaFin).toMillis());
        }

        if (resultado != null) {
            ejecucion.setTotalProductosEncontrados(resultado.getTotalProductosEncontrados());
            ejecucion.setTotalProductosGuardados(resultado.getTotalProductosGuardados());
            ejecucion.setTotalProductosNuevos(resultado.getTotalProductosNuevos());
            ejecucion.setTotalProductosActualizados(resultado.getTotalProductosActualizados());
            ejecucion.setTotalProductosDesactivados(resultado.getTotalProductosDesactivados());
            ejecucion.setTotalProductosCambioPrecio(resultado.getTotalProductosCambioPrecio());
            ejecucion.setTotalProductosBajadaPrecio(resultado.getTotalProductosBajadaPrecio());
            ejecucion.setTotalProductosSubidaPrecio(resultado.getTotalProductosSubidaPrecio());
            ejecucion.setTotalProductosRebajaMayor(resultado.getTotalProductosRebajaMayor());
            ejecucion.setMensajeEstado(resultado.getMensajeEstado());
        }

        if (exception != null) {
            ejecucion.setDetalleError(limpiarMensajeErrorScraping(exception));
        }

        scrapingEjecucionRepository.save(ejecucion);
        scrapingResumenAdminService.enviarResumenSiCorresponde(ejecucion, resultado, exception);
    }

    private HttpClient crearHttpClientScraping() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(scrapingRelayConnectTimeoutMs))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private void actualizarEstadoRelayActual(EstadoScrapingAdminDTO estado) {
        try {
            HttpResponse<String> response = crearHttpClientEstadoRelay().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(construirPingUrlRelay()))
                            .timeout(Duration.ofMillis(obtenerReadTimeoutEstadoRelay()))
                            .header("X-Relay-Token", scrapingRelayToken)
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                estado.setRelayDisponible(true);
                estado.setRelayMensaje("Puente local conectado y listo para scrapear.");
                return;
            }

            estado.setRelayDisponible(false);
            estado.setRelayMensaje("El puente local no respondio correctamente (HTTP " + response.statusCode() + ").");
        } catch (Exception e) {
            estado.setRelayDisponible(false);
            estado.setRelayMensaje("Servidor local apagado o relay no disponible ahora mismo.");
        }
    }

    private HttpClient crearHttpClientEstadoRelay() {
        long timeoutConexion = Math.max(750L, Math.min(scrapingRelayConnectTimeoutMs, 1500L));

        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutConexion))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private long obtenerReadTimeoutEstadoRelay() {
        return Math.max(1000L, Math.min(scrapingRelayReadTimeoutMs, 3000L));
    }

    private String construirPingUrlRelay() {
        if (scrapingRelayReceiverUrl == null || scrapingRelayReceiverUrl.isBlank()) {
            return "http://127.0.0.1:8095/internal/mail-relay/ping";
        }

        if (scrapingRelayReceiverUrl.endsWith("/scraping")) {
            return scrapingRelayReceiverUrl.substring(0, scrapingRelayReceiverUrl.length() - "/scraping".length()) + "/ping";
        }

        return scrapingRelayReceiverUrl + "/ping";
    }

    private ScrapingPendienteAdminDTO mapearPendienteAdmin(ScrapingPendiente scrapingPendiente) {
        ScrapingPendienteAdminDTO dto = new ScrapingPendienteAdminDTO();
        dto.setId(scrapingPendiente.getId());
        dto.setTipo(scrapingPendiente.getTipo() == null ? null : scrapingPendiente.getTipo().name());
        dto.setNombreProceso(scrapingPendiente.getTipo() == null ? "Scraping" : scrapingPendiente.getTipo().getNombreProceso());
        dto.setEstado(scrapingPendiente.getEstado() == null ? null : scrapingPendiente.getEstado().name());
        dto.setIntentos(scrapingPendiente.getIntentos());
        dto.setUltimoError(scrapingPendiente.getUltimoError());
        dto.setFechaCreacion(scrapingPendiente.getFechaCreacion());
        dto.setFechaUltimoIntento(scrapingPendiente.getFechaUltimoIntento());
        return dto;
    }

    private ScrapingEjecucionAdminDTO mapearEjecucionAdmin(ScrapingEjecucion ejecucion) {
        ScrapingEjecucionAdminDTO dto = new ScrapingEjecucionAdminDTO();
        dto.setId(ejecucion.getId());
        dto.setTipo(ejecucion.getTipo() == null ? null : ejecucion.getTipo().name());
        dto.setNombreProceso(ejecucion.getTipo() == null ? "Scraping" : ejecucion.getTipo().getNombreProceso());
        dto.setOrigen(ejecucion.getOrigen() == null ? null : ejecucion.getOrigen().name());
        dto.setEstado(ejecucion.getEstado() == null ? null : ejecucion.getEstado().name());
        dto.setRelayHabilitado(ejecucion.getRelayHabilitado());
        dto.setFechaInicio(ejecucion.getFechaInicio());
        dto.setFechaFin(ejecucion.getFechaFin());
        dto.setDuracionMs(ejecucion.getDuracionMs());
        dto.setTotalProductosEncontrados(ejecucion.getTotalProductosEncontrados());
        dto.setTotalProductosGuardados(ejecucion.getTotalProductosGuardados());
        dto.setTotalProductosNuevos(ejecucion.getTotalProductosNuevos());
        dto.setTotalProductosActualizados(ejecucion.getTotalProductosActualizados());
        dto.setTotalProductosDesactivados(ejecucion.getTotalProductosDesactivados());
        dto.setTotalProductosCambioPrecio(ejecucion.getTotalProductosCambioPrecio());
        dto.setTotalProductosBajadaPrecio(ejecucion.getTotalProductosBajadaPrecio());
        dto.setTotalProductosSubidaPrecio(ejecucion.getTotalProductosSubidaPrecio());
        dto.setMensajeEstado(ejecucion.getMensajeEstado());
        dto.setDetalleError(ejecucion.getDetalleError());
        return dto;
    }

    private ResultadoScrapingDTO mapearResultadoDesdeEjecucion(ScrapingEjecucion ejecucion) {
        ResultadoScrapingDTO resultado = new ResultadoScrapingDTO(
                ejecucion.getTipo() == null ? "Scraping" : ejecucion.getTipo().getNombreProceso()
        );
        resultado.setTotalProductosEncontrados(valorSeguro(ejecucion.getTotalProductosEncontrados()));
        resultado.setTotalProductosGuardados(valorSeguro(ejecucion.getTotalProductosGuardados()));
        resultado.setTotalProductosNuevos(valorSeguro(ejecucion.getTotalProductosNuevos()));
        resultado.setTotalProductosActualizados(valorSeguro(ejecucion.getTotalProductosActualizados()));
        resultado.setTotalProductosDesactivados(valorSeguro(ejecucion.getTotalProductosDesactivados()));
        resultado.setTotalProductosCambioPrecio(valorSeguro(ejecucion.getTotalProductosCambioPrecio()));
        resultado.setTotalProductosBajadaPrecio(valorSeguro(ejecucion.getTotalProductosBajadaPrecio()));
        resultado.setTotalProductosSubidaPrecio(valorSeguro(ejecucion.getTotalProductosSubidaPrecio()));
        resultado.setDuracionMs(ejecucion.getDuracionMs() == null ? 0 : ejecucion.getDuracionMs());
        resultado.setPendiente(ejecucion.getEstado() == EstadoScrapingEjecucion.PENDIENTE);
        resultado.setMensajeEstado(ejecucion.getMensajeEstado());
        return resultado;
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    public List<Producto> obtenerPorTienda(String nombreTienda) {
        List<Producto> productos = productoRepository.findByTiendaNombre(nombreTienda);

        for (Producto producto : productos) {
            List<ProductoTallaStock> tallaStocksExistentes =
                    productoTallaStockRepository.findByProductoId(producto.getId());

            List<ProductoTallaStock> tallaStocksCompletos = Arrays.stream(Talla.values())
                    .map(tallaEnum -> {
                        ProductoTallaStock tallaEncontrada = tallaStocksExistentes.stream()
                                .filter(item -> item.getTalla() == tallaEnum)
                                .findFirst()
                                .orElse(null);

                        if (tallaEncontrada != null) {
                            return tallaEncontrada;
                        }

                        ProductoTallaStock nueva = new ProductoTallaStock();
                        nueva.setProducto(producto);
                        nueva.setTalla(tallaEnum);
                        nueva.setStock(0);
                        return nueva;
                    })
                    .toList();

            producto.setTallaStocks(tallaStocksCompletos);
        }

        return productos;
    }

    public ProductoPageResponseDTO buscarProductos(String tienda,
                                                   List<Seccion> secciones,
                                                   List<String> categorias,
                                                   String busqueda,
                                                   String orden,
                                                   Boolean enOferta,
                                                   Boolean nuevaColeccion,
                                                   Boolean incluirNoDisponibles,
                                                   int page,
                                                   int size) {
        int pagina = Math.max(page, 0);
        int tamano = Math.min(Math.max(size, 1), 48);

        Pageable pageable = PageRequest.of(pagina, tamano, obtenerOrdenProductos(orden));
        Specification<Producto> specification = crearEspecificacionProductos(
                tienda,
                secciones,
                categorias,
                busqueda,
                enOferta,
                nuevaColeccion,
                Boolean.TRUE.equals(incluirNoDisponibles),
                false
        );

        Page<Producto> productosPage = productoRepository.findAll(specification, pageable);

        List<Long> productoIds = productosPage.getContent()
                .stream()
                .map(Producto::getId)
                .toList();

        Map<Long, List<ProductoTallaStock>> tallasPorProducto = productoIds.isEmpty()
                ? Map.of()
                : productoTallaStockRepository.findByProductoIdIn(productoIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getProducto().getId()));

        Map<Long, List<ProductoImagen>> imagenesPorProducto = productoIds.isEmpty()
                ? Map.of()
                : productoImagenRepository.findByProductoIdInOrderByProductoIdAscOrdenAsc(productoIds)
                .stream()
                .collect(Collectors.groupingBy(imagen -> imagen.getProducto().getId()));

        List<ProductoListadoDTO> productosDTO = productosPage.getContent()
                .stream()
                .map(producto -> convertirAProductoListadoDTO(
                        producto,
                        tallasPorProducto.get(producto.getId()),
                        imagenesPorProducto.get(producto.getId())
                ))
                .toList();

        return new ProductoPageResponseDTO(
                productosDTO,
                productosPage.getNumber(),
                productosPage.getTotalPages(),
                productosPage.getTotalElements(),
                productosPage.isLast()
        );
    }

    public List<ProductoSeleccionStockDTO> buscarProductosSinStockParaSeleccion(String tienda,
                                                                                 List<Seccion> secciones,
                                                                                 List<String> categorias,
                                                                                 String busqueda,
                                                                                 Boolean incluirNoDisponibles) {
        Specification<Producto> specification = crearEspecificacionProductos(
                tienda,
                secciones,
                categorias,
                busqueda,
                null,
                null,
                Boolean.TRUE.equals(incluirNoDisponibles),
                true
        );

        return productoRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::convertirAProductoSeleccionStockDTO)
                .toList();
    }

    public List<String> obtenerCategoriasCatalogo(String tienda,
                                                  List<Seccion> secciones,
                                                  Boolean incluirNoDisponibles) {
        String tiendaLimpia = tienda == null ? "" : tienda.trim();
        boolean incluirNoDisponiblesValor = Boolean.TRUE.equals(incluirNoDisponibles);

        List<Seccion> seccionesValidas = secciones == null
                ? List.of()
                : secciones.stream()
                .filter(Objects::nonNull)
                .toList();

        if (incluirNoDisponiblesValor) {
            return obtenerCategoriasCatalogoIncluyendoNoDisponibles(tiendaLimpia, seccionesValidas);
        }

        if (!tiendaLimpia.isBlank() && !seccionesValidas.isEmpty()) {
            return productoRepository.findCategoriasDistintasPorTiendaYSecciones(tiendaLimpia, seccionesValidas);
        }

        if (!tiendaLimpia.isBlank()) {
            return productoRepository.findCategoriasDistintasPorTienda(tiendaLimpia);
        }

        if (!seccionesValidas.isEmpty()) {
            return productoRepository.findCategoriasDistintasPorSecciones(seccionesValidas);
        }

        return productoRepository.findCategoriasDistintas();
    }

    private List<String> obtenerCategoriasCatalogoIncluyendoNoDisponibles(String tienda,
                                                                          List<Seccion> secciones) {
        Specification<Producto> specification = crearEspecificacionProductos(
                tienda,
                secciones,
                null,
                null,
                null,
                null,
                true,
                false
        );

        return productoRepository.findAll(specification)
                .stream()
                .map(Producto::getCategoria)
                .filter(Objects::nonNull)
                .map(Categoria::getNombre)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private boolean productoDisponibleEnCatalogo(Producto producto) {
        return producto != null && !Boolean.FALSE.equals(producto.getDisponibleCatalogo());
    }

    private Sort obtenerOrdenProductos(String orden) {
        if (orden == null || orden.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        return switch (orden) {
            case "precio-asc", "precioAsc" -> Sort.by(Sort.Direction.ASC, "precio");
            case "precio-desc", "precioDesc" -> Sort.by(Sort.Direction.DESC, "precio");
            case "nombre-asc", "nombreAsc" -> Sort.by(Sort.Direction.ASC, "nombre");
            case "nombre-desc", "nombreDesc" -> Sort.by(Sort.Direction.DESC, "nombre");
            case "recientes" -> Sort.by(Sort.Direction.DESC, "id");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }

    private Specification<Producto> crearEspecificacionProductos(String tienda,
                                                                 List<Seccion> secciones,
                                                                 List<String> categorias,
                                                                 String busqueda,
                                                                 Boolean enOferta,
                                                                 Boolean nuevaColeccion,
                                                                 boolean incluirNoDisponibles,
                                                                 boolean soloSinStock) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);

            Join<Producto, Tienda> tiendaJoin = root.join("tienda", JoinType.LEFT);
            Join<Producto, Categoria> categoriaJoin = root.join("categoria", JoinType.LEFT);

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (!incluirNoDisponibles) {
                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.isTrue(root.get("disponibleCatalogo")),
                                criteriaBuilder.isNull(root.get("disponibleCatalogo"))
                        )
                );
            }

            if (tienda != null && !tienda.isBlank()) {
                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(tiendaJoin.get("nombre")),
                                tienda.trim().toLowerCase()
                        )
                );
            }

            if (enOferta != null) {
                predicates.add(criteriaBuilder.equal(root.get("enOferta"), enOferta));
            }

            if (nuevaColeccion != null) {
                predicates.add(criteriaBuilder.equal(root.get("nuevaColeccion"), nuevaColeccion));
            }

            if (secciones != null && !secciones.isEmpty()) {
                List<Seccion> seccionesValidas = secciones.stream()
                        .filter(Objects::nonNull)
                        .toList();

                if (!seccionesValidas.isEmpty()) {
                    predicates.add(root.get("seccion").in(seccionesValidas));
                }
            }

            if (categorias != null && !categorias.isEmpty()) {
                List<String> categoriasLimpias = categorias.stream()
                        .filter(categoriaActual -> categoriaActual != null && !categoriaActual.isBlank())
                        .map(categoriaActual -> categoriaActual.trim().toLowerCase())
                        .toList();

                if (!categoriasLimpias.isEmpty()) {
                    List<jakarta.persistence.criteria.Predicate> predicatesCategoria = new ArrayList<>();

                    for (String categoriaActual : categoriasLimpias) {
                        predicatesCategoria.add(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(categoriaJoin.get("nombre")),
                                        "%" + categoriaActual + "%"
                                )
                        );
                    }

                    predicates.add(
                            criteriaBuilder.or(predicatesCategoria.toArray(new jakarta.persistence.criteria.Predicate[0]))
                    );
                }
            }

            if (busqueda != null && !busqueda.isBlank()) {
                String termino = "%" + busqueda.trim().toLowerCase() + "%";

                predicates.add(
                        criteriaBuilder.or(
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(criteriaBuilder.coalesce(root.get("nombre"), "")),
                                        termino
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(criteriaBuilder.coalesce(categoriaJoin.get("nombre"), "")),
                                        termino
                                ),
                                criteriaBuilder.like(
                                        criteriaBuilder.lower(criteriaBuilder.coalesce(tiendaJoin.get("nombre"), "")),
                                        termino
                                )
                        )
                );
            }

            if (soloSinStock) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<ProductoTallaStock> stockRoot = subquery.from(ProductoTallaStock.class);

                subquery.select(stockRoot.get("producto").get("id"));
                subquery.where(
                        criteriaBuilder.equal(stockRoot.get("producto").get("id"), root.get("id")),
                        criteriaBuilder.greaterThan(stockRoot.get("stock"), 0)
                );

                predicates.add(criteriaBuilder.not(criteriaBuilder.exists(subquery)));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private ProductoListadoDTO convertirAProductoListadoDTO(Producto producto,
                                                            List<ProductoTallaStock> tallaStocks,
                                                            List<ProductoImagen> imagenes) {
        CategoriaSimpleDTO categoriaDTO = null;

        if (producto.getCategoria() != null) {
            categoriaDTO = new CategoriaSimpleDTO(
                    producto.getCategoria().getId(),
                    producto.getCategoria().getNombre()
            );
        }

        TiendaSimpleDTO tiendaDTO = null;

        if (producto.getTienda() != null) {
            tiendaDTO = new TiendaSimpleDTO(
                    producto.getTienda().getId(),
                    producto.getTienda().getNombre(),
                    producto.getTienda().getUrl()
            );
        }

        ProductoListadoDTO dto = new ProductoListadoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioOriginal(),
                producto.getPorcentajeDescuento(),
                producto.getEnOferta(),
                producto.getUrlImagen(),
                producto.getUrlProducto(),
                producto.getSeccion(),
                categoriaDTO,
                tiendaDTO,
                construirTallasCompletas(tallaStocks),
                construirImagenesProducto(imagenes)
        );

        dto.setDisponibleCatalogo(producto.getDisponibleCatalogo());
        return dto;
    }

    private ProductoSeleccionStockDTO convertirAProductoSeleccionStockDTO(Producto producto) {
        CategoriaSimpleDTO categoriaDTO = null;

        if (producto.getCategoria() != null) {
            categoriaDTO = new CategoriaSimpleDTO(
                    producto.getCategoria().getId(),
                    producto.getCategoria().getNombre()
            );
        }

        TiendaSimpleDTO tiendaDTO = null;

        if (producto.getTienda() != null) {
            tiendaDTO = new TiendaSimpleDTO(
                    producto.getTienda().getId(),
                    producto.getTienda().getNombre(),
                    producto.getTienda().getUrl()
            );
        }

        return new ProductoSeleccionStockDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getSeccion(),
                categoriaDTO,
                tiendaDTO
        );
    }

    private List<ProductoImagenResponseDTO> construirImagenesProducto(List<ProductoImagen> imagenes) {
        if (imagenes == null || imagenes.isEmpty()) {
            return List.of();
        }

        return imagenes.stream()
                .filter(imagen -> imagen.getUrlImagen() != null && !imagen.getUrlImagen().isBlank())
                .sorted(Comparator.comparingInt(ProductoImagen::getOrden))
                .map(imagen -> new ProductoImagenResponseDTO(
                        imagen.getId(),
                        imagen.getUrlImagen(),
                        imagen.getOrden()
                ))
                .toList();
    }

    private List<ProductoTallaStockResponseDTO> construirTallasCompletas(List<ProductoTallaStock> tallaStocks) {
        Map<Talla, ProductoTallaStock> tallasExistentes = tallaStocks == null
                ? Map.of()
                : tallaStocks.stream()
                .collect(Collectors.toMap(
                        ProductoTallaStock::getTalla,
                        item -> item,
                        (item1, item2) -> item1
                ));

        return Arrays.stream(Talla.values())
                .map(tallaEnum -> {
                    ProductoTallaStockResponseDTO dto = new ProductoTallaStockResponseDTO();
                    dto.setTalla(tallaEnum);

                    ProductoTallaStock tallaEncontrada = tallasExistentes.get(tallaEnum);

                    if (tallaEncontrada != null) {
                        dto.setStock(tallaEncontrada.getStock());
                    } else {
                        dto.setStock(0);
                    }

                    return dto;
                })
                .toList();
    }

    @Transactional
    public void asignarTallaStock(ProductoTallaStockDTO dto){
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("producto no encontrado"));

        Optional<ProductoTallaStock> existente =
                productoTallaStockRepository.findByProductoIdAndTalla(dto.getProductoId(), dto.getTalla());

        if (existente.isPresent()) {
            ProductoTallaStock productoTallaStockExistente = existente.get();
            productoTallaStockExistente.setStock(dto.getStock());
            productoTallaStockRepository.save(productoTallaStockExistente);
        } else {
            ProductoTallaStock productoTallaStock = new ProductoTallaStock();
            productoTallaStock.setProducto(producto);
            productoTallaStock.setTalla(dto.getTalla());
            productoTallaStock.setStock(dto.getStock());

            productoTallaStockRepository.save(productoTallaStock);
        }
    }

    @Transactional
    public void asignarTallaStockMasivo(ProductoTallaStockMasivoDTO dto) {
        if (dto == null || dto.getStock() == null || dto.getStock() < 0) {
            throw new IllegalArgumentException("Stock no valido");
        }

        List<Long> productoIds = dto.getProductoIds() == null
                ? List.of()
                : dto.getProductoIds().stream().filter(Objects::nonNull).distinct().toList();

        List<Talla> tallas = dto.getTallas() == null
                ? List.of()
                : dto.getTallas().stream().filter(Objects::nonNull).distinct().toList();

        if (productoIds.isEmpty() || tallas.isEmpty()) {
            throw new IllegalArgumentException("Debes indicar productos y tallas");
        }

        List<Producto> productos = productoRepository.findAllById(productoIds);

        if (productos.size() != productoIds.size()) {
            throw new RuntimeException("No se encontraron todos los productos seleccionados");
        }

        Map<Long, Producto> productosPorId = productos.stream()
                .collect(Collectors.toMap(Producto::getId, producto -> producto));

        Map<String, ProductoTallaStock> existentesPorClave = productoTallaStockRepository.findByProductoIdIn(productoIds)
                .stream()
                .collect(Collectors.toMap(
                        item -> construirClaveStock(item.getProducto().getId(), item.getTalla()),
                        item -> item,
                        (item1, item2) -> item1
                ));

        List<ProductoTallaStock> stockAGuardar = new ArrayList<>();

        for (Long productoId : productoIds) {
            Producto producto = productosPorId.get(productoId);

            for (Talla talla : tallas) {
                String clave = construirClaveStock(productoId, talla);
                ProductoTallaStock productoTallaStock = existentesPorClave.get(clave);

                if (productoTallaStock == null) {
                    productoTallaStock = new ProductoTallaStock();
                    productoTallaStock.setProducto(producto);
                    productoTallaStock.setTalla(talla);
                }

                productoTallaStock.setStock(dto.getStock());
                stockAGuardar.add(productoTallaStock);
            }
        }

        productoTallaStockRepository.saveAll(stockAGuardar);
    }

    public List<ProductoTallaStockResponseDTO> obtenerTallasStockPorProducto(Long productoId,
                                                                             Boolean incluirNoDisponibles) {
        Producto producto = obtenerPorId(productoId, incluirNoDisponibles);

        if (producto == null) {
            throw new RuntimeException("Producto no disponible");
        }

        List<ProductoTallaStock> lista = productoTallaStockRepository.findByProductoId(productoId);

        return Arrays.stream(Talla.values())
                .map(tallaEnum -> {
                    ProductoTallaStockResponseDTO dto = new ProductoTallaStockResponseDTO();
                    dto.setTalla(tallaEnum);

                    ProductoTallaStock tallaEncontrada = lista.stream()
                            .filter(item -> item.getTalla() == tallaEnum)
                            .findFirst()
                            .orElse(null);

                    if (tallaEncontrada != null) {
                        dto.setStock(tallaEncontrada.getStock());
                    } else {
                        dto.setStock(0);
                    }

                    return dto;
                })
                .toList();
    }

    private String construirClaveStock(Long productoId, Talla talla) {
        return productoId + "::" + (talla == null ? "" : talla.name());
    }
}
