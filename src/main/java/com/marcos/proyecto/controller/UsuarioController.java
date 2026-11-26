
package com.marcos.proyecto.controller;

import com.marcos.proyecto.modelo.Usuario;
import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    // COMPROBAR ADMINISTRADOR ACTIVO EN MYSQL
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

    // LISTAR USUARIOS
    @GetMapping
    public String listar(Model model, HttpSession session) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        model.addAttribute("usuarios", usuarioRepository.findAll());

        return "usuarios/lista";
    }

    // MOSTRAR FORMULARIO
    @GetMapping("/nuevo")
    public String nuevo(HttpSession session) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        return "usuarios/formulario";
    }

    // GUARDAR USUARIO
    @PostMapping("/guardar")
    public String guardar(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String rol,
            HttpSession session,
            Model model) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        try {

            nombre = nombre.trim();
            email = email.trim();

            if (nombre.isEmpty() || nombre.length() > 100) {
                throw new IllegalArgumentException(
                        "Ingrese un nombre válido de máximo 100 caracteres"
                );
            }

            if (email.isEmpty() || email.length() > 50) {
                throw new IllegalArgumentException(
                        "Ingrese un usuario válido de máximo 50 caracteres"
                );
            }

            if (!"ADMIN".equals(rol) && !"VENDEDOR".equals(rol)) {
                throw new IllegalArgumentException(
                        "Seleccione un rol válido"
                );
            }

            if (password == null || password.length() < 12) {
                throw new IllegalArgumentException(
                        "La contraseña debe tener al menos 12 caracteres"
                );
            }

            Usuario usuario = new Usuario();

            usuario.setNomUs(nombre);
            usuario.setEmailUs(email);
            usuario.setRolUs(rol);

            usuarioService.registrar(usuario, password);

            return "redirect:/usuarios";

        } catch (IllegalArgumentException |
                 DataIntegrityViolationException e) {

            model.addAttribute(
                    "error",
                    e instanceof DataIntegrityViolationException
                    ? "No se pudo registrar el usuario. Verifique que no exista."
                    : e.getMessage()
            );

            return "usuarios/formulario";
        }
    }

    // DESACTIVAR USUARIO: SOLO ADMINISTRADOR
    @PostMapping("/desactivar/{id}")
    public String desactivar(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes flash) {

        if (session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        if (!esAdministrador(session)) {
            return "redirect:/dashboard";
        }

        // EVITAR QUE EL ADMINISTRADOR SE DESACTIVE A SÍ MISMO
        Long idAdministrador =
                ((Number) session.getAttribute("usuarioLogueado"))
                        .longValue();

        if (idAdministrador.equals(id)) {
            flash.addFlashAttribute(
                    "error",
                    "No puedes desactivar tu propia cuenta."
            );

            return "redirect:/usuarios";
        }

        Usuario usuario = usuarioRepository.findById(id)
                .orElse(null);

        if (usuario == null) {
            flash.addFlashAttribute(
                    "error",
                    "El usuario no existe."
            );

            return "redirect:/usuarios";
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            flash.addFlashAttribute(
                    "error",
                    "Este usuario ya está desactivado."
            );

            return "redirect:/usuarios";
        }

        // EVITAR DESACTIVAR AL ÚLTIMO ADMINISTRADOR ACTIVO
        if ("ADMIN".equals(usuario.getRolUs())) {

            long administradoresActivos = usuarioRepository.findAll()
                    .stream()
                    .filter(u -> "ADMIN".equals(u.getRolUs()))
                    .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                    .count();

            if (administradoresActivos <= 1) {
                flash.addFlashAttribute(
                        "error",
                        "No se puede desactivar al último administrador activo."
                );

                return "redirect:/usuarios";
            }
        }

        // DESACTIVACIÓN LÓGICA
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        flash.addFlashAttribute(
                "msg",
                "Usuario desactivado correctamente."
        );

        return "redirect:/usuarios";
    }
}
