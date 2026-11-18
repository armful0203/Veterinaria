from pathlib import Path
import json

root = Path(__file__).parent / 'gimnasio'
base = root / 'src/main/java/com/puce/gimnasio'
def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text.strip() + '\n', encoding='utf-8')

models = {
    'Socio': [('String','nombre'),('String','plan'),('double','mensualidad'),('boolean','activo')],
    'ClaseGrupal': [('String','nombre'),('String','instructor'),('String','horario'),('int','cupos')],
    'Maquina': [('String','nombre'),('String','marca'),('int','anioCompra'),('boolean','enMantenimiento')]
}
queries = {
    'Socio': '    List<Socio> findByActivoTrue();',
    'ClaseGrupal': '    List<ClaseGrupal> findByCuposGreaterThanOrderByCuposDesc(int cupos);',
    'Maquina': '    List<Maquina> findByEnMantenimientoFalse();\n    List<Maquina> findByAnioCompraLessThan(int anio);'
}
special_services = {
'Socio': '''
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
''',
'ClaseGrupal': '''
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
''',
'Maquina': '''
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
'''
}
special_controllers = {
'Socio': '''
    @GetMapping("/ingresos")
    public ResponseEntity<Double> ingresos() {
        return ResponseEntity.ok(socioService.calcularIngresos());
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Socio> desactivar(@PathVariable int id) {
        return socioService.desactivar(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
''',
'ClaseGrupal': '''
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
''',
'Maquina': '''
    @GetMapping("/operativas")
    public ResponseEntity<List<Maquina>> operativas() {
        return ResponseEntity.ok(maquinaService.obtenerOperativas());
    }

    @GetMapping("/antiguas")
    public ResponseEntity<List<Maquina>> antiguas(@RequestParam(defaultValue = "5") int anios) {
        return ResponseEntity.ok(maquinaService.obtenerAntiguas(anios));
    }

    @PutMapping("/{id}/mantenimiento")
    public ResponseEntity<Maquina> mantenimiento(@PathVariable int id) {
        return maquinaService.enviarAMantenimiento(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
'''
}
routes = {'Socio':'socio','ClaseGrupal':'clase','Maquina':'maquina'}
for model, fields in models.items():
    var = model[0].lower() + model[1:]
    table = '\n@Table(name = "clase_grupal")' if model == 'ClaseGrupal' else ''
    write(base/'model'/f'{model}.java', f'''package com.puce.gimnasio.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity{table}
public class {model} {{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
''' + '\n'.join(f'    private {typ} {name};' for typ,name in fields) + '\n}')
    write(base/'repository'/f'{model}Repository.java', f'''package com.puce.gimnasio.repository;

import com.puce.gimnasio.model.{model};
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface {model}Repository extends JpaRepository<{model}, Integer> {{
{queries[model]}
}}''')
    setters = '\n'.join(f'            existente.set{name[0].upper()+name[1:]}(nuevaData.{"is" if typ=="boolean" else "get"}{name[0].upper()+name[1:]}());' for typ,name in fields)
    write(base/'services'/f'{model}Service.java', f'''package com.puce.gimnasio.services;

import com.puce.gimnasio.model.{model};
import com.puce.gimnasio.repository.{model}Repository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class {model}Service {{
    private final {model}Repository {var}Repository;

    public {model}Service({model}Repository {var}Repository) {{
        this.{var}Repository = {var}Repository;
    }}

    public List<{model}> traerTodos() {{
        return {var}Repository.findAll();
    }}

    public Optional<{model}> traerUno(int id) {{
        return {var}Repository.findById(id);
    }}

    public {model} guardar({model} {var}) {{
        {var}.setId(0);
        return {var}Repository.save({var});
    }}

    public Optional<{model}> editar(int id, {model} nuevaData) {{
        return {var}Repository.findById(id).map(existente -> {{
{setters}
            return {var}Repository.save(existente);
        }});
    }}

    public boolean eliminar(int id) {{
        if ({var}Repository.existsById(id)) {{
            {var}Repository.deleteById(id);
            return true;
        }}
        return false;
    }}
{special_services[model]}
}}''')
    write(base/'controller'/f'{model}Controller.java', f'''package com.puce.gimnasio.controller;

import com.puce.gimnasio.model.{model};
import com.puce.gimnasio.services.{model}Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/{routes[model]}")
public class {model}Controller {{
    private final {model}Service {var}Service;

    public {model}Controller({model}Service {var}Service) {{
        this.{var}Service = {var}Service;
    }}

    @GetMapping
    public List<{model}> verTodos() {{
        return {var}Service.traerTodos();
    }}

    @GetMapping("/{{id}}")
    public ResponseEntity<{model}> traerUno(@PathVariable int id) {{
        return {var}Service.traerUno(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }}

    @PostMapping
    public ResponseEntity<{model}> guardar(@RequestBody {model} {var}) {{
        return new ResponseEntity<>({var}Service.guardar({var}), HttpStatus.CREATED);
    }}

    @PutMapping("/{{id}}")
    public ResponseEntity<{model}> editar(@PathVariable int id, @RequestBody {model} {var}) {{
        return {var}Service.editar(id, {var}).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }}

    @DeleteMapping("/{{id}}")
    public ResponseEntity<Void> eliminar(@PathVariable int id) {{
        if ({var}Service.eliminar(id)) {{
            return ResponseEntity.noContent().build();
        }}
        return ResponseEntity.notFound().build();
    }}
{special_controllers[model]}
}}''')

