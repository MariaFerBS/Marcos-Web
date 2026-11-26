
package com.marcos.proyecto.modelo;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prod")
    private Long idProd;

    @Column(name = "id_cat")
    private Long idCat;

    @Column(name = "id_prov")
    private Long idProv;

    @Column(name = "cod_prod", length = 50)
    private String codProd;

    @Column(name = "nom_prod", nullable = false, length = 150)
    private String nomProd;

    @Column(name = "desc_prod", length = 255)
    private String descProd;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_prod")
    private Integer stockProd;

    @Column(name = "stock_minimo")
    private Integer stockMinimo;

    @Column(name = "unidad_medida", length = 50)
    private String unidadMedida;

    @Column(name = "estado", length = 30)
    private String estado;

    @Column(name = "imagen", length = 255)
    private String imagen;

    // Campos auxiliares para mostrar en las vistas
    @Transient
    private String nomCat;

    @Transient
    private String nomProv;

    // Constructor vacio
    public Producto() {
    }

    // Constructor completo original
    public Producto(Long idProd, Long idCat, Long idProv,
                    String codProd, String nomProd,
                    String descProd, BigDecimal precioCompra,
                    BigDecimal precioVenta, Integer stockProd,
                    Integer stockMinimo, String unidadMedida,
                    String estado, String imagen,
                    String nomCat, String nomProv) {

        this.idProd = idProd;
        this.idCat = idCat;
        this.idProv = idProv;
        this.codProd = codProd;
        this.nomProd = nomProd;
        this.descProd = descProd;
        this.precioCompra = precioCompra;
        this.precioVenta = precioVenta;
        this.stockProd = stockProd;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida;
        this.estado = estado;
        this.imagen = imagen;
        this.nomCat = nomCat;
        this.nomProv = nomProv;
    }

    public Long getIdProd() {
        return idProd;
    }

    public void setIdProd(Long idProd) {
        this.idProd = idProd;
    }

    public Long getIdCat() {
        return idCat;
    }

    public void setIdCat(Long idCat) {
        this.idCat = idCat;
    }

    public Long getIdProv() {
        return idProv;
    }

    public void setIdProv(Long idProv) {
        this.idProv = idProv;
    }

    public String getCodProd() {
        return codProd;
    }

    public void setCodProd(String codProd) {
        this.codProd = codProd;
    }

    public String getNomProd() {
        return nomProd;
    }

    public void setNomProd(String nomProd) {
        this.nomProd = nomProd;
    }

    public String getDescProd() {
        return descProd;
    }

    public void setDescProd(String descProd) {
        this.descProd = descProd;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public Integer getStockProd() {
        return stockProd;
    }

    public void setStockProd(Integer stockProd) {
        this.stockProd = stockProd;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getNomCat() {
        return nomCat;
    }

    public void setNomCat(String nomCat) {
        this.nomCat = nomCat;
    }

    public String getNomProv() {
        return nomProv;
    }

    public void setNomProv(String nomProv) {
        this.nomProv = nomProv;
    }

    // Identifica productos con stock bajo
    public boolean isStockBajo() {
        return stockProd != null
                && stockMinimo != null
                && stockProd <= stockMinimo;
    }
}
