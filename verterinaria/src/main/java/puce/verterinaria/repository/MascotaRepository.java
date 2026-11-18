package puce.verterinaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puce.verterinaria.model.Mascota;

import java.util.List;

public interface MascotaRepository extends JpaRepository <Mascota, Integer >{
    List<Mascota> findByEspecieIgnoreCase(String especie);
    List<Mascota> findByDuenoIgnoreCase(String dueno);
}
