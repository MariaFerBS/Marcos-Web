
package com.marcos.proyecto;

import com.marcos.proyecto.modelo.Usuario;
import com.marcos.proyecto.service.UsuarioService;
import com.marcos.proyecto.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InicializarAdmin implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public InicializarAdmin(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public void run(String... args) {

        String email = System.getenv("ADMIN_EMAIL");
        String password = System.getenv("ADMIN_PASSWORD");

        // No ejecutar si faltan las credenciales
        if (email == null || email.isBlank()
                || password == null || password.length() < 12) {
            return;
        }

        // Crear administrador únicamente
        // cuando la tabla de usuarios esté vacía
        if (usuarioRepository.count() == 0) {

            Usuario admin = new Usuario();

            admin.setNomUs("Administrador");
            admin.setEmailUs(email);
            admin.setRolUs("ADMIN");

            usuarioService.registrar(admin, password);

            System.out.println(
                "Administrador inicial registrado correctamente"
            );
        }
    }
}
