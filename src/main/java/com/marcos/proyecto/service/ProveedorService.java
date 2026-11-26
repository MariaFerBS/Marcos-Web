
package com.marcos.proyecto.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.marcos.proyecto.modelo.Proveedor;
import com.marcos.proyecto.repository.ProveedorRepository;

import jakarta.annotation.PostConstruct;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    // CONSTRUCTOR
    public ProveedorService(
            ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    // PROVEEDORES INICIALES
    @PostConstruct
    public void inicializarProveedores() {

        if (proveedorRepository.count() == 0) {

            proveedorRepository.save(new Proveedor(
                    null,
                    "Distribuidora Lima SAC",
                    "20123456789",
                    "987654321"
            ));

            proveedorRepository.save(new Proveedor(
                    null,
                    "Comercial Andina EIRL",
                    "20987654321",
                    "912345678"
            ));

            proveedorRepository.save(new Proveedor(
                    null,
                    "Abarrotes del Centro SRL",
                    "20556677889",
                    "955443322"
            ));
        }
    }

    // LISTAR
    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    // BUSCAR POR NOMBRE O RUC
    public List<Proveedor> buscar(String q) {

        if (q == null || q.isBlank()) {
            return listar();
        }

        String filtro = q.toLowerCase();

        return proveedorRepository.findAll()
                .stream()
                .filter(p ->
                    (p.getNomProv() != null &&
                     p.getNomProv().toLowerCase().contains(filtro))
                    ||
                    (p.getRucProv() != null &&
                     p.getRucProv().contains(filtro))
                )
                .toList();
    }

    // OBTENER POR ID
    public Proveedor obtener(Long id) {
        return proveedorRepository.findById(id)
                .orElse(null);
    }

    // GUARDAR O ACTUALIZAR
    public Proveedor guardar(Proveedor prov) {
        return proveedorRepository.save(prov);
    }

    // ELIMINAR
    public void eliminar(Long id) {

        if (proveedorRepository.existsById(id)) {
            proveedorRepository.deleteById(id);
        }
    }
}
