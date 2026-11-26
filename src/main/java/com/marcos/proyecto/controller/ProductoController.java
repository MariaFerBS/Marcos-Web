
package com.marcos.proyecto.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marcos.proyecto.modelo.Categoria;
import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.modelo.Proveedor;
import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.CategoriaService;
import com.marcos.proyecto.service.ProductoService;
import com.marcos.proyecto.service.ProveedorService;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // COMPROBAR USUARIO ACTIVO EN MYSQL
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

    // COMPROBAR ADMINISTRADOR EN MYSQL
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

    // LISTA Y BUSCADOR: ADMIN Y VENDEDOR
    @GetMapping
    public String lista(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long idCat,
            @RequestParam(required = false) Long idProv,
            @RequestParam(required = false) String estado,
            Model model,
            HttpSession session) {

        if (!usuarioActivo(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "productos",
                productoService.buscar(q, idCat, idProv, estado)
        );

        model.addAttribute("q", q);
        model.addAttribute("idCat", idCat);
        model.addAttribute("idProv", idProv);
        model.addAttribute("estado", estado);

        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("proveedores", proveedorService.listar());

        return "productos/lista";
    }

    // IMÁGENES DISPONIBLES
    private List<String> imagenesDisponibles() {

        Path carpeta = Paths.get("src/main/resources/static/images");

        if (!Files.exists(carpeta)) {
            return List.of();
        }

        try (Stream<Path> archivos = Files.list(carpeta)) {

            return archivos
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

        } catch (IOException e) {
            return List.of();
        }
    }

    // NUEVO PRODUCTO: SOLO ADMIN
    @GetMapping("/nuevo")
    public String nuevo(
            @RequestParam(required = false) Long idCat,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        Producto nuevoProd = new Producto();

        if (idCat != null) {
            nuevoProd.setIdCat(idCat);
        }

        model.addAttribute("producto", nuevoProd);
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("proveedores", proveedorService.listar());
        model.addAttribute("imagenes", imagenesDisponibles());

        return "productos/formulario";
    }

    // EDITAR PRODUCTO: SOLO ADMIN
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("producto", productoService.obtener(id));
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("proveedores", proveedorService.listar());
        model.addAttribute("imagenes", imagenesDisponibles());

        return "productos/formulario";
    }

    // GUARDAR PRODUCTO: SOLO ADMIN
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Producto producto,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        Categoria cat = categoriaService.obtener(producto.getIdCat());

        if (cat != null) {
            producto.setNomCat(cat.getNomCat());
        }

        Proveedor prov = proveedorService.obtener(producto.getIdProv());

        if (prov != null) {
            producto.setNomProv(prov.getNomProv());
        }

        productoService.guardar(producto);

        flash.addFlashAttribute(
                "msg",
                "Producto guardado correctamente"
        );

        return "redirect:/productos";
    }

    // ELIMINAR PRODUCTO: SOLO ADMIN
    // Se conserva temporalmente la ruta original.
    @GetMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id,
            RedirectAttributes flash,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        productoService.eliminar(id);

        flash.addFlashAttribute(
                "msg",
                "Producto eliminado"
        );

        return "redirect:/productos";
    }
}
