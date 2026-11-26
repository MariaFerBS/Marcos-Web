
package com.marcos.proyecto.service;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcos.proyecto.modelo.Movimiento;
import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.repository.MovimientoRepository;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ProductoService productoService;

    public MovimientoService(
            MovimientoRepository movimientoRepository,
            ProductoService productoService) {

        this.movimientoRepository = movimientoRepository;
        this.productoService = productoService;
    }

    // LISTAR TODOS LOS MOVIMIENTOS
    @Transactional(readOnly = true)
    public List<Movimiento> listar() {
        return movimientoRepository.findAll();
    }

    // LISTAR LOS ÚLTIMOS CINCO
    @Transactional(readOnly = true)
    public List<Movimiento> listarUltimos() {
        return movimientoRepository.findTop5ByOrderByIdMovDesc();
    }

    // CONTAR ENTRADAS DE HOY
    @Transactional(readOnly = true)
    public long totalEntradasHoy() {
        return movimientoRepository
                .countByTipoMovIgnoreCaseAndFecMov(
                        "ENTRADA", LocalDate.now()
                );
    }

    // CONTAR SALIDAS DE HOY
    @Transactional(readOnly = true)
    public long totalSalidasHoy() {
        return movimientoRepository
                .countByTipoMovIgnoreCaseAndFecMov(
                        "SALIDA", LocalDate.now()
                );
    }

    // REGISTRAR UN MOVIMIENTO
    @Transactional
    public void registrarMovimiento(
            Long idProd,
            String tipoMov,
            int cantidad,
            String motivo,
            Long idUs) {

        if (idProd == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un producto."
            );
        }

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
            );
        }

        if (tipoMov == null ||
                (!"ENTRADA".equalsIgnoreCase(tipoMov)
                 && !"SALIDA".equalsIgnoreCase(tipoMov))) {

            throw new IllegalArgumentException(
                    "Tipo de movimiento no válido."
            );
        }

        Producto producto = productoService.obtener(idProd);

        if ("ENTRADA".equalsIgnoreCase(tipoMov)) {
            productoService.aumentarStock(idProd, cantidad);
        } else {
            productoService.descontarStock(idProd, cantidad);
        }

        Movimiento movimiento = new Movimiento(
                null,
                idProd,
                idUs != null ? idUs : 1L,
                tipoMov.toUpperCase(),
                cantidad,
                LocalDate.now(),
                motivo,
                producto.getNomProd()
        );

        movimientoRepository.save(movimiento);
    }

    // REGISTRAR VARIOS PRODUCTOS EN UNA SOLA TRANSACCIÓN
    @Transactional
    public void registrarEntradaMultiple(
            List<Long> idsProd,
            List<Integer> cantidades,
            String motivo,
            Long idUs) {

        if (idsProd == null || idsProd.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe agregar al menos un producto."
            );
        }

        if (cantidades == null ||
                cantidades.size() != idsProd.size()) {

            throw new IllegalArgumentException(
                    "Los productos y cantidades no coinciden."
            );
        }

        // VALIDAR TODAS LAS FILAS ANTES DE MODIFICAR STOCK
        for (int i = 0; i < idsProd.size(); i++) {

            if (idsProd.get(i) == null ||
                    cantidades.get(i) == null ||
                    cantidades.get(i) <= 0) {

                throw new IllegalArgumentException(
                        "Producto o cantidad inválida en la fila "
                        + (i + 1)
                );
            }
        }

        // GUARDAR TODA LA ENTRADA EN UNA TRANSACCIÓN
        for (int i = 0; i < idsProd.size(); i++) {

            registrarMovimiento(
                    idsProd.get(i),
                    "ENTRADA",
                    cantidades.get(i),
                    motivo,
                    idUs
            );
        }
    }
}
