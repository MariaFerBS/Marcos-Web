package com.marcos.proyecto.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.marcos.proyecto.modelo.DetalleVenta;
import com.marcos.proyecto.modelo.Usuario;
import com.marcos.proyecto.modelo.Venta;
import com.marcos.proyecto.repository.UsuarioRepository;
import com.marcos.proyecto.service.BoletaPDFService;
import com.marcos.proyecto.service.CategoriaService;
import com.marcos.proyecto.service.CorreoService;
import com.marcos.proyecto.service.ProductoService;
import com.marcos.proyecto.service.QRService;
import com.marcos.proyecto.service.VentaService;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private QRService qrService;

    @Autowired
    private BoletaPDFService boletaPDFService;

    @Autowired
    private CorreoService correoService;

    @Value("${apiperu.token}")
    private String apiPeruToken;

    // OBTENER EL USUARIO ACTIVO DESDE MYSQL
    private Usuario obtenerUsuarioActivo(HttpSession session) {

        Object idSesion = session.getAttribute("usuarioLogueado");

        if (!(idSesion instanceof Number)) {
            return null;
        }

        Long idUsuario = ((Number) idSesion).longValue();

        return usuarioRepository.findById(idUsuario)
                .filter(usuario ->
                        Boolean.TRUE.equals(usuario.getActivo()))
                .filter(usuario ->
                        "ADMIN".equals(usuario.getRolUs())
                        || "VENDEDOR".equals(usuario.getRolUs()))
                .orElse(null);
    }

    // VALIDAR CORREO
    private boolean correoValido(String correo) {

        return correo != null
                && correo.length() <= 150
                && correo.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                );
    }

    // CONSULTAR DNI O RUC EN APIPERUDEV
    @GetMapping("/consultar-doc/{numero}")
    @ResponseBody
    public ResponseEntity<?> consultarDocumento(
            @PathVariable String numero,
            HttpSession session) {

        if (obtenerUsuarioActivo(session) == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "No autorizado"));
        }

        String doc = numero.trim();
        String url;

        if (doc.matches("\\d{8}")) {
            url = "https://apiperu.dev/api/dni/" + doc;
        } else if (doc.matches("\\d{11}")) {
            url = "https://apiperu.dev/api/ruc/" + doc;
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Documento inválido"));
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiPeruToken);
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of("success", false, "message", "Error al consultar ApiPeruDev"));
        }
    }

    // MOSTRAR FORMULARIO DE VENTA
    @GetMapping("/nueva")
    public String nuevaVenta(
            Model model,
            HttpSession session) {

        Usuario usuario = obtenerUsuarioActivo(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        String nomVendedor = usuario.getNomUs();

        long proximoId = ventaService.listar().size() + 1;

        String nroVenta = String.format("VEN-%05d", proximoId);

        model.addAttribute("nroVenta", nroVenta);
        model.addAttribute("fechaHoy", LocalDate.now());
        model.addAttribute("vendedor", nomVendedor);

        model.addAttribute("productos", productoService.listar());
        model.addAttribute("categorias", categoriaService.listar());

        return "ventas/nueva";
    }

    // REGISTRAR VENTA
    @PostMapping("/guardar")
    public String guardarVenta(

            @RequestParam(required = false)
            String nomCliente,

            @RequestParam(required = false)
            String numDocumentoCliente,

            @RequestParam(required = false)
            String emailCliente,

            @RequestParam(required = false)
            String nroVenta,

            @RequestParam(defaultValue = "0")
            BigDecimal descuento,

            @RequestParam(value = "idProd", required = false)
            List<Long> idsProd,

            @RequestParam(value = "cantDetalle", required = false)
            List<Integer> cantidades,

            HttpSession session,
            RedirectAttributes flash) {

        Usuario usuario = obtenerUsuarioActivo(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            if (idsProd == null || idsProd.isEmpty()) {
                throw new IllegalArgumentException(
                        "Debe agregar al menos un producto."
                );
            }

            if (cantidades == null ||
                    cantidades.size() != idsProd.size()) {

                throw new IllegalArgumentException(
                        "Los productos y las cantidades no coinciden."
                );
            }

            if (descuento == null ||
                    descuento.compareTo(BigDecimal.ZERO) < 0) {

                throw new IllegalArgumentException(
                        "El descuento no puede ser negativo."
                );
            }

            // VALIDAR CORREO
            if (emailCliente != null && !emailCliente.isBlank()) {

                emailCliente = emailCliente.trim();

                if (!correoValido(emailCliente)) {
                    throw new IllegalArgumentException(
                            "Ingrese un correo electrónico válido."
                    );
                }

            } else {
                emailCliente = null;
            }

            Venta venta = new Venta();

            venta.setIdUs(usuario.getIdUs());

            venta.setNomCliente(
                    nomCliente != null && !nomCliente.isBlank()
                            ? nomCliente.trim()
                            : "Público General"
            );

            venta.setNumDocumentoCliente(
                    numDocumentoCliente != null
                            && !numDocumentoCliente.isBlank()
                            ? numDocumentoCliente.trim()
                            : "Sin Documento"
            );

            venta.setEmailCliente(emailCliente);
            venta.setDescuento(descuento);
            venta.setMetodoPago("Efectivo");

            // AGREGAR PRODUCTOS
            for (int i = 0; i < idsProd.size(); i++) {

                Long idProducto = idsProd.get(i);
                Integer cantidad = cantidades.get(i);

                if (idProducto == null ||
                        cantidad == null ||
                        cantidad <= 0) {

                    throw new IllegalArgumentException(
                            "Seleccione un producto y una cantidad válida."
                    );
                }

                DetalleVenta detalle = new DetalleVenta();

                detalle.setIdProd(idProducto);
                detalle.setCantDetalle(cantidad);

                venta.getDetalles().add(detalle);
            }

            Venta guardada = ventaService.procesarVenta(venta);

            flash.addFlashAttribute(
                    "msg",
                    "Venta registrada con éxito."
            );

            return "redirect:/ventas/boleta/"
                    + guardada.getIdVenta();

        } catch (IllegalArgumentException e) {

            flash.addFlashAttribute("error", e.getMessage());

            return "redirect:/ventas/nueva";
        }
    }

    // MOSTRAR BOLETA Y SU CODIGO QR
    @GetMapping("/boleta/{id}")
    public String verBoleta(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        Usuario usuario = obtenerUsuarioActivo(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        Venta venta = ventaService.obtener(id);

        if (venta == null) {
            return "redirect:/ventas/nueva";
        }

        model.addAttribute("venta", venta);

        // GENERAR QR EN BASE64
        model.addAttribute(
                "qrBase64",
                qrService.generarQRBase64(venta)
        );

        return "ventas/boleta";
    }

    // DESCARGAR BOLETA PDF
    @GetMapping("/boleta/{id}/pdf")
    public ResponseEntity<byte[]> descargarPDF(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario = obtenerUsuarioActivo(session);

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        Venta venta = ventaService.obtener(id);

        if (venta == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdf = boletaPDFService.generarPDF(venta);

        String nombreArchivo = String.format(
                "Comprobante-B001-%06d.pdf",
                venta.getIdVenta()
        );

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentDispositionFormData(
                "attachment",
                nombreArchivo
        );

        headers.setContentLength(pdf.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    // ENVIAR BOLETA POR GMAIL
    @PostMapping("/boleta/{id}/enviar")
    public String enviarBoleta(
            @PathVariable Long id,

            @RequestParam String emailCliente,

            HttpSession session,
            RedirectAttributes flash) {

        Usuario usuario = obtenerUsuarioActivo(session);

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            Venta venta = ventaService.obtener(id);

            if (venta == null) {
                throw new IllegalArgumentException(
                        "No se encontró la venta."
                );
            }

            emailCliente = emailCliente.trim();

            if (!correoValido(emailCliente)) {
                throw new IllegalArgumentException(
                        "Ingrese un correo electrónico válido."
                );
            }

            String numeroBoleta = String.format(
                    "B001-%06d",
                    venta.getIdVenta()
            );

            // GENERAR PDF
            byte[] pdf = boletaPDFService.generarPDF(venta);

            // ENVIAR PDF POR CORREO
            correoService.enviarBoleta(
                    emailCliente,
                    venta.getNomCliente(),
                    numeroBoleta,
                    pdf
            );

            // ACTUALIZAR CORREO EN MYSQL
            ventaService.actualizarCorreoCliente(
                    id,
                    emailCliente
            );

            flash.addFlashAttribute(
                    "msg",
                    "Comprobante enviado correctamente a " + emailCliente
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            flash.addFlashAttribute(
                    "error",
                    e.getMessage()
            );
        }

        return "redirect:/ventas/boleta/" + id;
    }
}