write(root/'src/main/resources/application.properties', '''spring.application.name=gimnasio
server.port=${SERVER_PORT:9091}

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/gimnasio}
spring.datasource.username=${DB_USERNAME:gimnasio_app}
spring.datasource.password=${DB_PASSWORD:gimnasio123}
spring.jpa.hibernate.ddl-auto=update
server.error.include-message=always
''')

write(root/'database/script.sql', """-- PostgreSQL: ejecutar el archivo completo con psql como administrador.
-- psql -U postgres -d postgres -f database/script.sql
-- Incluye comandos propios de psql; no pegar completo en Query Tool de pgAdmin.
\\set ON_ERROR_STOP on
SELECT 'CREATE ROLE gimnasio_app LOGIN PASSWORD ''gimnasio123'''
WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'gimnasio_app')\\gexec
SELECT 'CREATE DATABASE gimnasio OWNER gimnasio_app'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'gimnasio')\\gexec
\\connect gimnasio
GRANT CONNECT ON DATABASE gimnasio TO gimnasio_app;
GRANT USAGE, CREATE ON SCHEMA public TO gimnasio_app;
SET ROLE gimnasio_app;

CREATE TABLE IF NOT EXISTS socio (
    id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nombre varchar(255), plan varchar(255), mensualidad double precision NOT NULL,
    activo boolean NOT NULL
);
CREATE TABLE IF NOT EXISTS clase_grupal (
    id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nombre varchar(255), instructor varchar(255), horario varchar(255), cupos integer NOT NULL
);
CREATE TABLE IF NOT EXISTS maquina (
    id integer GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    nombre varchar(255), marca varchar(255), anio_compra integer NOT NULL,
    en_mantenimiento boolean NOT NULL
);

-- En una base existente carga los ejemplos solo cuando la tabla esta vacia.
INSERT INTO socio (nombre, plan, mensualidad, activo)
SELECT * FROM (VALUES
 ('Carlos Mena','mensual',30.0,true), ('Ana Perez','trimestral',25.0,true),
 ('Luis Torres','mensual',30.0,false), ('Maria Ruiz','anual',20.0,true),
 ('Juan Cevallos','mensual',35.0,true)
) AS datos(nombre,plan,mensualidad,activo) WHERE NOT EXISTS (SELECT 1 FROM socio);

INSERT INTO clase_grupal (nombre, instructor, horario, cupos)
SELECT * FROM (VALUES
 ('Spinning','Maria Ruiz','Lunes 18:00',15), ('Yoga','Ana Perez','Martes 08:00',8),
 ('Zumba','Luis Torres','Miercoles 19:00',0), ('Pilates','Sofia Mena','Jueves 09:00',5),
 ('Funcional','Carlos Mena','Viernes 17:00',20)
) AS datos(nombre,instructor,horario,cupos) WHERE NOT EXISTS (SELECT 1 FROM clase_grupal);

INSERT INTO maquina (nombre, marca, anio_compra, en_mantenimiento)
SELECT * FROM (VALUES
 ('Prensa de piernas','Technogym',2019,false), ('Caminadora','Life Fitness',2024,false),
 ('Bicicleta estatica','Matrix',2018,true), ('Polea alta','Technogym',2022,false),
 ('Banco de pecho','Body Solid',2015,false)
) AS datos(nombre,marca,anio_compra,en_mantenimiento) WHERE NOT EXISTS (SELECT 1 FROM maquina);
RESET ROLE;
""")

