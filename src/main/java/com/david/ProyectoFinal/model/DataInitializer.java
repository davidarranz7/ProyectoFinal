package com.david.ProyectoFinal.model;

import com.david.ProyectoFinal.repository.EstablecimientoRepository;
import com.david.ProyectoFinal.repository.TiendaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initEstablecimientos(TiendaRepository tiendaRepository,
                                           EstablecimientoRepository establecimientoRepository) {
        return args -> {
            if (establecimientoRepository.count() > 0) {
                return;
            }

            Tienda zara = tiendaRepository.findByNombre("Zara").orElse(null);
            Tienda bershka = tiendaRepository.findByNombre("Bershka").orElse(null);
            Tienda pullAndBear = tiendaRepository.findByNombre("Pull&Bear").orElse(null);

            if (zara != null) {
                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Zara Vigo Vialia",
                        "CC Vialia, Vigo",
                        "Vigo",
                        "Pontevedra",
                        zara
                ));

                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Zara Gran Vía Vigo",
                        "Centro Comercial Gran Vía, Vigo",
                        "Vigo",
                        "Pontevedra",
                        zara
                ));

                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Zara Príncipe",
                        "Calle Príncipe, Vigo",
                        "Vigo",
                        "Pontevedra",
                        zara
                ));
            }

            if (bershka != null) {
                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Bershka Vigo Vialia",
                        "CC Vialia, Vigo",
                        "Vigo",
                        "Pontevedra",
                        bershka
                ));

                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Bershka Gran Vía Vigo",
                        "Centro Comercial Gran Vía, Vigo",
                        "Vigo",
                        "Pontevedra",
                        bershka
                ));
            }

            if (pullAndBear != null) {
                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Pull&Bear Vigo Vialia",
                        "CC Vialia, Vigo",
                        "Vigo",
                        "Pontevedra",
                        pullAndBear
                ));

                establecimientoRepository.save(new Establecimiento(
                        null,
                        "Pull&Bear Gran Vía Vigo",
                        "Centro Comercial Gran Vía, Vigo",
                        "Vigo",
                        "Pontevedra",
                        pullAndBear
                ));
            }
        };
    }
}
