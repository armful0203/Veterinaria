package puce.verterinaria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puce.verterinaria.model.Medicamento;
import puce.verterinaria.services.MedicamentoService;
import java.util.List;

@RestController
@RequestMapping("/api/medicamento")
public class MedicamentoController {
    private final MedicamentoService medicamentoService;

    public MedicamentoController(MedicamentoService medicamentoService) {
        this.medicamentoService = medicamentoService;
    }

    @GetMapping
    public List<Medicamento> verTodos() {
        return medicamentoService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicamento> traerUno(@PathVariable int id) {
        return medicamentoService.traerUno(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Medicamento> guardar(@RequestBody Medicamento medicamento) {
        Medicamento nuevo = medicamentoService.guardar(medicamento);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medicamento> editar(@PathVariable int id, @RequestBody Medicamento medicamento) {
        return medicamentoService.editar(id, medicamento)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (medicamentoService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Situaciones especiales
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Medicamento>> stockBajo(@RequestParam(defaultValue = "5") int minimo) {
        return ResponseEntity.ok(medicamentoService.buscarStockBajo(minimo));
    }

    @GetMapping("/venta-libre")
    public ResponseEntity<List<Medicamento>> ventaLibre() {
        return ResponseEntity.ok(medicamentoService.obtenerVentaLibre());
    }

    @PutMapping("/{id}/despachar")
    public ResponseEntity<Medicamento> despachar(@PathVariable int id, @RequestParam int cantidad) {
        return medicamentoService.despachar(id, cantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
