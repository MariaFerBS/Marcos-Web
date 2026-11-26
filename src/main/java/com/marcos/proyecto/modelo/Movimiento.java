
package com.marcos.proyecto.modelo;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mov")
    private Long idMov;

    @Column(name = "id_prod")
    private Long idProd;

    @Column(name = "id_us")
    private Long idUs;

    @Column(name = "tipo_mov")
    private String tipoMov;

    @Column(name = "cant_mov")
    private Integer cantMov;

    @Column(name = "fec_mov")
    private LocalDate fecMov;

    @Column(name = "motivo_mov")
    private String motivoMov;

    @Column(name = "nom_prod")
    private String nomProd;

    @Column(name = "nom_us")
    private String nomUs;

    // CONSTRUCTOR VACÍO
    public Movimiento() {
    }

    // CONSTRUCTOR ORIGINAL
    public Movimiento(
            Long idMov,
            Long idProd,
            Long idUs,
            String tipoMov,
            Integer cantMov,
            LocalDate fecMov,
            String motivoMov,
            String nomProd) {

        this.idMov = idMov;
        this.idProd = idProd;
        this.idUs = idUs;
        this.tipoMov = tipoMov;
        this.cantMov = cantMov;
        this.fecMov = fecMov;
        this.motivoMov = motivoMov;
        this.nomProd = nomProd;
    }

    // CONSTRUCTOR CON NOMBRE DE USUARIO
    public Movimiento(
            Long idMov,
            Long idProd,
            Long idUs,
            String tipoMov,
            Integer cantMov,
            LocalDate fecMov,
            String motivoMov,
            String nomProd,
            String nomUs) {

        this.idMov = idMov;
        this.idProd = idProd;
        this.idUs = idUs;
        this.tipoMov = tipoMov;
        this.cantMov = cantMov;
        this.fecMov = fecMov;
        this.motivoMov = motivoMov;
        this.nomProd = nomProd;
        this.nomUs = nomUs;
    }

    // GETTERS Y SETTERS
    public Long getIdMov() {
        return idMov;
    }

    public void setIdMov(Long idMov) {
        this.idMov = idMov;
    }

    public Long getIdProd() {
        return idProd;
    }

    public void setIdProd(Long idProd) {
        this.idProd = idProd;
    }

    public Long getIdUs() {
        return idUs;
    }

    public void setIdUs(Long idUs) {
        this.idUs = idUs;
    }

    public String getTipoMov() {
        return tipoMov;
    }

    public void setTipoMov(String tipoMov) {
        this.tipoMov = tipoMov;
    }

    public Integer getCantMov() {
        return cantMov;
    }

    public void setCantMov(Integer cantMov) {
        this.cantMov = cantMov;
    }

    public LocalDate getFecMov() {
        return fecMov;
    }

    public void setFecMov(LocalDate fecMov) {
        this.fecMov = fecMov;
    }

    public String getMotivoMov() {
        return motivoMov;
    }

    public void setMotivoMov(String motivoMov) {
        this.motivoMov = motivoMov;
    }

    public String getNomProd() {
        return nomProd;
    }

    public void setNomProd(String nomProd) {
        this.nomProd = nomProd;
    }

    public String getNomUs() {
        return nomUs;
    }

    public void setNomUs(String nomUs) {
        this.nomUs = nomUs;
    }
}
