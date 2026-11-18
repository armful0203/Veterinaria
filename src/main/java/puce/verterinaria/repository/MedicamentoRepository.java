package puce.verterinaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import puce.verterinaria.model.Medicamento;
import java.util.List;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {
    List<Medicamento> findByStockLessThan(int minimo);
    List<Medicamento> findByRequiereRecetaFalse();
}
