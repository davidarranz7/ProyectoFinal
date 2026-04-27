package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.CrearIncidenciaRequestDTO;
import com.david.ProyectoFinal.dto.IncidenciaResponseDTO;
import com.david.ProyectoFinal.dto.MensajeIncidenciaResponseDTO;
import com.david.ProyectoFinal.model.EstadoIncidencia;

import java.util.List;

public interface IncidenciaService {

    /// Recibirá los datos del formulario público, creará la incidencia, guardará el primer mensaje y devolverá los datos principales al frontend
    IncidenciaResponseDTO crearIncidencia(CrearIncidenciaRequestDTO request);

    /// Devuelve todas las incidencias para mostrarlas en el admin
    List<IncidenciaResponseDTO> obtenerTodasLasIncidencias();

    /// Permite filtrar incidencias por estado.
    List<IncidenciaResponseDTO> obtenerIncidenciasPorEstado(EstadoIncidencia estadoIncidencia);

    /// Devuelve una incidencia concreta.
    IncidenciaResponseDTO obtenerIncidenciaPorId(Long incidenciaId);

    /// Devuelve la conversación completa de una incidencia.
    List<MensajeIncidenciaResponseDTO> obtenerMensajesDeIncidencia(Long incidenciaId);

    /// Permite al admin cambiar el estado de una incidencia.
    IncidenciaResponseDTO cambiarEstadoIncidencia(Long incidenciaId, EstadoIncidencia nuevoEstado);

    /// Permite al admin responder una incidencia, guarda el mensaje y envía correo al usuario.
    MensajeIncidenciaResponseDTO responderIncidencia(Long incidenciaId, String mensaje);

    /// Guarda una respuesta enviada por el usuario desde Gmail/correo.
    MensajeIncidenciaResponseDTO registrarRespuestaUsuarioDesdeEmail(
            String codigoSeguimiento,
            String emailRemitente,
            String contenido
    );

    /// Devuelve todos los estados posibles de una incidencia.
    List<EstadoIncidencia> obtenerEstadosIncidencia();
}