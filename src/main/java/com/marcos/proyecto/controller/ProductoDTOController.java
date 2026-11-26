
package com.marcos.proyecto.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.marcos.proyecto.dto.ProductoDTO;
import com.marcos.proyecto.service.ProductoService;

@RestController
@CrossOrigin(
    origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500"
    },
    allowCredentials = "true"
)
public class ProductoDTOController {

    private final ProductoService productoService;

    public ProductoDTOController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos/dto")
    public List<ProductoDTO> listarProductosDTO() {
        return productoService.listarProductosDTO();
    }
}
