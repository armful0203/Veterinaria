package com.puce.gimnasio.services;

import com.puce.gimnasio.model.Maquina;
import com.puce.gimnasio.repository.MaquinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class MaquinaService {
    private final MaquinaRepository maquinaRepository;

    public MaquinaService(MaquinaRepository maquinaRepository) {
        this.maquinaRepository = maquinaRepository;
    }

    public List<Maquina> traerTodos() {
        return maquinaRepository.findAll();
    }

    public Optional<Maquina> traerUno(int id) {
        return maquinaRepository.findById(id);
    }

    public Maquina guardar(Maquina maquina) {
        maquina.setId(0);
        return maquinaRepository.save(maquina);
    }

    public Optional<Maquina> editar(int id, Maquina nuevaData) {
        return maquinaRepository.findById(id).map(existente -> {
            existente.setNombre(nuevaData.getNombre());
            existente.setMarca(nuevaData.getMarca());
            existente.setAnioCompra(nuevaData.getAnioCompra());
            existente.setEnMantenimiento(nuevaData.isEnMantenimiento());
            return maquinaRepository.save(existente);
        });
    }

    public boolean eliminar(int id) {
        if (maquinaRepository.existsById(id)) {
            maquinaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Maquina> obtenerOperativas() {
        return maquinaRepository.findByEnMantenimientoFalse();
    }

    public List<Maquina> obtenerAntiguas(int anios) {
        return maquinaRepository.findByAnioCompraLessThan(java.time.Year.now().getValue() - anios);
    }

    @Transactional
    public Optional<Maquina> enviarAMantenimiento(int id) {
        return maquinaRepository.findById(id).map(maquina -> {
            if (maquina.isEnMantenimiento()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La maquina ya esta en mantenimiento");
            }
            maquina.setEnMantenimiento(true);
            return maquinaRepository.save(maquina);
        });
    }

}
