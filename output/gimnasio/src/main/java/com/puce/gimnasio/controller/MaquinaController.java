package com.puce.gimnasio.controller;

import com.puce.gimnasio.model.Maquina;
import com.puce.gimnasio.services.MaquinaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maquina")
public class MaquinaController {
    private final MaquinaService maquinaService;

    public MaquinaController(MaquinaService maquinaService) {
        this.maquinaService = maquinaService;
    }

    @GetMapping
    public List<Maquina> verTodos() {
        return maquinaService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Maquina> traerUno(@PathVariable int id) {
        return maquinaService.traerUno(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Maquina> guardar(@RequestBody Maquina maquina) {
        return new ResponseEntity<>(maquinaService.guardar(maquina), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Maquina> editar(@PathVariable int id, @RequestBody Maquina maquina) {
        return maquinaService.editar(id, maquina).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (maquinaService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/operativas")
    public ResponseEntity<List<Maquina>> operativas() {
        return ResponseEntity.ok(maquinaService.obtenerOperativas());
    }

    @GetMapping("/antiguas")
    public ResponseEntity<List<Maquina>> antiguas(@RequestParam(defaultValue = "5") int anios) {
        return ResponseEntity.ok(maquinaService.obtenerAntiguas(anios));
    }

    @PutMapping("/{id}/mantenimiento")
    public ResponseEntity<Maquina> mantenimiento(@PathVariable int id) {
        return maquinaService.enviarAMantenimiento(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
