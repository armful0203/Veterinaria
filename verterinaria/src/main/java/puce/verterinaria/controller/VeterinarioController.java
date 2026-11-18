package puce.verterinaria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puce.verterinaria.model.Veterinario;
import puce.verterinaria.services.VeterinarioService;
import java.util.List;

@RestController
@RequestMapping("/api/veterinario")
public class VeterinarioController {
    private final VeterinarioService veterinarioService;

    public VeterinarioController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping
    public List<Veterinario> verTodos() {
        return veterinarioService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veterinario> traerUno(@PathVariable int id) {
        return veterinarioService.traerUno(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Veterinario> guardar(@RequestBody Veterinario veterinario) {
        Veterinario nuevo = veterinarioService.guardar(veterinario);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veterinario> editar(@PathVariable int id, @RequestBody Veterinario veterinario) {
        return veterinarioService.editar(id, veterinario)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (veterinarioService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Situaciones especiales
    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Veterinario>> porEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(veterinarioService.buscarDisponiblesPorEspecialidad(especialidad));
    }

    @GetMapping("/mas-experimentado")
    public ResponseEntity<Veterinario> masExperimentado() {
        return veterinarioService.obtenerMasExperimentado()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/disponibilidad")
    public ResponseEntity<Veterinario> disponibilidad(@PathVariable int id, @RequestParam boolean valor) {
        return veterinarioService.cambiarDisponibilidad(id, valor)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
