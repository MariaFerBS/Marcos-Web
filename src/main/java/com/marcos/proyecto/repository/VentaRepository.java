
package com.marcos.proyecto.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.marcos.proyecto.modelo.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Listar ventas de una fecha exacta ordenadas de más reciente a más antigua
    List<Venta> findByFecVentaOrderByIdVentaDesc(LocalDate fecVenta);

    // Listar ventas en un rango de fechas
    List<Venta> findByFecVentaBetweenOrderByFecVentaDescIdVentaDesc(
            LocalDate fechaInicio,
            LocalDate fechaFin);

    // Resumen consolidado agrupado por día (Fecha, Cantidad de Ventas, Total del Día)
    @Query("SELECT v.fecVenta, COUNT(v), COALESCE(SUM(v.totalVenta), 0) " +
           "FROM Venta v " +
           "GROUP BY v.fecVenta " +
           "ORDER BY v.fecVenta DESC")
    List<Object[]> obtenerResumenPorDia();
}