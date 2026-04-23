package com.david.ProyectoFinal.service;


import com.david.ProyectoFinal.dto.ActualizarPerfilDTO;
import com.david.ProyectoFinal.dto.CambiarPasswordDTO;
import com.david.ProyectoFinal.dto.UsuarioPerfilDTO;
import com.david.ProyectoFinal.dto.ValidacionCampoDTO;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service/// INdica que esto es la logica de negocio
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TarjetaRepository tarjetaRepository;
    private final CarritoRepository carritoRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final FavoritoRepository favoritoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, TarjetaRepository tarjetaRepository, CarritoRepository carritoRepository, ItemCarritoRepository itemCarritoRepository, PagoRepository pagoRepository, PedidoRepository pedidoRepository, ItemPedidoRepository itemPedidoRepository, FavoritoRepository favoritoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.carritoRepository = carritoRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
        this.favoritoRepository = favoritoRepository;
    }

    public List<Usuario> obtenerTodos(){
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario){
        if (usuarioRepository.existsByNombre(usuario.getNombre())) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario obternerPorId(Long id){
        return usuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        tarjetaRepository.deleteByUsuarioId(id);
        itemCarritoRepository.deleteByCarritoUsuarioId(id);
        carritoRepository.deleteByUsuarioId(id);
        pagoRepository.deleteByUsuarioId(id);

        itemPedidoRepository.deleteByPedidoUsuarioId(id);
        pedidoRepository.deleteByUsuarioId(id);
        favoritoRepository.deleteByUsuarioId(id);

        usuarioRepository.delete(usuario);
    }

    public Usuario actutualizar(Long id, Usuario usuarioActualizado){
        Usuario usuario = usuarioRepository.findById(id).orElse(null);

        if(usuario != null){
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setEmail(usuarioActualizado.getEmail());
            usuario.setRol(usuarioActualizado.getRol());
            usuario.setFotoPerfilUrl(usuarioActualizado.getFotoPerfilUrl());

            return usuarioRepository.save(usuario);
        }
        return null;
    }

    public Usuario login(String nombre, String password) {
        for (Usuario u : usuarioRepository.findAll()) {
            if (u.getNombre().equals(nombre) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    /// por si falla en algun momento!
    /*
    public Usuario login(String nombre, String password) {
    for (Usuario u : usuarioRepository.findAll()) {
        if (u.getNombre() != null
                && u.getPassword() != null
                && u.getNombre().equals(nombre)
                && u.getPassword().equals(password)) {
            return u;
        }
    }
    return null;
}
    */

    public UsuarioPerfilDTO obtenerPerfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return new UsuarioPerfilDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getFotoPerfilUrl(),
                usuario.getFormaFotoPerfil()
        );
    }

    public UsuarioPerfilDTO actualizarPerfil(Long usuarioId, ActualizarPerfilDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nombreLimpio = dto.getNombre() != null ? dto.getNombre().trim() : "";
        String emailLimpio = dto.getEmail() != null ? dto.getEmail().trim() : "";

        if (nombreLimpio.isBlank()) {
            throw new RuntimeException("El nombre no puede estar vacío");
        }

        if (emailLimpio.isBlank()) {
            throw new RuntimeException("El email no puede estar vacío");
        }

        for (Usuario otroUsuario : usuarioRepository.findAll()) {
            if (!otroUsuario.getId().equals(usuarioId)) {
                if (otroUsuario.getNombre() != null && otroUsuario.getNombre().equalsIgnoreCase(nombreLimpio)) {
                    throw new RuntimeException("Ese nombre de usuario ya está en uso");
                }

                if (otroUsuario.getEmail() != null && otroUsuario.getEmail().equalsIgnoreCase(emailLimpio)) {
                    throw new RuntimeException("Ese email ya está en uso");
                }
            }
        }

        usuario.setNombre(nombreLimpio);
        usuario.setEmail(emailLimpio);

        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        return new UsuarioPerfilDTO(
                usuarioActualizado.getId(),
                usuarioActualizado.getNombre(),
                usuarioActualizado.getEmail(),
                usuarioActualizado.getRol().name(),
                usuarioActualizado.getFotoPerfilUrl(),
                usuarioActualizado.getFormaFotoPerfil()
        );
    }

    public UsuarioPerfilDTO subirFotoPerfil(Long usuarioId, MultipartFile foto, String formaFotoPerfil) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (foto == null || foto.isEmpty()) {
            throw new RuntimeException("Debes seleccionar una imagen");
        }

        String contentType = foto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("El archivo debe ser una imagen válida");
        }

        if (formaFotoPerfil == null || formaFotoPerfil.isBlank()) {
            formaFotoPerfil = "cuadrado";
        }

        if (!formaFotoPerfil.equals("cuadrado") && !formaFotoPerfil.equals("circulo")) {
            throw new RuntimeException("La forma de la foto no es válida");
        }

        String extension = obtenerExtensionSegunContentType(contentType);
        String nombreCarpetaUsuario = limpiarNombreParaCarpeta(usuario.getNombre());

        try {
            Path carpetaUsuario = Paths.get(
                    System.getProperty("user.dir"),
                    "uploads",
                    "fotosPerfil",
                    nombreCarpetaUsuario
            );

            Files.createDirectories(carpetaUsuario);

            eliminarFotosAnteriores(carpetaUsuario);

            String nombreArchivo = "foto-perfil." + extension;
            Path rutaArchivo = carpetaUsuario.resolve(nombreArchivo);

            Files.write(rutaArchivo, foto.getBytes());

            String urlFoto = "/uploads/fotosPerfil/" + nombreCarpetaUsuario + "/" + nombreArchivo;

            usuario.setFotoPerfilUrl(urlFoto);
            usuario.setFormaFotoPerfil(formaFotoPerfil);

            Usuario usuarioActualizado = usuarioRepository.save(usuario);

            return new UsuarioPerfilDTO(
                    usuarioActualizado.getId(),
                    usuarioActualizado.getNombre(),
                    usuarioActualizado.getEmail(),
                    usuarioActualizado.getRol().name(),
                    usuarioActualizado.getFotoPerfilUrl(),
                    usuarioActualizado.getFormaFotoPerfil()
            );

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("No se pudo guardar la imagen");
        }
    }

    private String limpiarNombreParaCarpeta(String nombreUsuario) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return "usuario_sin_nombre";
        }

        return nombreUsuario
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_");
    }

    private String obtenerExtensionSegunContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }

    private void eliminarFotosAnteriores(Path carpetaUsuario) throws IOException {
        if (!Files.exists(carpetaUsuario)) {
            return;
        }

        try (var archivos = Files.list(carpetaUsuario)) {
            archivos
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException("No se pudo eliminar la foto anterior");
                        }
                    });
        }
    }

    public void cambiarPassword(Long usuarioId, CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (dto.getPasswordActual() == null || dto.getPasswordNueva() == null || dto.getConfirmarPassword() == null) {
            throw new RuntimeException("Todos los campos son obligatorios");
        }

        if (!usuario.getPassword().equals(dto.getPasswordActual())) {
            throw new RuntimeException("La contraseña actual no es correcta");
        }

        if (!dto.getPasswordNueva().equals(dto.getConfirmarPassword())) {
            throw new RuntimeException("La nueva contraseña y la confirmación no coinciden");
        }

        if (dto.getPasswordNueva().length() < 4) {
            throw new RuntimeException("La nueva contraseña debe tener al menos 4 caracteres");
        }

        usuario.setPassword(dto.getPasswordNueva());
        usuarioRepository.save(usuario);
    }

    public ValidacionCampoDTO validarNombrePerfil(Long usuarioId, String nombre) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String nombreLimpio = nombre != null ? nombre.trim() : "";

        if (nombreLimpio.isBlank()) {
            return new ValidacionCampoDTO(false, "El nombre no puede estar vacío");
        }

        if (nombreLimpio.length() < 3) {
            return new ValidacionCampoDTO(false, "Debe tener al menos 3 caracteres");
        }

        if (usuario.getNombre() != null && usuario.getNombre().equalsIgnoreCase(nombreLimpio)) {
            return new ValidacionCampoDTO(true, "Es tu nombre actual");
        }

        boolean existe = usuarioRepository.existsByNombreIgnoreCase(nombreLimpio);

        if (existe) {
            return new ValidacionCampoDTO(false, "Ese nombre ya está en uso");
        }

        return new ValidacionCampoDTO(true, "Nombre disponible");
    }

    public ValidacionCampoDTO validarEmailPerfil(Long usuarioId, String email) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String emailLimpio = email != null ? email.trim() : "";

        if (emailLimpio.isBlank()) {
            return new ValidacionCampoDTO(false, "El email no puede estar vacío");
        }

        if (!emailLimpio.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return new ValidacionCampoDTO(false, "Formato de email no válido");
        }

        if (usuario.getEmail() != null && usuario.getEmail().equalsIgnoreCase(emailLimpio)) {
            return new ValidacionCampoDTO(true, "Es tu email actual");
        }

        boolean existe = usuarioRepository.existsByEmailIgnoreCase(emailLimpio);

        if (existe) {
            return new ValidacionCampoDTO(false, "Ese email ya está en uso");
        }

        return new ValidacionCampoDTO(true, "Email disponible");
    }

}