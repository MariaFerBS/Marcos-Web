
package com.marcos.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marcos.proyecto.modelo.DetalleVenta;

public interface DetalleVentaRepository
        extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByIdVenta(Long idVenta);
}
