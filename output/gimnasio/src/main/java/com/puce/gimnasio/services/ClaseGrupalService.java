package com.puce.gimnasio.services;

import com.puce.gimnasio.model.ClaseGrupal;
import com.puce.gimnasio.repository.ClaseGrupalRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClaseGrupalService {
    private final ClaseGrupalRepository claseGrupalRepository;

    public ClaseGrupalService(ClaseGrupalRepository claseGrupalRepository) {
        this.claseGrupalRepository = claseGrupalRepository;
    }

    public List<ClaseGrupal> traerTodos() {
        return claseGrupalRepository.findAll();
    }

    public Optional<ClaseGrupal> traerUno(int id) {
        return claseGrupalRepository.findById(id);
    }

    public ClaseGrupal guardar(ClaseGrupal claseGrupal) {
        claseGrupal.setId(0);
        return claseGrupalRepository.save(claseGrupal);
    }

    public Optional<ClaseGrupal> editar(int id, ClaseGrupal nuevaData) {
        return claseGrupalRepository.findById(id).map(existente -> {
            existente.setNombre(nuevaData.getNombre());
            existente.setInstructor(nuevaData.getInstructor());
            existente.setHorario(nuevaData.getHorario());
            existente.setCupos(nuevaData.getCupos());
            return claseGrupalRepository.save(existente);
        });
    }

    public boolean eliminar(int id) {
        if (claseGrupalRepository.existsById(id)) {
            claseGrupalRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<ClaseGrupal> inscribir(int id) {
        return claseGrupalRepository.findById(id).map(clase -> {
            if (clase.getCupos() <= 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La clase no tiene cupos disponibles");
            }
            clase.setCupos(clase.getCupos() - 1);
            return claseGrupalRepository.save(clase);
        });
    }

    @Transactional
    public Optional<ClaseGrupal> cancelarInscripcion(int id) {
        return claseGrupalRepository.findById(id).map(clase -> {
            clase.setCupos(clase.getCupos() + 1);
            return claseGrupalRepository.save(clase);
        });
    }

    public List<ClaseGrupal> obtenerConCupos() {
        return claseGrupalRepository.findByCuposGreaterThanOrderByCuposDesc(0);
    }

}
