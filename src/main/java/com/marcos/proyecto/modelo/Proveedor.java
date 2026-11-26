
package com.marcos.proyecto.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prov")
    private Long idProv;

    @Column(name = "nom_prov", nullable = false, length = 150)
    private String nomProv;

    @Column(name = "ruc_prov", length = 11)
    private String rucProv;

    @Column(name = "tel_prov", length = 20)
    private String telProv;

    // Constructor vacio
    public Proveedor() {
    }

    // Constructor original
    public Proveedor(Long idProv, String nomProv,
                     String rucProv, String telProv) {
        this.idProv = idProv;
        this.nomProv = nomProv;
        this.rucProv = rucProv;
        this.telProv = telProv;
    }

    public Long getIdProv() {
        return idProv;
    }

    public void setIdProv(Long idProv) {
        this.idProv = idProv;
    }

    public String getNomProv() {
        return nomProv;
    }

    public void setNomProv(String nomProv) {
        this.nomProv = nomProv;
    }

    public String getRucProv() {
        return rucProv;
    }

    public void setRucProv(String rucProv) {
        this.rucProv = rucProv;
    }

    public String getTelProv() {
        return telProv;
    }

    public void setTelProv(String telProv) {
        this.telProv = telProv;
    }
}
