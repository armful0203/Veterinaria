package puce.verterinaria.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import puce.verterinaria.model.Medicamento;
import puce.verterinaria.repository.MedicamentoRepository;
import java.lang.reflect.Proxy;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class MedicamentoServiceTests {
    private Medicamento registro;
    private MedicamentoService service;
    private int guardados;
    private int consultas;

    @BeforeEach
    void preparar() {
        registro = new Medicamento();
        guardados = 0;
        consultas = 0;
        // Doble de repositorio en memoria: no necesita base de datos ni agentes JVM.
        var repository = (MedicamentoRepository) Proxy.newProxyInstance(
                MedicamentoRepository.class.getClassLoader(),
                new Class<?>[]{MedicamentoRepository.class}, (proxy, method, args) -> {
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
        service = new MedicamentoService(repository);
    }

    @Test
    void rechazaCantidadesNoPositivasAntesDeConsultar() {
        for (int cantidad : new int[]{0, -1}) {
            var error = assertThrows(ResponseStatusException.class, () -> service.despachar(1, cantidad));
            assertEquals(400, error.getStatusCode().value());
        }
        assertEquals(0, consultas);
        assertEquals(0, guardados);
    }

    @Test
    void stockInsuficienteNoModificaInventario() {
        registro.setStock(3);
        var error = assertThrows(ResponseStatusException.class, () -> service.despachar(1, 4));
        assertEquals(409, error.getStatusCode().value());
        assertEquals(3, registro.getStock());
        assertEquals(0, guardados);
    }

    @Test
    void despachaHastaAgotarStock() {
        registro.setStock(3);
        assertEquals(0, service.despachar(1, 3).orElseThrow().getStock());
        assertEquals(1, guardados);
    }

    @Test
    void inexistenteNoSeGuarda() {
        registro = null;
        assertTrue(service.despachar(1, 2).isEmpty());
        assertEquals(0, guardados);
    }
}
