
package com.marcos.proyecto.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcos.proyecto.modelo.DetalleVenta;
import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.modelo.Venta;
import com.marcos.proyecto.repository.VentaRepository;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoService productoService;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoService productoService) {

        this.ventaRepository = ventaRepository;
        this.productoService = productoService;
    }

    // LISTAR VENTAS DESDE MYSQL
    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAll();
    }

    // OBTENER VENTA POR ID
    @Transactional(readOnly = true)
    public Venta obtener(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    // PROCESAR VENTA
    @Transactional
    public Venta procesarVenta(Venta venta) {

        // 1. VALIDAR LA VENTA
        if (venta == null ||
                venta.getDetalles() == null ||
                venta.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe agregar al menos un producto a la venta."
            );
        }

        // 2. AGRUPAR CANTIDADES POR PRODUCTO
        Map<Long, Integer> cantidadesPorProducto =
                new HashMap<>();

        for (DetalleVenta detalle : venta.getDetalles()) {

            if (detalle == null ||
                    detalle.getIdProd() == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar un producto válido."
                );
            }

            if (detalle.getCantDetalle() == null ||
                    detalle.getCantDetalle() <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            try {
                cantidadesPorProducto.merge(
                        detalle.getIdProd(),
                        detalle.getCantDetalle(),
                        Math::addExact
                );
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException(
                        "La cantidad solicitada es demasiado grande."
                );
            }
        }

        // 3. COMPROBAR PRODUCTOS Y STOCK
        Map<Long, Producto> productosValidados =
                new HashMap<>();

        for (Map.Entry<Long, Integer> entrada :
                cantidadesPorProducto.entrySet()) {

            Long idProducto = entrada.getKey();
            Integer cantidadSolicitada = entrada.getValue();

            Producto producto =
                    productoService.obtener(idProducto);

            if (producto == null) {
                throw new IllegalArgumentException(
                        "Uno de los productos seleccionados no existe."
                );
            }

            if (!"Activo".equalsIgnoreCase(producto.getEstado())) {
                throw new IllegalArgumentException(
                        "El producto " + producto.getNomProd()
                        + " está inactivo."
                );
            }

            if (producto.getStockProd() == null ||
                    producto.getStockProd() < cantidadSolicitada) {

                throw new IllegalArgumentException(
                        "Stock insuficiente para "
                        + producto.getNomProd()
                        + ". Cantidad solicitada: "
                        + cantidadSolicitada
                        + "."
                );
            }

            productosValidados.put(idProducto, producto);
        }

        // 4. ASIGNAR DATOS DE LA VENTA
        venta.setIdVenta(null);
        venta.setFecVenta(LocalDate.now());

        // 5. PREPARAR DETALLES
        for (DetalleVenta detalle : venta.getDetalles()) {

            Producto producto =
                    productosValidados.get(detalle.getIdProd());

            detalle.setIdDetalle(null);
            detalle.setIdVenta(null);

            detalle.setNomProd(producto.getNomProd());
            detalle.setPrecioUnitario(producto.getPrecioVenta());

            detalle.calcularSubtotal();
        }

        // 6. CALCULAR SUBTOTAL
        BigDecimal subtotal = venta.getDetalles().stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. VALIDAR Y APLICAR DESCUENTO
        BigDecimal descuento = venta.getDescuento();

        if (descuento == null) {
            descuento = BigDecimal.ZERO;
        }

        if (descuento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede ser negativo."
            );
        }

        if (descuento.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "El descuento no puede superar el subtotal."
            );
        }

        venta.setDescuento(descuento);

        // 8. CALCULAR TOTAL FINAL
        venta.setTotalVenta(subtotal.subtract(descuento));

        // 9. DESCONTAR STOCK
        for (Map.Entry<Long, Integer> entrada :
                cantidadesPorProducto.entrySet()) {

            productoService.descontarStock(
                    entrada.getKey(),
                    entrada.getValue()
            );
        }

        // 10. GUARDAR VENTA EN MYSQL
        return ventaRepository.saveAndFlush(venta);
    }
    
    // ACTUALIZAR CORREO DEL CLIENTE
    @Transactional
    public void actualizarCorreoCliente(Long id, String correo) {

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la venta."
                ));

        venta.setEmailCliente(correo);

        ventaRepository.save(venta);
    }

}
