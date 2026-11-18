package puce.verterinaria.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import puce.verterinaria.model.Medicamento;
import puce.verterinaria.repository.MedicamentoRepository;
import java.util.List;
import java.util.Optional;

@Service
public class MedicamentoService {
    private final MedicamentoRepository medicamentoRepository;

    public MedicamentoService(MedicamentoRepository medicamentoRepository) {
        this.medicamentoRepository = medicamentoRepository;
    }

    public List<Medicamento> traerTodos() {
        return medicamentoRepository.findAll();
    }

    public Optional<Medicamento> traerUno(int id) {
        return medicamentoRepository.findById(id);
    }

    public Medicamento guardar(Medicamento medicamento) {
        return medicamentoRepository.save(medicamento);
    }

    public Optional<Medicamento> editar(int id, Medicamento nuevaData) {
        return medicamentoRepository.findById(id).map(existente -> {
            existente.setNombre(nuevaData.getNombre());
            existente.setPrecio(nuevaData.getPrecio());
            existente.setStock(nuevaData.getStock());
            existente.setRequiereReceta(nuevaData.isRequiereReceta());
            return medicamentoRepository.save(existente);
        });
    }

    public boolean eliminar(int id) {
        if (medicamentoRepository.existsById(id)) {
            medicamentoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Situaciones especiales: las reglas se resuelven en el servicio.
    public List<Medicamento> buscarStockBajo(int minimo) {
        return medicamentoRepository.findByStockLessThan(minimo);
    }

    public List<Medicamento> obtenerVentaLibre() {
        return medicamentoRepository.findByRequiereRecetaFalse();
    }

    @Transactional
    public Optional<Medicamento> despachar(int id, int cantidad) {
        if (cantidad <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que cero");
        }
        return medicamentoRepository.findById(id).map(medicamento -> {
            if (medicamento.getStock() < cantidad) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente");
            }
            medicamento.setStock(medicamento.getStock() - cantidad);
            return medicamentoRepository.save(medicamento);
        });
    }
}
