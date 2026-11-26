
package com.marcos.proyecto.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marcos.proyecto.modelo.Categoria;
import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.CategoriaService;
import com.marcos.proyecto.service.ProductoService;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProductoService productoService;

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
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
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
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .filter(usuario -> "ADMIN".equals(usuario.getRolUs()))
                .isPresent();
    }

    // LISTA DE CATEGORÍAS: ADMIN Y VENDEDOR
    @GetMapping
    public String lista(
            @RequestParam(required = false) String q,
            Model model,
            HttpSession session) {

        if (!usuarioActivo(session)) {
            return "redirect:/login";
        }

        model.addAttribute("categorias", categoriaService.buscar(q));
        model.addAttribute("q", q);

        return "categorias/lista";
    }

    // DETALLE DE CATEGORÍA: ADMIN Y VENDEDOR
    @GetMapping("/detalle/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!usuarioActivo(session)) {
            return "redirect:/login";
        }

        Categoria categoria = categoriaService.obtener(id);

        if (categoria == null) {
            return "redirect:/categorias";
        }

        List<Producto> productosDeCategoria =
                productoService.listar().stream()
                        .filter(p -> id.equals(p.getIdCat()))
                        .collect(Collectors.toList());

        model.addAttribute("categoria", categoria);
        model.addAttribute("productos", productosDeCategoria);

        return "categorias/detalle";
    }

    // NUEVA CATEGORÍA: SOLO ADMIN
    @GetMapping("/nuevo")
    public String nuevo(
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("categoria", new Categoria());

        return "categorias/formulario";
    }

    // EDITAR CATEGORÍA: SOLO ADMIN
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        Categoria cat = categoriaService.obtener(id);

        if (cat == null) {
            return "redirect:/categorias";
        }

        model.addAttribute("categoria", cat);

        return "categorias/formulario";
    }

    // GUARDAR CATEGORÍA: SOLO ADMIN
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Categoria categoria,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        categoriaService.guardar(categoria);

        flash.addFlashAttribute(
                "msg",
                "Categoría guardada exitosamente"
        );

        return "redirect:/categorias";
    }

    // ELIMINAR CATEGORÍA: SOLO ADMIN
    // Temporalmente conserva GET para mantener compatibilidad.
    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        boolean tieneProductos = productoService.listar().stream()
                .anyMatch(p -> id.equals(p.getIdCat()));

        if (tieneProductos) {

            flash.addFlashAttribute(
                    "error",
                    "No se puede eliminar la categoría porque tiene productos asignados."
            );

            return "redirect:/categorias";
        }

        categoriaService.eliminar(id);

        flash.addFlashAttribute(
                "msg",
                "Categoría eliminada correctamente"
        );

        return "redirect:/categorias";
    }
}
