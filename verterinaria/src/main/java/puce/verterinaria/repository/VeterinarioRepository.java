package puce.verterinaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puce.verterinaria.model.Veterinario;
import java.util.List;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario, Integer> {
    List<Veterinario> findByEspecialidadIgnoreCaseAndDisponibleTrue(String especialidad);
    Optional<Veterinario> findFirstByOrderByAniosExperienciaDescIdAsc();
}
