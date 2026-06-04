package com.david.mailrelay;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class MailRelaySecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Desactiva CSRF para que el servidor pueda hacer POST al relay
                .csrf(csrf -> csrf.disable())

                // Permitimos que llegue al controlador.
                // La seguridad real la hace MailRelayController con X-Relay-Token.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())

                // Quitamos login básico
                .httpBasic(httpBasic -> httpBasic.disable())

                // Quitamos formulario de login
                .formLogin(formLogin -> formLogin.disable())

                // Quitamos logout porque aquí no hay sesión de usuario
                .logout(logout -> logout.disable())

                .build();
    }
}