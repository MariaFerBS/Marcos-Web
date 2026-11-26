
package com.marcos.proyecto.dto;

public record TipoCambioDTO(
    String monedaBase,
    String monedaDestino,
    double tipoCambio,
    String ultimaActualizacion,
    String proximaActualizacion,
    String proveedor
) {
}
