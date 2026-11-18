package com.puce.gimnasio.repository;

import com.puce.gimnasio.model.ClaseGrupal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClaseGrupalRepository extends JpaRepository<ClaseGrupal, Integer> {
    List<ClaseGrupal> findByCuposGreaterThanOrderByCuposDesc(int cupos);
}
