
package com.marcos.proyecto.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.marcos.proyecto.modelo.Categoria;
import com.marcos.proyecto.repository.CategoriaRepository;

import jakarta.annotation.PostConstruct;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // CONSTRUCTOR
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // CATEGORIAS INICIALES
    @PostConstruct
    public void inicializarCategorias() {

        if (categoriaRepository.count() == 0) {

            categoriaRepository.save(
                new Categoria(null, "Abarrotes",
                    "Arroz, azúcar, fideos, aceites")
            );

            categoriaRepository.save(
                new Categoria(null, "Bebidas",
                    "Gaseosas, jugos, aguas minerales")
            );

            categoriaRepository.save(
                new Categoria(null, "Lácteos",
                    "Leche, quesos, yogures")
            );

            categoriaRepository.save(
                new Categoria(null, "Limpieza",
                    "Detergentes, jabones, desinfectantes")
            );
        }
    }

    // LISTAR
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    // BUSCAR POR NOMBRE O DESCRIPCION
    public List<Categoria> buscar(String q) {

        if (q == null || q.isBlank()) {
            return listar();
        }

        String filtro = q.toLowerCase();

        return categoriaRepository.findAll()
                .stream()
                .filter(c ->
                    (c.getNomCat() != null &&
                     c.getNomCat().toLowerCase().contains(filtro))
                    ||
                    (c.getDescCat() != null &&
                     c.getDescCat().toLowerCase().contains(filtro))
                )
                .toList();
    }

    // OBTENER POR ID
    public Categoria obtener(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    // GUARDAR O ACTUALIZAR
    public Categoria guardar(Categoria cat) {
        return categoriaRepository.save(cat);
    }

    // ELIMINAR
    public void eliminar(Long id) {

        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
        }
    }
}
