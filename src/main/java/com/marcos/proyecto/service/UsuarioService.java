
package com.marcos.proyecto.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.marcos.proyecto.modelo.Usuario;
import com.marcos.proyecto.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    // INICIAR SESIÓN
    public Usuario autenticar(String email, String password) {

        if (email == null || password == null) {
            return null;
        }

        Usuario usuario = usuarioRepository
                .findByEmailUsIgnoreCase(email)
                .orElse(null);

        if (usuario == null) {
            return null;
        }

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            return null;
        }

        if (passwordEncoder.matches(password, usuario.getPasswordUs())) {
            return usuario;
        }

        return null;
    }

    // CAMBIAR CONTRASEÑA
    public void cambiarPassword(String email, String nuevaPassword) {

        if (nuevaPassword == null || nuevaPassword.length() < 12) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 12 caracteres"
            );
        }

        Usuario usuario = usuarioRepository
                .findByEmailUsIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario"
                ));

        usuario.setPasswordUs(
                passwordEncoder.encode(nuevaPassword)
        );

        usuarioRepository.save(usuario);
    }

    // REGISTRAR USUARIO
    public Usuario registrar(Usuario usuario, String password) {

        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 12 caracteres"
            );
        }

        if (usuarioRepository
                .findByEmailUsIgnoreCase(usuario.getEmailUs())
                .isPresent()) {

            throw new IllegalArgumentException(
                    "El correo ya está registrado"
            );
        }

        usuario.setIdUs(null);
        usuario.setPasswordUs(passwordEncoder.encode(password));
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }
}
