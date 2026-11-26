
package com.marcos.proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.marcos.proyecto.modelo.Producto;
import com.marcos.proyecto.dto.ProductoDTO;

public interface ProductoRepository
        extends JpaRepository<Producto, Long> {

    @Query("""
        SELECT new com.marcos.proyecto.dto.ProductoDTO(
            p.idProd,
            p.codProd,
            p.nomProd,
            c.nomCat,
            pr.nomProv,
            p.precioCompra,
            p.precioVenta,
            p.stockProd,
            p.stockMinimo,
            p.estado
        )
        FROM Producto p, Categoria c, Proveedor pr
        WHERE p.idCat = c.idCat
          AND p.idProv = pr.idProv
        """)
    List<ProductoDTO> listarProductosDTO();
}
