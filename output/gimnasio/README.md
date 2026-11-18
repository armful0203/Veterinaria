# Gimnasio — Opción 5

API de Spring Boot para socios, clases grupales y máquinas, correspondiente a las páginas 12 y 13 de `Tarea_Back_End_Varios_Modelos(1).pdf`.

Sigue la estructura del ejercicio de veterinaria: paquetes `model`, `repository`, `services` y `controller`, inyección por constructor, `Optional` para búsquedas y edición, y `ResponseEntity` en los controladores. Las reglas de negocio están en los servicios. Los tres modelos son independientes, sin relaciones ni identificadores de otras entidades.

## Tecnologías

- Java 27 y Spring Boot 4.1.2, conservados del proyecto inicial.
- Maven Wrapper, Spring Web MVC, Spring Data JPA y Lombok.
- PostgreSQL, con el usuario de aplicación `gimnasio_app`. No utiliza H2.

## Estructura

```text
gimnasio/
├── pom.xml
├── mvnw / mvnw.cmd
├── database/script.sql
├── postman/gimnasio.postman_collection.json
├── docs/capturas/README.md
├── README.md
└── src/
    ├── main/
    │   ├── java/com/puce/gimnasio/
    │   │   ├── GimnasioApplication.java
    │   │   ├── model/          Socio, ClaseGrupal, Maquina
    │   │   ├── repository/     Un repositorio por modelo
    │   │   ├── services/       Un servicio por modelo
    │   │   └── controller/     Un controlador por modelo
    │   └── resources/application.properties
    └── test/java/com/puce/gimnasio/
        ├── GimnasioApiTests.java
        └── GimnasioApplicationTests.java
```

## Preparar la base de datos

Abre una terminal en la carpeta que contiene `pom.xml`. Con PostgreSQL iniciado, ejecuta como administrador:

```powershell
psql -h localhost -U postgres -d postgres -f database/script.sql
```

Si `psql` no está en el PATH, utiliza la ruta de tu instalación. Por ejemplo, para PostgreSQL 18 en Windows:

```powershell
& 'C:\Program Files\PostgreSQL\18\bin\psql.exe' -h localhost -U postgres -d postgres -f database/script.sql
```

El archivo usa instrucciones de **psql** (`\gexec`, `\connect`), por lo que debe ejecutarse desde esa herramienta. Crea la base `gimnasio`, el usuario `gimnasio_app`, las tres tablas y cinco registros por tabla. Las tablas no tienen claves foráneas. Se puede ejecutar antes del primer arranque; Hibernate utiliza `ddl-auto=update`.

Si la base o el usuario ya existen, no los vuelve a crear ni cambia sus contraseñas. Los ejemplos se insertan solo si la tabla correspondiente está vacía. Una base existente con otro propietario o permisos puede necesitar ajustes del administrador.

Configuración predeterminada para desarrollo:

| Parámetro | Valor |
| --- | --- |
| Puerto HTTP | `9091` |
| Base | `jdbc:postgresql://localhost:5432/gimnasio` |
| Usuario de la API | `gimnasio_app` |
| Contraseña de ejemplo | `gimnasio123` |

