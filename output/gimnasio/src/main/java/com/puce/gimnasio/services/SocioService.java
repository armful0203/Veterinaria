package com.puce.gimnasio.services;

import com.puce.gimnasio.model.Socio;
import com.puce.gimnasio.repository.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SocioService {
    private final SocioRepository socioRepository;

    public SocioService(SocioRepository socioRepository) {
        this.socioRepository = socioRepository;
    }

    public List<Socio> traerTodos() {
        return socioRepository.findAll();
    }

    public Optional<Socio> traerUno(int id) {
        return socioRepository.findById(id);
    }

    public Socio guardar(Socio socio) {
        socio.setId(0);
        return socioRepository.save(socio);
    }

    public Optional<Socio> editar(int id, Socio nuevaData) {
        return socioRepository.findById(id).map(existente -> {
            existente.setNombre(nuevaData.getNombre());
            existente.setPlan(nuevaData.getPlan());
            existente.setMensualidad(nuevaData.getMensualidad());
            existente.setActivo(nuevaData.isActivo());
            return socioRepository.save(existente);
        });
    }

    public boolean eliminar(int id) {
        if (socioRepository.existsById(id)) {
            socioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public double calcularIngresos() {
        return socioRepository.findByActivoTrue().stream()
                .mapToDouble(Socio::getMensualidad).sum();
    }

    @Transactional
    public Optional<Socio> desactivar(int id) {
        return socioRepository.findById(id).map(socio -> {
            if (!socio.isActivo()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El socio ya esta inactivo");
            }
            socio.setActivo(false);
            return socioRepository.save(socio);
        });
    }

}
