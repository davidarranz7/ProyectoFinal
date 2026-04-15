package com.david.ProyectoFinal.service;

import com.david.ProyectoFinal.dto.GuardarTarjetaDTO;
import com.david.ProyectoFinal.dto.TarjetaDTO;
import com.david.ProyectoFinal.model.Tarjeta;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.TarjetaRepository;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TarjetaService {

    private final TarjetaRepository tarjetaRepository;
    private final UsuarioRepository usuarioRepository;

    public TarjetaService(TarjetaRepository tarjetaRepository, UsuarioRepository usuarioRepository) {
        this.tarjetaRepository = tarjetaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TarjetaDTO> obtenerTarjetasPorUsuario(Long usuarioId) {
        return tarjetaRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    public TarjetaDTO guardarTarjeta(Long usuarioId, GuardarTarjetaDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        validarDatosTarjeta(dto);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setTitular(dto.getTitular().trim());
        tarjeta.setNumeroEnmascarado(enmascararNumero(dto.getNumeroTarjeta()));
        tarjeta.setFechaExpiracion(dto.getFechaExpiracion().trim());
        tarjeta.setTipo(dto.getTipo());
        tarjeta.setUsuario(usuario);

        Tarjeta tarjetaGuardada = tarjetaRepository.save(tarjeta);

        return convertirADTO(tarjetaGuardada);
    }

    public Tarjeta obtenerPorId(Long tarjetaId) {
        return tarjetaRepository.findById(tarjetaId)
                .orElseThrow(() -> new RuntimeException("Tarjeta no encontrada"));
    }

    public void eliminarTarjeta(Long tarjetaId) {
        if (!tarjetaRepository.existsById(tarjetaId)) {
            throw new RuntimeException("Tarjeta no encontrada");
        }

        tarjetaRepository.deleteById(tarjetaId);
    }

    private void validarDatosTarjeta(GuardarTarjetaDTO dto) {
        if (dto.getTitular() == null || dto.getTitular().isBlank()) {
            throw new RuntimeException("El titular es obligatorio");
        }

        if (dto.getNumeroTarjeta() == null || dto.getNumeroTarjeta().isBlank()) {
            throw new RuntimeException("El número de tarjeta es obligatorio");
        }

        String numeroLimpio = dto.getNumeroTarjeta().replaceAll("\\s+", "");

        if (!numeroLimpio.matches("\\d{16}")) {
            throw new RuntimeException("La tarjeta debe tener exactamente 16 dígitos");
        }

        if (dto.getFechaExpiracion() == null || dto.getFechaExpiracion().isBlank()) {
            throw new RuntimeException("La fecha de expiración es obligatoria");
        }

        if (!dto.getFechaExpiracion().matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new RuntimeException("La fecha de expiración debe tener formato MM/AA");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");
        YearMonth fechaTarjeta = YearMonth.parse(dto.getFechaExpiracion(), formatter);
        YearMonth fechaActual = YearMonth.now();

        if (fechaTarjeta.isBefore(fechaActual)) {
            throw new RuntimeException("La tarjeta está caducada");
        }

        if (dto.getTipo() == null) {
            throw new RuntimeException("El tipo de tarjeta es obligatorio");
        }
    }

    private String enmascararNumero(String numeroTarjeta) {
        String numeroLimpio = numeroTarjeta.replaceAll("\\s+", "");
        String ultimos4 = numeroLimpio.substring(numeroLimpio.length() - 4);
        return "**** **** **** " + ultimos4;
    }

    private TarjetaDTO convertirADTO(Tarjeta tarjeta) {
        return new TarjetaDTO(
                tarjeta.getId(),
                tarjeta.getTitular(),
                tarjeta.getNumeroEnmascarado(),
                tarjeta.getFechaExpiracion(),
                tarjeta.getTipo()
        );
    }
}