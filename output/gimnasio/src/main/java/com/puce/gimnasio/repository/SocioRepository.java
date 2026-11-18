package com.puce.gimnasio.repository;

import com.puce.gimnasio.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SocioRepository extends JpaRepository<Socio, Integer> {
    List<Socio> findByActivoTrue();
}
