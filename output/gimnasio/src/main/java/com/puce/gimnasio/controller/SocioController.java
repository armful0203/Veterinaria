package com.puce.gimnasio.controller;

import com.puce.gimnasio.model.Socio;
import com.puce.gimnasio.services.SocioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/socio")
public class SocioController {
    private final SocioService socioService;

    public SocioController(SocioService socioService) {
        this.socioService = socioService;
    }

    @GetMapping
    public List<Socio> verTodos() {
        return socioService.traerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Socio> traerUno(@PathVariable int id) {
        return socioService.traerUno(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Socio> guardar(@RequestBody Socio socio) {
        return new ResponseEntity<>(socioService.guardar(socio), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Socio> editar(@PathVariable int id, @RequestBody Socio socio) {
        return socioService.editar(id, socio).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {
        if (socioService.eliminar(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ingresos")
    public ResponseEntity<Double> ingresos() {
        return ResponseEntity.ok(socioService.calcularIngresos());
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Socio> desactivar(@PathVariable int id) {
        return socioService.desactivar(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
