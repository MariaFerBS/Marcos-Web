
package com.marcos.proyecto.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.marcos.proyecto.modelo.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailUsIgnoreCase(String emailUs);

}
