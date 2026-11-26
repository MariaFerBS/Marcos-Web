
package com.marcos.proyecto.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.marcos.proyecto.modelo.Movimiento;

public interface MovimientoRepository
        extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findTop5ByOrderByIdMovDesc();

    long countByTipoMovIgnoreCaseAndFecMov(
            String tipoMov,
            LocalDate fecMov
    );
}
