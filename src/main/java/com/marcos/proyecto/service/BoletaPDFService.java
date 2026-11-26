
package com.marcos.proyecto.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import com.marcos.proyecto.modelo.DetalleVenta;
import com.marcos.proyecto.modelo.Venta;

@Service
public class BoletaPDFService {

    private final QRService qrService;

    public BoletaPDFService(QRService qrService) {
        this.qrService = qrService;
    }

    // FORMATO DE DINERO
    private String formatoDinero(BigDecimal cantidad) {

        if (cantidad == null) {
            cantidad = BigDecimal.ZERO;
        }

        NumberFormat formato =
                NumberFormat.getNumberInstance(Locale.US);

        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);

        return "S/ " + formato.format(cantidad);
    }

    // EVITAR CARACTERES NO COMPATIBLES CON HELVETICA
    private String textoCompatible(String texto) {

        if (texto == null) {
            return "";
        }

        Charset charset = Charset.forName("windows-1252");

        return new String(
                texto.getBytes(charset),
                charset
        );
    }

    // ESCRIBIR TEXTO EN EL PDF
    private void escribirTexto(
            PDPageContentStream contenido,
            String texto,
            float x,
            float y,
            float tamanio,
            PDType1Font fuente) throws IOException {

        contenido.beginText();
        contenido.setFont(fuente, tamanio);
        contenido.newLineAtOffset(x, y);
        contenido.showText(textoCompatible(texto));
        contenido.endText();
    }

    // DIBUJAR UNA LÍNEA
    private void dibujarLinea(
            PDPageContentStream contenido,
            float x1,
            float y1,
            float x2,
            float y2) throws IOException {

        contenido.moveTo(x1, y1);
        contenido.lineTo(x2, y2);
        contenido.stroke();
    }

    // GENERAR COMPROBANTE PDF
    public byte[] generarPDF(Venta venta) {

        if (venta == null || venta.getIdVenta() == null) {
            throw new IllegalArgumentException(
                    "No se encontró una venta válida."
            );
        }

        if (venta.getDetalles() == null ||
                venta.getDetalles().isEmpty()) {

            throw new IllegalArgumentException(
                    "La venta no contiene productos."
            );
        }

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);

            PDType1Font negrita = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

            PDType1Font normal = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

            try (PDPageContentStream contenido =
                    new PDPageContentStream(documento, pagina)) {

                // ENCABEZADO
                escribirTexto(
                        contenido,
                        "TIENDA DE ABARROTES",
                        190, 790, 17, negrita
                );

                escribirTexto(
                        contenido,
                        "RUC: 20456789123",
                        240, 770, 10, normal
                );

                escribirTexto(
                        contenido,
                        "Av. Universitaria 1234 - Lima",
                        210, 755, 10, normal
                );

                escribirTexto(
                        contenido,
                        "COMPROBANTE DE VENTA",
                        205, 725, 14, negrita
                );

                String numero = String.format(
                        "B001-%06d",
                        venta.getIdVenta()
                );

                escribirTexto(
                        contenido,
                        numero,
                        250, 705, 12, negrita
                );

                dibujarLinea(contenido, 45, 690, 550, 690);

                // DATOS DEL CLIENTE
                escribirTexto(
                        contenido,
                        "Fecha: " + venta.getFecVenta(),
                        50, 665, 10, normal
                );

                escribirTexto(
                        contenido,
                        "Cliente: " + venta.getNomCliente(),
                        50, 645, 10, normal
                );

                escribirTexto(
                        contenido,
                        "Documento: " + venta.getNumDocumentoCliente(),
                        50, 625, 10, normal
                );

                escribirTexto(
                        contenido,
                        "Correo: " + (
                                venta.getEmailCliente() != null
                                        ? venta.getEmailCliente()
                                        : "No registrado"
                        ),
                        50, 605, 10, normal
                );

                escribirTexto(
                        contenido,
                        "Metodo de pago: " + venta.getMetodoPago(),
                        50, 585, 10, normal
                );

                // TABLA DE PRODUCTOS
                dibujarLinea(contenido, 45, 565, 550, 565);

                escribirTexto(
                        contenido,
                        "CANT.",
                        50, 548, 9, negrita
                );

                escribirTexto(
                        contenido,
                        "DESCRIPCION",
                        110, 548, 9, negrita
                );

                escribirTexto(
                        contenido,
                        "P. UNIT.",
                        370, 548, 9, negrita
                );

                escribirTexto(
                        contenido,
                        "IMPORTE",
                        465, 548, 9, negrita
                );

                dibujarLinea(contenido, 45, 540, 550, 540);

                float posicionY = 522;

                for (DetalleVenta detalle : venta.getDetalles()) {

                    if (posicionY < 190) {
                        throw new IllegalArgumentException(
                                "La venta contiene demasiados productos "
                                + "para este formato de PDF."
                        );
                    }

                    escribirTexto(
                            contenido,
                            String.valueOf(detalle.getCantDetalle()),
                            55, posicionY, 9, normal
                    );

                    String nombreProducto = detalle.getNomProd();

                    if (nombreProducto != null &&
                            nombreProducto.length() > 38) {

                        nombreProducto =
                                nombreProducto.substring(0, 35) + "...";
                    }

                    escribirTexto(
                            contenido,
                            nombreProducto,
                            110, posicionY, 9, normal
                    );

                    escribirTexto(
                            contenido,
                            formatoDinero(detalle.getPrecioUnitario()),
                            365, posicionY, 9, normal
                    );

                    escribirTexto(
                            contenido,
                            formatoDinero(detalle.getSubtotal()),
                            465, posicionY, 9, normal
                    );

                    posicionY -= 20;
                }

                // TOTALES
                dibujarLinea(
                        contenido,
                        330,
                        posicionY - 5,
                        550,
                        posicionY - 5
                );

                BigDecimal descuento = venta.getDescuento() != null
                        ? venta.getDescuento()
                        : BigDecimal.ZERO;

                escribirTexto(
                        contenido,
                        "Descuento: " + formatoDinero(descuento),
                        360, posicionY - 25, 10, normal
                );

                escribirTexto(
                        contenido,
                        "TOTAL: " + formatoDinero(venta.getTotalVenta()),
                        350, posicionY - 50, 13, negrita
                );

                // CODIGO QR
                byte[] imagenQR = qrService.generarQRBytes(venta);

                PDImageXObject imagen = PDImageXObject.createFromByteArray(
                        documento,
                        imagenQR,
                        "codigo-qr"
                );

                contenido.drawImage(
                        imagen,
                        55,
                        85,
                        105,
                        105
                );

                escribirTexto(
                        contenido,
                        "QR informativo de la venta",
                        45, 70, 8, normal
                );

                escribirTexto(
                        contenido,
                        "Gracias por su compra.",
                        350, 110, 11, negrita
                );

                escribirTexto(
                        contenido,
                        "Comprobante interno de venta.",
                        350, 92, 8, normal
                );
            }

            documento.save(salida);

            return salida.toByteArray();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No se pudo generar el comprobante PDF.",
                    e
            );
        }
    }
}
