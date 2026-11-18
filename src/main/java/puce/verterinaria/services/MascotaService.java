package puce.verterinaria.services;


import org.springframework.stereotype.Service;
import puce.verterinaria.model.Mascota;
import puce.verterinaria.repository.MascotaRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
public class MascotaService {

    private MascotaRepository mascotaRepository;

    public MascotaService(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }


    // traer todos

    public List<Mascota> traerTodos(){
        return mascotaRepository.findAll();
    }

    // Traer uno
    public Optional<Mascota> traerUno(int id){
        return mascotaRepository.findById(id);
    }

    //Guardar
    public Mascota guardar(Mascota mascota){
        return mascotaRepository.save(mascota);
    }

    // editar
    public Optional<Mascota> editar(int id, Mascota nuevaData) {
        return mascotaRepository.findById(id).map(mascotaExistente -> {
            mascotaExistente.setNombre(nuevaData.getNombre());
            mascotaExistente.setEspecie(nuevaData.getEspecie());
            mascotaExistente.setEdad(nuevaData.getEdad());
            mascotaExistente.setDueno(nuevaData.getDueno());
            return mascotaRepository.save(mascotaExistente);
        });
    }

    public boolean eliminar(int id) {
        if (mascotaRepository.existsById(id)) {
            mascotaRepository.deleteById(id);
            return true;
        }
        return false;
    }


    // Situaciones Especiales

    // 1. Mascotas de esa especie, sin importar mayúsculas
    public List<Mascota> buscarPorEspecie(String especie) {
        return mascotaRepository.findByEspecieIgnoreCase(especie);
    }
    // 2. Mascotas de un mismo dueño
    public List<Mascota> buscarPorDueno(String dueno) {
        return mascotaRepository.findByDuenoIgnoreCase(dueno);
    }
    // 3. Mascotas de 1 año o menos
    public List<Mascota> obtenerCachorros() {
        return mascotaRepository.findAll().stream()
                .filter(m -> m.getEdad() <= 1)
                .toList();
    }
    // 4. Conteo de cuántas mascotas hay por especie: {"perro": 3, "gato": 2}
    public Map<String, Long> conteoPorEspecie() {
        return mascotaRepository.findAll().stream()
                .collect(Collectors.groupingBy(Mascota::getEspecie, Collectors.counting()));
    }



}
