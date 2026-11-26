
package com.marcos.proyecto.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcos.proyecto.dto.TipoCambioDTO;
import com.marcos.proyecto.service.TipoCambioService;

@RestController
@RequestMapping("/api")
public class TipoCambioController {

    private final TipoCambioService tipoCambioService;

    public TipoCambioController(TipoCambioService tipoCambioService) {
        this.tipoCambioService = tipoCambioService;
    }

    @GetMapping("/tipo-cambio")
    public TipoCambioDTO consultarTipoCambio() {
        return tipoCambioService.consultarTipoCambio();
    }
}
