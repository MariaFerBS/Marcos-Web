
package com.marcos.proyecto.dto;

import java.math.BigDecimal;

public record ProductoDTO(
    Long idProd,
    String codProd,
    String nomProd,
    String nomCat,
    String nomProv,
    BigDecimal precioCompra,
    BigDecimal precioVenta,
    Integer stockProd,
    Integer stockMinimo,
    String estado
) {
}
