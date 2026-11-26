
package com.marcos.proyecto.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marcos.proyecto.modelo.Proveedor;
import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.ProveedorService;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // VERIFICAR USUARIO ACTIVO EN MYSQL
    private boolean usuarioActivo(HttpSession session) {

        Object idSesion = session.getAttribute("usuarioLogueado");

        if (!(idSesion instanceof Number)) {
            return false;
        }

        Long id = ((Number) idSesion).longValue();

        return usuarioRepository.findById(id)
                .filter(usuario ->
                        Boolean.TRUE.equals(usuario.getActivo()))
                .isPresent();
    }

    // VERIFICAR ADMINISTRADOR EN MYSQL
    private boolean esAdministrador(HttpSession session) {

        Object idSesion = session.getAttribute("usuarioLogueado");

        if (!(idSesion instanceof Number)) {
            return false;
        }

        Long id = ((Number) idSesion).longValue();

        return usuarioRepository.findById(id)
                .filter(usuario ->
                        Boolean.TRUE.equals(usuario.getActivo()))
                .filter(usuario ->
                        "ADMIN".equals(usuario.getRolUs()))
                .isPresent();
    }

    // LISTA Y BUSCADOR: ADMIN Y VENDEDOR
    @GetMapping
    public String lista(
            @RequestParam(required = false) String q,
            Model model,
            HttpSession session) {

        if (!usuarioActivo(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "proveedores",
                proveedorService.buscar(q)
        );

        model.addAttribute("q", q);

        return "proveedores/lista";
    }

    // NUEVO PROVEEDOR: SOLO ADMIN
    @GetMapping("/nuevo")
    public String nuevo(
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        model.addAttribute(
                "proveedor",
                new Proveedor()
        );

        return "proveedores/formulario";
    }

    // EDITAR PROVEEDOR: SOLO ADMIN
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        Proveedor prov = proveedorService.obtener(id);

        if (prov == null) {
            return "redirect:/proveedores";
        }

        model.addAttribute("proveedor", prov);

        return "proveedores/formulario";
    }

    // GUARDAR PROVEEDOR: SOLO ADMIN
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Proveedor proveedor,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        proveedorService.guardar(proveedor);

        flash.addFlashAttribute(
                "msg",
                "Proveedor guardado exitosamente"
        );

        return "redirect:/proveedores";
    }

    // ELIMINAR PROVEEDOR: SOLO ADMIN
    // Se mantiene GET temporalmente para no romper
    // los enlaces existentes de la interfaz.
    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        proveedorService.eliminar(id);

        flash.addFlashAttribute(
                "msg",
                "Proveedor eliminado correctamente"
        );

        return "redirect:/proveedores";
    }
}
