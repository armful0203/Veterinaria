package puce.verterinaria.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import puce.verterinaria.model.Veterinario;
import puce.verterinaria.repository.VeterinarioRepository;
import java.lang.reflect.Proxy;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class VeterinarioServiceTests {
    private Veterinario registro;
    private VeterinarioService service;
    private int guardados;
    private int consultas;

    @BeforeEach
    void preparar() {
        registro = new Veterinario();
        guardados = 0;
        consultas = 0;
        // Doble de repositorio en memoria: no necesita base de datos ni agentes JVM.
        var repository = (VeterinarioRepository) Proxy.newProxyInstance(
                VeterinarioRepository.class.getClassLoader(),
                new Class<?>[]{VeterinarioRepository.class}, (proxy, method, args) -> {
                    if (method.getName().equals("findById") || method.getName().startsWith("findFirstBy")) {
                        consultas++;
                        return Optional.ofNullable(registro);
                    }
                    if (method.getName().equals("save")) {
                        guardados++;
                        return args[0];
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        service = new VeterinarioService(repository);
    }

    @Test
    void disponibilidadRepetidaNoSeGuarda() {
        registro.setDisponible(true);
        var error = assertThrows(ResponseStatusException.class, () -> service.cambiarDisponibilidad(1, true));
        assertEquals(409, error.getStatusCode().value());
        assertTrue(registro.isDisponible());
        assertEquals(0, guardados);
    }

    @Test
    void cambiaDisponibilidad() {
        registro.setDisponible(true);
        assertFalse(service.cambiarDisponibilidad(1, false).orElseThrow().isDisponible());
        assertEquals(1, guardados);
    }

    @Test
    void inexistenteYTablaVacia() {
        registro = null;
        assertTrue(service.cambiarDisponibilidad(1, false).isEmpty());
        assertTrue(service.obtenerMasExperimentado().isEmpty());
        assertEquals(0, guardados);
    }
}
