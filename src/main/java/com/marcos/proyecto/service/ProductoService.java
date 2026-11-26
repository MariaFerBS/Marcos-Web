
package com.marcos.proyecto.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.modelo.Categoria;
import com.marcos.proyecto.modelo.Proveedor;

import com.marcos.proyecto.repository.ProductoRepository;
import com.marcos.proyecto.repository.CategoriaRepository;
import com.marcos.proyecto.repository.ProveedorRepository;

import jakarta.annotation.PostConstruct;
import com.marcos.proyecto.dto.ProductoDTO;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;

    // CONSTRUCTOR
    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            ProveedorRepository proveedorRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.proveedorRepository = proveedorRepository;
    }

    // PRODUCTOS INICIALES
    @PostConstruct
    public void inicializarProductos() {

        if (productoRepository.count() == 0
                && categoriaRepository.existsById(1L)
                && categoriaRepository.existsById(2L)
                && proveedorRepository.existsById(1L)
                && proveedorRepository.existsById(2L)) {

            productoRepository.save(new Producto(
                    null, 1L, 1L,
                    "P001", "Arroz 1kg", "Arroz extra",
                    new BigDecimal("4.50"),
                    new BigDecimal("6.00"),
                    50, 10, "Unidad", "Activo",
                    "arroz.jpg", null, null
            ));

            productoRepository.save(new Producto(
                    null, 1L, 1L,
                    "P002", "Aceite 1L", "Aceite vegetal",
                    new BigDecimal("7.00"),
                    new BigDecimal("9.50"),
                    8, 10, "Unidad", "Activo",
                    "aceite.jpg", null, null
            ));

            productoRepository.save(new Producto(
                    null, 2L, 2L,
                    "P003", "Gaseosa 1.5L",
                    "Gaseosa Inca Kola",
                    new BigDecimal("5.00"),
                    new BigDecimal("7.00"),
                    25, 8, "Unidad", "Activo",
                    "gaseosa.jpg", null, null
            ));
        }
    }

    // COMPLETAR NOMBRES PARA LAS VISTAS
    private Producto completarDatos(Producto p) {

        if (p.getIdCat() != null) {
            p.setNomCat(
                categoriaRepository.findById(p.getIdCat())
                    .map(Categoria::getNomCat)
                    .orElse("")
            );
        }

        if (p.getIdProv() != null) {
            p.setNomProv(
                proveedorRepository.findById(p.getIdProv())
                    .map(Proveedor::getNomProv)
                    .orElse("")
            );
        }

        return p;
        
    }

    // LISTAR
    public List<Producto> listar() {
        return productoRepository.findAll()
                .stream()
                .map(this::completarDatos)
                .toList();
    }

    // BUSCAR
    public List<Producto> buscar(
            String q, Long idCat, Long idProv, String estado) {

        String filtro = q == null ? "" : q.toLowerCase();

        return listar().stream()
                .filter(p -> {

                    if (!filtro.isBlank()) {

                        boolean coincide =
                            (p.getNomProd() != null &&
                             p.getNomProd().toLowerCase().contains(filtro))
                            ||
                            (p.getNomCat() != null &&
                             p.getNomCat().toLowerCase().contains(filtro))
                            ||
                            (p.getNomProv() != null &&
                             p.getNomProv().toLowerCase().contains(filtro));

                        if (!coincide) return false;
                    }

                    if (idCat != null &&
                        !Objects.equals(p.getIdCat(), idCat))
                        return false;

                    if (idProv != null &&
                        !Objects.equals(p.getIdProv(), idProv))
                        return false;

                    if (estado != null && !estado.isBlank()
                        && !estado.equalsIgnoreCase(p.getEstado()))
                        return false;

                    return true;
                })
                .toList();
    }

    // CONSULTAR
    public Producto obtener(Long id) {

        Producto p = productoRepository.findById(id)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Producto no encontrado: " + id
                    )
                );

        return completarDatos(p);
    }

    // GUARDAR O ACTUALIZAR
    @Transactional
    public Producto guardar(Producto p) {
        return productoRepository.save(p);
    }

    // ELIMINAR
    @Transactional
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    // STOCK BAJO
    public List<Producto> obtenerStockBajo() {
        return listar().stream()
                .filter(Producto::isStockBajo)
                .toList();
    }

    // TOTAL DE PRODUCTOS
    public int totalProductos() {
        return Math.toIntExact(productoRepository.count());
    }

    // DESCONTAR STOCK
    @Transactional
    public synchronized void descontarStock(
            Long idProd, int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        Producto p = obtener(idProd);

        if (p.getStockProd() == null ||
            p.getStockProd() < cantidad) {

            throw new IllegalArgumentException(
                "Stock insuficiente para: " + p.getNomProd()
                + " (Stock disponible: "
                + (p.getStockProd() == null ? 0 : p.getStockProd())
                + ")"
            );
        }

        p.setStockProd(p.getStockProd() - cantidad);
        productoRepository.save(p);
    }

    // AUMENTAR STOCK
    @Transactional
    public synchronized void aumentarStock(
            Long idProd, int cantidad) {

        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "La cantidad debe ser mayor que cero"
            );
        }

        Producto p = obtener(idProd);

        if (p.getStockProd() == null) {
            p.setStockProd(0);
        }

        p.setStockProd(p.getStockProd() + cantidad);
        productoRepository.save(p);
    }
    
    // CONSULTA DTO DE PRODUCTOS, CATEGORIAS Y PROVEEDORES
    public List<ProductoDTO> listarProductosDTO() {
        return productoRepository.listarProductosDTO();
    }

}
