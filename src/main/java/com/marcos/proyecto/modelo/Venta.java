
package com.marcos.proyecto.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Long idVenta;

    @Column(name = "id_us")
    private Long idUs;

    @Column(name = "nom_cliente")
    private String nomCliente;

    @Column(name = "num_documento_cliente")
    private String numDocumentoCliente;

    @Column(name = "email_cliente", length = 150)
    private String emailCliente;

    @Column(name = "fec_venta")
    private LocalDate fecVenta;

    @Column(name = "total_venta", precision = 12, scale = 2)
    private BigDecimal totalVenta;

    @Column(name = "descuento", precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "metodo_pago")
    private String metodoPago;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
        name = "id_venta",
        referencedColumnName = "id_venta"
    )
    private List<DetalleVenta> detalles = new ArrayList<>();

    public Venta() {
        this.fecVenta = LocalDate.now();
        this.totalVenta = BigDecimal.ZERO;
        this.descuento = BigDecimal.ZERO;
    }

    public void calcularTotal() {
        BigDecimal subtotal = detalles.stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuentoAplicado = descuento != null
                ? descuento
                : BigDecimal.ZERO;

        this.totalVenta = subtotal.subtract(descuentoAplicado);
    }

    public Long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Long idVenta) {
        this.idVenta = idVenta;
    }

    public Long getIdUs() {
        return idUs;
    }

    public void setIdUs(Long idUs) {
        this.idUs = idUs;
    }

    public String getNomCliente() {
        return nomCliente;
    }

    public void setNomCliente(String nomCliente) {
        this.nomCliente = nomCliente;
    }

    public String getNumDocumentoCliente() {
        return numDocumentoCliente;
    }

    public void setNumDocumentoCliente(String numDocumentoCliente) {
        this.numDocumentoCliente = numDocumentoCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public void setEmailCliente(String emailCliente) {
        this.emailCliente = emailCliente;
    }

    public LocalDate getFecVenta() {
        return fecVenta;
    }

    public void setFecVenta(LocalDate fecVenta) {
        this.fecVenta = fecVenta;
    }

    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }
}
