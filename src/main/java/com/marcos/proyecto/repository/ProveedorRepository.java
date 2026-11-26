
package com.marcos.proyecto.repository;

import com.marcos.proyecto.modelo.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository
        extends JpaRepository<Proveedor, Long> {
}
