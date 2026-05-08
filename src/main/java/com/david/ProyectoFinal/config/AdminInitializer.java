package com.david.ProyectoFinal.config;

import com.david.ProyectoFinal.model.Rol;
import com.david.ProyectoFinal.model.Usuario;
import com.david.ProyectoFinal.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public AdminInitializer(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        String nombreAdmin = "admin";
        String emailAdmin = "admin@admin.com";

        boolean existeAdminPorNombre = usuarioRepository.existsByNombreIgnoreCase(nombreAdmin);
        boolean existeAdminPorEmail = usuarioRepository.existsByEmailIgnoreCase(emailAdmin);

        if (existeAdminPorNombre || existeAdminPorEmail) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombre(nombreAdmin);
        admin.setEmail(emailAdmin);
        admin.setPassword("1234");
        admin.setRol(Rol.ADMIN);
        admin.setFotoPerfilUrl(null);
        admin.setFormaFotoPerfil("CIRCULAR");

        usuarioRepository.save(admin);

        System.out.println("Usuario ADMIN creado automáticamente.");
    }
}