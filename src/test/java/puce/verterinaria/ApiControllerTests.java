package puce.verterinaria;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import puce.verterinaria.controller.*;
import puce.verterinaria.model.*;
import puce.verterinaria.repository.*;
import puce.verterinaria.services.*;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP real de Spring MVC y servicios reales; repositorios dobles en memoria, sin PostgreSQL. */
class ApiControllerTests {
    private <T> T repositorio(Class<T> tipo) {
        Map<Integer, Object> datos = new LinkedHashMap<>();
        int[] siguiente = {1};
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo},
                (proxy, metodo, args) -> {
                    String nombre = metodo.getName();
                    switch (nombre) {
                        case "save":
                            Object entidad = args[0];
                            int id = (int) entidad.getClass().getMethod("getId").invoke(entidad);
                            if (id == 0) {
                                id = siguiente[0]++;
                                entidad.getClass().getMethod("setId", int.class).invoke(entidad, id);
                            }
                            datos.put(id, entidad);
                            return entidad;
                        case "findAll": return new ArrayList<>(datos.values());
                        case "findById": return Optional.ofNullable(datos.get(args[0]));
                        case "existsById": return datos.containsKey(args[0]);
                        case "deleteById": datos.remove(args[0]); return null;
                        case "findByEspecieIgnoreCase":
                            return datos.values().stream().map(Mascota.class::cast)
                                    .filter(m -> m.getEspecie().equalsIgnoreCase((String) args[0])).toList();
                        case "findByDuenoIgnoreCase":
                            return datos.values().stream().map(Mascota.class::cast)
                                    .filter(m -> m.getDueno().equalsIgnoreCase((String) args[0])).toList();
                        case "findByStockLessThan":
                            return datos.values().stream().map(Medicamento.class::cast)
                                    .filter(m -> m.getStock() < (int) args[0]).toList();
                        case "findByRequiereRecetaFalse":
                            return datos.values().stream().map(Medicamento.class::cast)
                                    .filter(m -> !m.isRequiereReceta()).toList();
                        case "findByEspecialidadIgnoreCaseAndDisponibleTrue":
                            return datos.values().stream().map(Veterinario.class::cast)
                                    .filter(v -> v.isDisponible() && v.getEspecialidad().equalsIgnoreCase((String) args[0])).toList();
                        case "findFirstByOrderByAniosExperienciaDescIdAsc":
                            return datos.values().stream().map(Veterinario.class::cast)
                                    .sorted(Comparator.comparingInt(Veterinario::getAniosExperiencia).reversed()
                                            .thenComparingInt(Veterinario::getId)).findFirst();
                        default: throw new UnsupportedOperationException(nombre);
                    }
                }));
    }

    private void crear(MockMvc mvc, String ruta, String cuerpo) throws Exception {
        mvc.perform(get(ruta + "/0")).andExpect(status().isNotFound());
        mvc.perform(post(ruta).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(get(ruta)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(ruta + "/1")).andExpect(status().isOk());
        mvc.perform(put(ruta + "/1").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk());
        mvc.perform(put(ruta + "/0").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isNotFound());
    }

    private void borrar(MockMvc mvc, String ruta) throws Exception {
        mvc.perform(delete(ruta + "/1")).andExpect(status().isNoContent());
        mvc.perform(get(ruta + "/1")).andExpect(status().isNotFound());
        mvc.perform(delete(ruta + "/1")).andExpect(status().isNotFound());
    }

    @Test
    void mascotaCrudYConsultas() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MascotaController(
                new MascotaService(repositorio(MascotaRepository.class)))).build();
        String ruta = "/api/mascota";
        crear(mvc, ruta, """
                {"nombre":"Luna","edad":1,"especie":"perro","dueno":"Ana"}
                """);
        mvc.perform(get(ruta + "/especie/PERRO")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Luna"));
        mvc.perform(get(ruta + "/dueno/ANA")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(ruta + "/cachorros")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(ruta + "/conteo-especies")).andExpect(status().isOk()).andExpect(jsonPath("$.perro").value(1));
        borrar(mvc, ruta);
    }

    @Test
    void medicamentoCrudDespachoYErrores() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new MedicamentoController(
                new MedicamentoService(repositorio(MedicamentoRepository.class)))).build();
        String ruta = "/api/medicamento";
        crear(mvc, ruta, """
                {"nombre":"Vitaminas","precio":8.75,"stock":3,"requiereReceta":false}
                """);
        mvc.perform(get(ruta + "/stock-bajo")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(ruta + "/stock-bajo?minimo=3")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get(ruta + "/venta-libre")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(put(ruta + "/1/despachar?cantidad=0")).andExpect(status().isBadRequest());
        mvc.perform(put(ruta + "/1/despachar?cantidad=4")).andExpect(status().isConflict());
        mvc.perform(get(ruta + "/1")).andExpect(jsonPath("$.stock").value(3));
        mvc.perform(put(ruta + "/0/despachar?cantidad=1")).andExpect(status().isNotFound());
        mvc.perform(put(ruta + "/1/despachar?cantidad=2")).andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(1));
        borrar(mvc, ruta);
    }

    @Test
    void veterinarioCrudDisponibilidadYErrores() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new VeterinarioController(
                new VeterinarioService(repositorio(VeterinarioRepository.class)))).build();
        String ruta = "/api/veterinario";
        mvc.perform(get(ruta + "/mas-experimentado")).andExpect(status().isNotFound());
        crear(mvc, ruta, """
                {"nombre":"Ana","especialidad":"cirugia","aniosExperiencia":7,"disponible":true}
                """);
        mvc.perform(get(ruta + "/especialidad/CIRUGIA")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get(ruta + "/mas-experimentado")).andExpect(status().isOk()).andExpect(jsonPath("$.nombre").value("Ana"));
        mvc.perform(put(ruta + "/1/disponibilidad?valor=true")).andExpect(status().isConflict());
        mvc.perform(put(ruta + "/0/disponibilidad?valor=false")).andExpect(status().isNotFound());
        mvc.perform(put(ruta + "/1/disponibilidad?valor=false")).andExpect(status().isOk()).andExpect(jsonPath("$.disponible").value(false));
        mvc.perform(get(ruta + "/especialidad/cirugia")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        borrar(mvc, ruta);
    }
}
