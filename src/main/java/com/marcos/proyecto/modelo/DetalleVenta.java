
package com.marcos.proyecto.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
@Table(name = "detalle_ventas")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "id_prod")
    private Long idProd;

    @Column(name = "nom_prod")
    private String nomProd;

    @Column(name = "cant_detalle")
    private Integer cantDetalle;

    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;

    // CONSTRUCTOR VACÍO
    public DetalleVenta() {
        this.cantDetalle = 1;
        this.precioUnitario = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
    }

    // CONSTRUCTOR CON PARÁMETROS
    public DetalleVenta(
            Long idProd,
            String nomProd,
            Integer cantDetalle,
            BigDecimal precioUnitario) {

        this.idProd = idProd;
        this.nomProd = nomProd;
        this.cantDetalle = cantDetalle;
        this.precioUnitario = precioUnitario;

        calcularSubtotal();
    }

    // CALCULAR SUBTOTAL
    public void calcularSubtotal() {

        if (cantDetalle != null && precioUnitario != null) {

            subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(cantDetalle)
            );

        } else {
            subtotal = BigDecimal.ZERO;
        }
    }

    // GETTERS Y SETTERS
    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public Long getIdProd() {
        return idProd;
    }

    public void setIdProd(Long idProd) {
        this.idProd = idProd;
    }

    public String getNomProd() {
        return nomProd;
    }

    public void setNomProd(String nomProd) {
        this.nomProd = nomProd;
    }

    public Integer getCantDetalle() {
        return cantDetalle;
    }

    public void setCantDetalle(Integer cantDetalle) {
        this.cantDetalle = cantDetalle;
        calcularSubtotal();
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
