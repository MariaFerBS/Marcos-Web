
package com.marcos.proyecto.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.marcos.proyecto.dto.TipoCambioDTO;

@Service
public class TipoCambioService {

    @Value("${exchange.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public TipoCambioService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://v6.exchangerate-api.com/v6")
                .build();
    }

    public TipoCambioDTO consultarTipoCambio() {

        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException(
                    "No se ha configurado la API Key");
        }

        Map<?, ?> respuesta = restClient.get()
                .uri("/{key}/latest/USD", apiKey.trim())
                .retrieve()
                .body(Map.class);

        if (respuesta == null) {
            throw new RuntimeException(
                    "La API no devolvió información");
        }

        String resultado = String.valueOf(
                respuesta.get("result"));

        if (!"success".equals(resultado)) {
            throw new RuntimeException(
                    "Error del proveedor: "
                    + respuesta.get("error-type"));
        }

        Map<?, ?> tasas = (Map<?, ?>)
                respuesta.get("conversion_rates");

        if (tasas == null || !(tasas.get("PEN") instanceof Number)) {
            throw new RuntimeException(
                    "No se encontró la tasa USD a PEN");
        }

        double tasa = ((Number) tasas.get("PEN")).doubleValue();

        return new TipoCambioDTO(
                String.valueOf(respuesta.get("base_code")),
                "PEN",
                tasa,
                String.valueOf(respuesta.get("time_last_update_utc")),
                String.valueOf(respuesta.get("time_next_update_utc")),
                "ExchangeRate-API"
        );
    }
}
