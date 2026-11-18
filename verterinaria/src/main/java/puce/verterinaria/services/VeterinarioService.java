package puce.verterinaria.services;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import puce.verterinaria.model.Veterinario;
import puce.verterinaria.repository.VeterinarioRepository;
import java.util.List;
import java.util.Optional;

@Service
public class VeterinarioService {
    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioService(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Veterinario> traerTodos() {
        return veterinarioRepository.findAll();
    }

    public Optional<Veterinario> traerUno(int id) {
        return veterinarioRepository.findById(id);
    }

    public Veterinario guardar(Veterinario veterinario) {
        return veterinarioRepository.save(veterinario);
    }

    public Optional<Veterinario> editar(int id, Veterinario nuevaData) {
        return veterinarioRepository.findById(id).map(existente -> {
            existente.setNombre(nuevaData.getNombre());
            existente.setEspecialidad(nuevaData.getEspecialidad());
            existente.setAniosExperiencia(nuevaData.getAniosExperiencia());
            existente.setDisponible(nuevaData.isDisponible());
            return veterinarioRepository.save(existente);
        });
    }

    public boolean eliminar(int id) {
        if (veterinarioRepository.existsById(id)) {
            veterinarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Situaciones especiales: las reglas se resuelven en el servicio.
    public List<Veterinario> buscarDisponiblesPorEspecialidad(String especialidad) {
        return veterinarioRepository.findByEspecialidadIgnoreCaseAndDisponibleTrue(especialidad);
    }

    public Optional<Veterinario> obtenerMasExperimentado() {
        return veterinarioRepository.findFirstByOrderByAniosExperienciaDescIdAsc();
    }

    @Transactional
    public Optional<Veterinario> cambiarDisponibilidad(int id, boolean valor) {
        return veterinarioRepository.findById(id).map(veterinario -> {
            if (veterinario.isDisponible() == valor) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tiene esa disponibilidad");
            }
            veterinario.setDisponible(valor);
            return veterinarioRepository.save(veterinario);
        });
    }
}
