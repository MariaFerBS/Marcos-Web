
package com.marcos.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.marcos.proyecto.service.MovimientoService;
import com.marcos.proyecto.service.ProductoService;
import com.marcos.proyecto.service.TipoCambioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private MovimientoService movimientoService;

    @Autowired
    private TipoCambioService tipoCambioService;

    @GetMapping
    public String dashboard(Model model, HttpSession session) {

        // Protección de ruta
        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        // Indicadores principales
        model.addAttribute(
                "totalProductos",
                productoService.totalProductos());

        model.addAttribute(
                "entradasHoy",
                movimientoService.totalEntradasHoy());

        model.addAttribute(
                "salidasHoy",
                movimientoService.totalSalidasHoy());

        // Productos con stock bajo
        var productosStockBajo = productoService.obtenerStockBajo();

        model.addAttribute("productosStockBajo", productosStockBajo);
        model.addAttribute("cantStockBajo", productosStockBajo.size());

        // Movimientos recientes
        model.addAttribute(
                "movimientosRecientes",
                movimientoService.listarUltimos());

        // Tipo de cambio USD a PEN
        try {
            model.addAttribute(
                    "tipoCambio",
                    tipoCambioService.consultarTipoCambio());

        } catch (Exception e) {
            model.addAttribute(
                    "errorTipoCambio",
                    "No se pudo consultar el tipo de cambio.");
        }

        return "dashboard";
    }
}
