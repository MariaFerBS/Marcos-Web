
package com.marcos.proyecto.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.marcos.proyecto.modelo.Usuario;
import com.marcos.proyecto.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository
                .findByEmailUsIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"
                ));

        String rol = usuario.getRolUs().toUpperCase().trim();

        if (rol.startsWith("ROLE_")) {
            rol = rol.substring(5);
        }

        return User.withUsername(usuario.getEmailUs())
                .password(usuario.getPasswordUs())
                .roles(rol)
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