La aplicación usa su propio usuario; `postgres` se emplea únicamente para preparar la base. Puedes cambiar la conexión mediante `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Ajusta también el usuario y la contraseña del script si eliges otros valores.

## Ejecutar

Configura `JAVA_HOME` con tu JDK 27 y ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

URL de socios: <http://localhost:9091/api/socio>. Se eligió `9091` para poder mantener el ejercicio de veterinaria en `9090`.

Para usar otro puerto:

```powershell
$env:SERVER_PORT='9092'
.\mvnw.cmd spring-boot:run
```

Actualiza `baseUrl` en Postman si cambias el puerto. Para compilar y generar el JAR:

```powershell
.\mvnw.cmd clean package
java -jar target/gimnasio-0.0.1-SNAPSHOT.jar
```

## Modelos

Todos tienen `id` entero autogenerado. En POST no es necesario enviarlo; el servicio utiliza un identificador nuevo. En PUT se conserva el identificador de la URL y se actualizan todos los demás campos.

| Modelo | Tabla | Campos adicionales |
| --- | --- | --- |
| Socio | `socio` | `nombre: String`, `plan: String`, `mensualidad: double`, `activo: boolean` |
| ClaseGrupal | `clase_grupal` | `nombre: String`, `instructor: String`, `horario: String`, `cupos: int` |
| Maquina | `maquina` | `nombre: String`, `marca: String`, `anioCompra: int`, `enMantenimiento: boolean` |

Ejemplos para POST o PUT:

```json
{"nombre":"Carlos Mena","plan":"mensual","mensualidad":30.0,"activo":true}
```

```json
{"nombre":"Spinning","instructor":"María Ruiz","horario":"Lunes 18:00","cupos":15}
```

```json
{"nombre":"Prensa de piernas","marca":"Technogym","anioCompra":2019,"enMantenimiento":false}
```

## Endpoints

### Socio

| Método | Ruta | Resultado | Códigos |
| --- | --- | --- | --- |
| GET | `/api/socio` | Listar | 200 |
| GET | `/api/socio/{id}` | Traer uno | 200, 404 |
| POST | `/api/socio` | Crear | 201 |
| PUT | `/api/socio/{id}` | Editar | 200, 404 |
| DELETE | `/api/socio/{id}` | Borrar | 204, 404 |
| GET | `/api/socio/ingresos` | Sumar mensualidades de socios activos | 200 |
| PUT | `/api/socio/{id}/desactivar` | Marcar inactivo | 200, 404, 409 si ya está inactivo |

Los ingresos se devuelven como un número JSON; sin socios activos el resultado es `0.0`.

### ClaseGrupal

| Método | Ruta | Resultado | Códigos |
| --- | --- | --- | --- |
| GET | `/api/clase` | Listar | 200 |
| GET | `/api/clase/{id}` | Traer una | 200, 404 |
| POST | `/api/clase` | Crear | 201 |
| PUT | `/api/clase/{id}` | Editar | 200, 404 |
| DELETE | `/api/clase/{id}` | Borrar | 204, 404 |
| PUT | `/api/clase/{id}/inscribir` | Restar un cupo | 200, 404, 409 sin cupos |
| PUT | `/api/clase/{id}/cancelar-inscripcion` | Sumar un cupo | 200, 404 |
| GET | `/api/clase/con-cupos` | Cupos mayores que cero, orden descendente | 200 |

La cancelación suma un cupo, como pide el PDF. No se almacena un historial de inscripciones ni una capacidad máxima porque esos campos y relaciones no forman parte del ejercicio.

### Maquina

| Método | Ruta | Resultado | Códigos |
| --- | --- | --- | --- |
| GET | `/api/maquina` | Listar | 200 |
| GET | `/api/maquina/{id}` | Traer una | 200, 404 |
| POST | `/api/maquina` | Crear | 201 |
| PUT | `/api/maquina/{id}` | Editar | 200, 404 |
| DELETE | `/api/maquina/{id}` | Borrar | 204, 404 |
| GET | `/api/maquina/operativas` | Fuera de mantenimiento | 200 |
| GET | `/api/maquina/antiguas?anios=5` | Antigüedad estrictamente mayor que 5 | 200 |
| PUT | `/api/maquina/{id}/mantenimiento` | Marcar en mantenimiento | 200, 404, 409 si ya estaba |

El año actual se calcula con `Year.now()`. Una máquina de exactamente cinco años no aparece al consultar `anios=5`. Si omites `anios`, se usa `5`. Un valor no numérico devuelve `400`.

## Probar con Postman

1. Importa `postman/gimnasio.postman_collection.json`.
2. Revisa la variable de colección `baseUrl`, inicialmente `http://localhost:9091`.
3. Con la API iniciada, ejecuta la colección completa con **Collection Runner**, en el orden incluido.
4. Revisa las aserciones de estado HTTP y contenido.

Hay una carpeta por modelo, con una petición por endpoint, casos `404` y conflictos `409`. POST guarda el identificador devuelto para las peticiones siguientes. La colección crea registros de prueba propios y los borra al terminar cada carpeta; no utiliza identificadores fijos de los datos iniciales. Si se interrumpe la ejecución, revisa los registros de prueba que queden.

## Pruebas automatizadas

```powershell
.\mvnw.cmd test
```

`GimnasioApiTests` prueba los 23 endpoints mediante MockMvc y repositorios en memoria, sin conectarse a PostgreSQL. Comprueba CRUD, códigos HTTP, conflictos, ingresos, orden por cupos y el límite de antigüedad. Estos dobles no verifican el SQL ni las consultas derivadas de Spring Data contra una base real.

Resultado durante la preparación: compilación correcta y ocho pruebas de API aprobadas, sin fallos ni errores. La prueba de arranque con base real quedó omitida por no haberse habilitado `RUN_DB_TESTS`.

La prueba de arranque con PostgreSQL se habilita expresamente después de preparar la base:

```powershell
$env:RUN_DB_TESTS='true'
.\mvnw.cmd test
Remove-Item Env:RUN_DB_TESTS
```

## Evidencias de la entrega

**Pendiente de completar en tu equipo:** ejecutar el script y la colección contra PostgreSQL y agregar las capturas solicitadas. La conexión a PostgreSQL quedó bloqueada por los permisos de red de la sesión de preparación. No se incluyen capturas simuladas.

Las instrucciones y nombres sugeridos están en [docs/capturas/README.md](docs/capturas/README.md). Antes de entregar, agrega aquí las tres capturas del gestor y las tres respuestas de Postman usando enlaces de imagen Markdown.

## Subir a GitHub

La raíz del repositorio debe ser esta carpeta, donde está `pom.xml`. Incluye `src`, `.mvn`, los wrappers, `database`, `postman`, `docs` y este README. `.gitignore` excluye `target` y los archivos del IDE.

Antes de entregar, clona el repositorio en otra carpeta, prepara una base vacía, inicia la API y ejecuta la colección completa, como indica el PDF.
