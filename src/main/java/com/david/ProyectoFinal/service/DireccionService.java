package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.ActualizarDireccionDTO;
import com.david.ProyectoFinal.dto.CrearDireccionDTO;
import com.david.ProyectoFinal.dto.DireccionDTO;
import com.david.ProyectoFinal.model.Direccion;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.DireccionRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DireccionService(DireccionRepository direccionRepository, UsuarioRepository usuarioRepository) {
        this.direccionRepository = direccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<DireccionDTO> obtenerDireccionesDeUsuario(Long usuarioId) {
        List<Direccion> direcciones = direccionRepository.findByUsuarioId(usuarioId);
        List<DireccionDTO> resultado = new ArrayList<>();

        for (Direccion direccion : direcciones) {
            resultado.add(convertirADTO(direccion));
        }

        return resultado;
    }

    @Transactional
    public DireccionDTO crearDireccion(Long usuarioId, CrearDireccionDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarDireccion(dto.getAlias(), dto.getProvincia(), dto.getMunicipio(),
                dto.getCalle(), dto.getNumero(), dto.getCodigoPostal());

        if (dto.isPrincipal()) {
            quitarDireccionPrincipalActual(usuarioId);
        }

        Direccion direccion = new Direccion();
        direccion.setAlias(dto.getAlias().trim());
        direccion.setProvincia(dto.getProvincia().trim());
        direccion.setMunicipio(dto.getMunicipio().trim());
        direccion.setCalle(dto.getCalle().trim());
        direccion.setNumero(dto.getNumero().trim());
        direccion.setPiso(dto.getPiso() != null ? dto.getPiso().trim() : "");
        direccion.setPuerta(dto.getPuerta() != null ? dto.getPuerta().trim() : "");
        direccion.setCodigoPostal(dto.getCodigoPostal().trim());
        direccion.setPrincipal(dto.isPrincipal());
        direccion.setUsuario(usuario);

        Direccion guardada = direccionRepository.save(direccion);

        return convertirADTO(guardada);
    }

    @Transactional
    public DireccionDTO actualizarDireccion(Long usuarioId, Long direccionId, ActualizarDireccionDTO dto) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        validarDireccion(dto.getAlias(), dto.getProvincia(), dto.getMunicipio(),
                dto.getCalle(), dto.getNumero(), dto.getCodigoPostal());

        if (dto.isPrincipal()) {
            quitarDireccionPrincipalActual(usuarioId);
        }

        direccion.setAlias(dto.getAlias().trim());
        direccion.setProvincia(dto.getProvincia().trim());
        direccion.setMunicipio(dto.getMunicipio().trim());
        direccion.setCalle(dto.getCalle().trim());
        direccion.setNumero(dto.getNumero().trim());
        direccion.setPiso(dto.getPiso() != null ? dto.getPiso().trim() : "");
        direccion.setPuerta(dto.getPuerta() != null ? dto.getPuerta().trim() : "");
        direccion.setCodigoPostal(dto.getCodigoPostal().trim());
        direccion.setPrincipal(dto.isPrincipal());

        Direccion actualizada = direccionRepository.save(direccion);

        return convertirADTO(actualizada);
    }

    @Transactional
    public void eliminarDireccion(Long usuarioId, Long direccionId) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        direccionRepository.delete(direccion);
    }

    @Transactional
    public DireccionDTO marcarComoPrincipal(Long usuarioId, Long direccionId) {
        Direccion direccion = direccionRepository.findByIdAndUsuarioId(direccionId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        quitarDireccionPrincipalActual(usuarioId);

        direccion.setPrincipal(true);
        Direccion actualizada = direccionRepository.save(direccion);

        return convertirADTO(actualizada);
    }

    private void quitarDireccionPrincipalActual(Long usuarioId) {
        List<Direccion> principales = direccionRepository.findByUsuarioIdAndPrincipalTrue(usuarioId);

        for (Direccion direccion : principales) {
            direccion.setPrincipal(false);
            direccionRepository.save(direccion);
        }
    }

    private void validarDireccion(String alias, String provincia, String municipio,
                                  String calle, String numero, String codigoPostal) {

        if (alias == null || alias.trim().isBlank()) {
            throw new RuntimeException("El alias de la dirección es obligatorio");
        }

        if (provincia == null || provincia.trim().isBlank()) {
            throw new RuntimeException("La provincia es obligatoria");
        }

        if (municipio == null || municipio.trim().isBlank()) {
            throw new RuntimeException("El municipio es obligatorio");
        }

        if (calle == null || calle.trim().isBlank()) {
            throw new RuntimeException("La calle es obligatoria");
        }

        if (numero == null || numero.trim().isBlank()) {
            throw new RuntimeException("El número es obligatorio");
        }

        if (codigoPostal == null || codigoPostal.trim().isBlank()) {
            throw new RuntimeException("El código postal es obligatorio");
        }

        if (!codigoPostal.trim().matches("\\d{5}")) {
            throw new RuntimeException("El código postal debe tener 5 números");
        }
    }

    private DireccionDTO convertirADTO(Direccion direccion) {
        return new DireccionDTO(
                direccion.getId(),
                direccion.getAlias(),
                direccion.getProvincia(),
                direccion.getMunicipio(),
                direccion.getCalle(),
                direccion.getNumero(),
                direccion.getPiso(),
                direccion.getPuerta(),
                direccion.getCodigoPostal(),
                direccion.isPrincipal()
        );
    }
}
