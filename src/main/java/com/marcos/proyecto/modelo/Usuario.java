
package com.marcos.proyecto.modelo;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUs;

    @Column(name = "nombre", nullable = false)
    private String nomUs;

    @Column(name = "username", nullable = false, unique = true)
    private String emailUs;

    @Column(name = "password_hash", nullable = false)
    private String passwordUs;

    @Column(name = "rol", nullable = false)
    private String rolUs;

    @Column(name = "activo")
    private Boolean activo = true;
    @Column(name = "descuento", precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    public Usuario() {}

    public Long getIdUs() { return idUs; }
    public void setIdUs(Long idUs) { this.idUs = idUs; }

    public String getNomUs() { return nomUs; }
    public void setNomUs(String nomUs) { this.nomUs = nomUs; }

    public String getEmailUs() { return emailUs; }
    public void setEmailUs(String emailUs) { this.emailUs = emailUs; }

    public String getPasswordUs() { return passwordUs; }
    public void setPasswordUs(String passwordUs) { this.passwordUs = passwordUs; }

    public String getRolUs() { return rolUs; }
    public void setRolUs(String rolUs) { this.rolUs = rolUs; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public BigDecimal getDescuento() {
    return descuento;
}

    public void setDescuento(BigDecimal descuento) {
    this.descuento = descuento;
}
}
