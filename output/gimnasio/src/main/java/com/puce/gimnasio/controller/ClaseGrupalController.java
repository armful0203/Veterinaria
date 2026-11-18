package com.puce.gimnasio.controller;

import com.puce.gimnasio.model.ClaseGrupal;
import com.puce.gimnasio.services.ClaseGrupalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clase")
public class ClaseGrupalController {
    private final ClaseGrupalService claseGrupalService;

    public ClaseGrupalController(ClaseGrupalService claseGrupalService) {
        this.claseGrupalService = claseGrupalService;
    }

    @GetMapping
    public List<ClaseGrupal> verTodos() {
        return claseGrupalService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaseGrupal> traerUno(@PathVariable int id) {
        return claseGrupalService.traerUno(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ClaseGrupal> guardar(@RequestBody ClaseGrupal claseGrupal) {
        return new ResponseEntity<>(claseGrupalService.guardar(claseGrupal), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClaseGrupal> editar(@PathVariable int id, @RequestBody ClaseGrupal claseGrupal) {
        return claseGrupalService.editar(id, claseGrupal).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (claseGrupalService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/inscribir")
    public ResponseEntity<ClaseGrupal> inscribir(@PathVariable int id) {
        return claseGrupalService.inscribir(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/cancelar-inscripcion")
    public ResponseEntity<ClaseGrupal> cancelarInscripcion(@PathVariable int id) {
        return claseGrupalService.cancelarInscripcion(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/con-cupos")
    public ResponseEntity<List<ClaseGrupal>> conCupos() {
        return ResponseEntity.ok(claseGrupalService.obtenerConCupos());
    }

}
