package com.david.ProyectoFinal.controller;


import com.david.ProyectoFinal.dto.PagoRequestDTO;
import com.david.ProyectoFinal.dto.PagoResponseDTO;
import com.david.ProyectoFinal.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
     }


    @PostMapping("/procesar")
    public ResponseEntity<PagoResponseDTO> procesarPago(@RequestBody PagoRequestDTO dto){
        PagoResponseDTO response = pagoService.procesarPago(dto);
        return ResponseEntity.ok(response);
    }
}