# Cada carpeta usa registros propios y guarda los ids devueltos por POST.
folders=[]
def request(items, name, method, path, status=200, body=None, checks=None):
    req={'method':method,'header':[],'url':'{{baseUrl}}'+path}
    if body is not None:
        req['header']=[{'key':'Content-Type','value':'application/json'}]
        req['body']={'mode':'raw','raw':json.dumps(body,ensure_ascii=False,indent=2),'options':{'raw':{'language':'json'}}}
    scripts=[f'pm.test("Estado {status}", () => pm.response.to.have.status({status}));']
    scripts+=checks or []
    items.append({'name':name,'request':req,'event':[{'listen':'test','script':{'type':'text/javascript','exec':scripts}}]})
samples={
'Socio':{'nombre':'Prueba Postman','plan':'mensual','mensualidad':30.0,'activo':True},
'ClaseGrupal':{'nombre':'Prueba Postman','instructor':'Maria Ruiz','horario':'Lunes 18:00','cupos':1},
'Maquina':{'nombre':'Prueba Postman','marca':'Technogym','anioCompra':2019,'enMantenimiento':False}}
for model in models:
    items=[]; route='/api/'+routes[model]; key=routes[model]+'Id'; ident='{{'+key+'}}'; path=route+'/'+ident
    request(items,'01 - Listar','GET',route)
    request(items,'02 - Crear','POST',route,201,samples[model],[f'pm.collectionVariables.set("{key}", pm.response.json().id);'])
    request(items,'03 - Traer uno','GET',path)
    request(items,'04 - Editar','PUT',path,200,{**samples[model],'nombre':'Prueba Postman editada'})
    if model=='Socio':
        request(items,'05 - Ingresos activos','GET',route+'/ingresos',checks=['pm.test("Ingreso numerico", () => pm.expect(pm.response.json()).to.be.a("number"));'])
        request(items,'06 - Desactivar','PUT',path+'/desactivar',checks=['pm.test("Inactivo", () => pm.expect(pm.response.json().activo).to.eql(false));'])
        request(items,'07 - Desactivar otra vez (409)','PUT',path+'/desactivar',409)
    elif model=='ClaseGrupal':
        request(items,'05 - Con cupos ordenados','GET',route+'/con-cupos',checks=['const clases = pm.response.json();','pm.test("Cupos positivos y orden descendente", () => { clases.forEach((c,i) => { pm.expect(c.cupos).to.be.above(0); if(i) pm.expect(clases[i-1].cupos).to.be.at.least(c.cupos); }); });'])
        request(items,'06 - Inscribir ultimo cupo','PUT',path+'/inscribir',checks=['pm.test("Sin cupos", () => pm.expect(pm.response.json().cupos).to.eql(0));'])
        request(items,'07 - Inscribir sin cupos (409)','PUT',path+'/inscribir',409)
        request(items,'08 - Cancelar inscripcion','PUT',path+'/cancelar-inscripcion',checks=['pm.test("Cupo devuelto", () => pm.expect(pm.response.json().cupos).to.eql(1));'])
    else:
        request(items,'05 - Operativas','GET',route+'/operativas',checks=['pm.test("Solo operativas", () => pm.response.json().forEach(m => pm.expect(m.enMantenimiento).to.eql(false)));'])
        request(items,'06 - Antiguas','GET',route+'/antiguas?anios=5',checks=['pm.test("Mas de cinco anios", () => pm.response.json().forEach(m => pm.expect(new Date().getFullYear()-m.anioCompra).to.be.above(5)));'])
        request(items,'07 - Mantenimiento','PUT',path+'/mantenimiento',checks=['pm.test("En mantenimiento", () => pm.expect(pm.response.json().enMantenimiento).to.eql(true));'])
        request(items,'08 - Mantenimiento repetido (409)','PUT',path+'/mantenimiento',409)
    request(items,'09 - Eliminar registro de prueba','DELETE',path,204)
    request(items,'10 - Traer eliminado (404)','GET',path,404)
    request(items,'11 - Editar eliminado (404)','PUT',path,404,samples[model])
    request(items,'12 - Eliminar inexistente (404)','DELETE',path,404)
    specials={'Socio':['desactivar'],'ClaseGrupal':['inscribir','cancelar-inscripcion'],'Maquina':['mantenimiento']}[model]
    for endpoint in specials:
        request(items,f'13 - {endpoint} inexistente (404)','PUT',path+'/'+endpoint,404)
    folders.append({'name':model,'item':items})
collection={'info':{'name':'Gimnasio - Opcion 5','description':'Ejecutar en orden con Collection Runner. Crea y borra registros de prueba propios; no depende de ids del script.','schema':'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},'variable':[{'key':'baseUrl','value':'http://localhost:9091'}],'item':folders}
write(root/'postman/gimnasio.postman_collection.json',json.dumps(collection,ensure_ascii=False,indent=2))

print('Generados 12 archivos de las cuatro capas, configuracion, SQL y Postman.')
