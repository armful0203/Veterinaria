package com.puce.gimnasio;

import com.puce.gimnasio.controller.*;
import com.puce.gimnasio.model.*;
import com.puce.gimnasio.repository.*;
import com.puce.gimnasio.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Proxy;
import java.time.Year;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Pruebas HTTP y de reglas con repositorios en memoria. No prueban PostgreSQL.
class GimnasioApiTests {
    private static class Memoria<T> {
        final Map<Integer, T> filas = new LinkedHashMap<>();
        final Function<T, Integer> id;
        int siguiente = 1;
        Memoria(Function<T, Integer> id) { this.id = id; }

        @SuppressWarnings("unchecked")
        <R> R repositorio(Class<R> tipo) {
            return (R) Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (p, m, args) -> {
                return switch (m.getName()) {
                    case "save" -> {
                        T fila = (T) args[0];
                        int clave = id.apply(fila);
                        if (clave == 0) {
                            clave = siguiente++;
                            fila.getClass().getMethod("setId", int.class).invoke(fila, clave);
                        }
                        filas.put(clave, fila);
                        yield fila;
                    }
                    case "findAll" -> new ArrayList<>(filas.values());
                    case "findById" -> Optional.ofNullable(filas.get((Integer) args[0]));
                    case "existsById" -> filas.containsKey((Integer) args[0]);
                    case "deleteById" -> { filas.remove((Integer) args[0]); yield null; }
                    case "findByActivoTrue" -> filas.values().stream().map(Socio.class::cast)
                            .filter(Socio::isActivo).toList();
                    case "findByCuposGreaterThanOrderByCuposDesc" -> filas.values().stream()
                            .map(ClaseGrupal.class::cast).filter(c -> c.getCupos() > (Integer) args[0])
                            .sorted(Comparator.comparingInt(ClaseGrupal::getCupos).reversed()).toList();
                    case "findByEnMantenimientoFalse" -> filas.values().stream().map(Maquina.class::cast)
                            .filter(c -> !c.isEnMantenimiento()).toList();
                    case "findByAnioCompraLessThan" -> filas.values().stream().map(Maquina.class::cast)
                            .filter(c -> c.getAnioCompra() < (Integer) args[0]).toList();
                    default -> throw new UnsupportedOperationException(m.getName());
                };
            });
        }
    }

    private void crud(MockMvc api, String ruta, String cuerpo) throws Exception {
        api.perform(get(ruta)).andExpect(status().isOk()).andExpect(content().json("[]"));
        api.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        api.perform(get(ruta + "/1")).andExpect(status().isOk());
        api.perform(put(ruta + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo.replace("Original", "Editado")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Editado"));
        api.perform(delete(ruta + "/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        api.perform(get(ruta + "/1")).andExpect(status().isNotFound());
        api.perform(put(ruta + "/1").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isNotFound());
        api.perform(delete(ruta + "/1")).andExpect(status().isNotFound());
    }

    @Test
    void crudSocioConEstadosHttp() throws Exception {
        var datos = new Memoria<Socio>(Socio::getId);
        var api = MockMvcBuilders.standaloneSetup(new SocioController(
                new SocioService(datos.repositorio(SocioRepository.class)))).build();
        crud(api, "/api/socio", """
                {"id":99,"nombre":"Original","plan":"mensual","mensualidad":30,"activo":true}
                """);
    }

    @Test
    void crudClaseConEstadosHttp() throws Exception {
        var datos = new Memoria<ClaseGrupal>(ClaseGrupal::getId);
        var api = MockMvcBuilders.standaloneSetup(new ClaseGrupalController(
                new ClaseGrupalService(datos.repositorio(ClaseGrupalRepository.class)))).build();
        crud(api, "/api/clase", """
                {"nombre":"Original","instructor":"Ana","horario":"Lunes 18:00","cupos":1}
                """);
    }

    @Test
    void crudMaquinaConEstadosHttp() throws Exception {
        var datos = new Memoria<Maquina>(Maquina::getId);
        var api = MockMvcBuilders.standaloneSetup(new MaquinaController(
                new MaquinaService(datos.repositorio(MaquinaRepository.class)))).build();
        crud(api, "/api/maquina", """
                {"nombre":"Original","marca":"Matrix","anioCompra":2019,"enMantenimiento":false}
                """);
    }

    @Test
    void ingresosSoloActivosYDesactivacionRepetida() throws Exception {
        var datos = new Memoria<Socio>(Socio::getId);
        var servicio = new SocioService(datos.repositorio(SocioRepository.class));
        var api = MockMvcBuilders.standaloneSetup(new SocioController(servicio)).build();
        api.perform(get("/api/socio/ingresos")).andExpect(status().isOk()).andExpect(content().string("0.0"));
        Socio activo = new Socio(); activo.setActivo(true); activo.setMensualidad(30);
        Socio inactivo = new Socio(); inactivo.setMensualidad(100);
        servicio.guardar(activo); servicio.guardar(inactivo);
        api.perform(get("/api/socio/ingresos")).andExpect(content().string("30.0"));
        api.perform(put("/api/socio/1/desactivar")).andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        api.perform(get("/api/socio/ingresos")).andExpect(content().string("0.0"));
        api.perform(put("/api/socio/1/desactivar")).andExpect(status().isConflict());
        api.perform(put("/api/socio/99/desactivar")).andExpect(status().isNotFound());
    }

    @Test
    void inscripcionAgotaUltimoCupoYCancelacionLoDevuelve() throws Exception {
        var datos = new Memoria<ClaseGrupal>(ClaseGrupal::getId);
        var servicio = new ClaseGrupalService(datos.repositorio(ClaseGrupalRepository.class));
        var api = MockMvcBuilders.standaloneSetup(new ClaseGrupalController(servicio)).build();
        ClaseGrupal clase = new ClaseGrupal(); clase.setCupos(1); servicio.guardar(clase);
        api.perform(put("/api/clase/1/inscribir")).andExpect(status().isOk()).andExpect(jsonPath("$.cupos").value(0));
        api.perform(put("/api/clase/1/inscribir")).andExpect(status().isConflict());
        assertEquals(0, clase.getCupos());
        api.perform(put("/api/clase/1/cancelar-inscripcion")).andExpect(status().isOk())
                .andExpect(jsonPath("$.cupos").value(1));
        api.perform(put("/api/clase/99/inscribir")).andExpect(status().isNotFound());
        api.perform(put("/api/clase/99/cancelar-inscripcion")).andExpect(status().isNotFound());
    }

    @Test
    void clasesConCuposEnOrdenDescendente() throws Exception {
        var datos = new Memoria<ClaseGrupal>(ClaseGrupal::getId);
        var servicio = new ClaseGrupalService(datos.repositorio(ClaseGrupalRepository.class));
        for (int cupos : new int[]{0, 2, 20, 5}) {
            ClaseGrupal clase = new ClaseGrupal(); clase.setCupos(cupos); servicio.guardar(clase);
        }
        var api = MockMvcBuilders.standaloneSetup(new ClaseGrupalController(servicio)).build();
        api.perform(get("/api/clase/con-cupos")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].cupos").value(20))
                .andExpect(jsonPath("$[2].cupos").value(2));
    }

    @Test
    void mantenimientoRepetidoNoCambiaEstadoYFiltraOperativas() throws Exception {
        var datos = new Memoria<Maquina>(Maquina::getId);
        var servicio = new MaquinaService(datos.repositorio(MaquinaRepository.class));
        servicio.guardar(new Maquina()); servicio.guardar(new Maquina());
        var api = MockMvcBuilders.standaloneSetup(new MaquinaController(servicio)).build();
        api.perform(put("/api/maquina/1/mantenimiento")).andExpect(status().isOk())
                .andExpect(jsonPath("$.enMantenimiento").value(true));
        api.perform(put("/api/maquina/1/mantenimiento")).andExpect(status().isConflict());
        api.perform(put("/api/maquina/99/mantenimiento")).andExpect(status().isNotFound());
        api.perform(get("/api/maquina/operativas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void antiguasUsaAnioActualYLimiteEstricto() throws Exception {
        var datos = new Memoria<Maquina>(Maquina::getId);
        var servicio = new MaquinaService(datos.repositorio(MaquinaRepository.class));
        for (int antiguedad : new int[]{6, 5, 4}) {
            Maquina maquina = new Maquina();
            maquina.setAnioCompra(Year.now().getValue() - antiguedad);
            servicio.guardar(maquina);
        }
        var api = MockMvcBuilders.standaloneSetup(new MaquinaController(servicio)).build();
        api.perform(get("/api/maquina/antiguas").param("anios", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
        api.perform(get("/api/maquina/antiguas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        api.perform(get("/api/maquina/antiguas").param("anios", "texto"))
                .andExpect(status().isBadRequest());
    }
}
