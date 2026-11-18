package puce.verterinaria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import puce.verterinaria.model.Mascota;
import puce.verterinaria.services.MascotaService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mascota")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }


    @GetMapping
    public List<Mascota> verTodos() {
        return mascotaService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> traerUno(@PathVariable int id) {
        return mascotaService.traerUno(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Mascota> guardar(@RequestBody Mascota mascota) {
        Mascota nuevaMascota = mascotaService.guardar(mascota);
        return new ResponseEntity<>(nuevaMascota, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> editar(@PathVariable int id, @RequestBody Mascota mascota) {
        return mascotaService.editar(id, mascota)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (mascotaService.eliminar(id)) {
            return ResponseEntity.noContent().build(); // Código 204
        }
        return ResponseEntity.notFound().build(); // Código 404
    }

    // --- SITUACIONES ESPECIALES ---

    // GET /api/mascota/especie/{especie}
    @GetMapping("/especie/{especie}")
    public ResponseEntity<List<Mascota>> porEspecie(@PathVariable String especie) {
        return ResponseEntity.ok(mascotaService.buscarPorEspecie(especie));
    }

    // GET /api/mascota/dueno/{dueno}
    @GetMapping("/dueno/{dueno}")
    public ResponseEntity<List<Mascota>> porDueno(@PathVariable String dueno) {
        return ResponseEntity.ok(mascotaService.buscarPorDueno(dueno));
    }

    // GET /api/mascota/cachorros
    @GetMapping("/cachorros")
    public ResponseEntity<List<Mascota>> cachorros() {
        return ResponseEntity.ok(mascotaService.obtenerCachorros());
    }

    // GET /api/mascota/conteo-especies
    @GetMapping("/conteo-especies")
    public ResponseEntity<Map<String, Long>> conteoPorEspecie() {
        return ResponseEntity.ok(mascotaService.conteoPorEspecie());
    }
}

