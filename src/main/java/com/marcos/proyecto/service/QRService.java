
package com.marcos.proyecto.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.marcos.proyecto.modelo.Venta;

@Service
public class QRService {

    // GENERAR CONTENIDO INFORMATIVO DE LA VENTA
    private String generarContenido(Venta venta) {

        if (venta == null || venta.getIdVenta() == null) {
            throw new IllegalArgumentException(
                    "No se puede generar el QR de una venta inválida."
            );
        }

        BigDecimal total = venta.getTotalVenta() != null
                ? venta.getTotalVenta()
                : BigDecimal.ZERO;

        return "COMPROBANTE DE VENTA"
                + "\nNumero: B001-" + String.format("%06d", venta.getIdVenta())
                + "\nFecha: " + venta.getFecVenta()
                + "\nTotal: S/ " + total.toPlainString()
                + "\nEstado: Venta registrada";
    }

    // GENERAR QR COMO ARREGLO DE BYTES
    public byte[] generarQRBytes(Venta venta) {

        try {

            String contenido = generarContenido(venta);

            QRCodeWriter qrWriter = new QRCodeWriter();

            BitMatrix matriz = qrWriter.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    250,
                    250
            );

            ByteArrayOutputStream salida =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    matriz,
                    "PNG",
                    salida
            );

            return salida.toByteArray();

        } catch (WriterException | IOException e) {

            throw new IllegalStateException(
                    "No se pudo generar el código QR.",
                    e
            );
        }
    }

    // GENERAR QR EN BASE64 PARA MOSTRARLO EN THYMELEAF
    public String generarQRBase64(Venta venta) {

        byte[] imagenQR = generarQRBytes(venta);

        return Base64.getEncoder().encodeToString(imagenQR);
    }
}
