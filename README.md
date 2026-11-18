# Clínica veterinaria · API REST

Proyecto de Back End con Spring Boot correspondiente a la **opción 3: Clínica veterinaria**. Permite administrar mascotas, medicamentos y veterinarios mediante una API conectada a PostgreSQL.

Los tres modelos son independientes: cada uno cuenta con sus propias capas `model`, `repository`, `services` y `controller`, siguiendo la estructura del modelo `Mascota`. No se utilizan relaciones JPA entre entidades.

## Tecnologías

| Tecnología | Uso |
| --- | --- |
| Java 27 | Lenguaje y versión configurada en el proyecto |
| Spring Boot 4.1.2 | Aplicación y API REST |
| Spring Web MVC | Controladores HTTP |
| Spring Data JPA | Persistencia mediante repositorios |
| PostgreSQL | Base de datos |
| Lombok | Generación de getters, setters y otros métodos |
| Maven Wrapper | Ejecución y construcción del proyecto |
| JUnit 5 | Pruebas unitarias |
| Postman | Colección de peticiones y comprobaciones HTTP |

## Organización

| Ruta | Contenido |
| --- | --- |
| `src/main/java/puce/verterinaria/model/` | Entidades `Mascota`, `Medicamento` y `Veterinario` |
| `src/main/java/puce/verterinaria/repository/` | Repositorios JPA y consultas por nombre de método |
| `src/main/java/puce/verterinaria/services/` | CRUD y reglas de negocio |
| `src/main/java/puce/verterinaria/controller/` | Endpoints y respuestas HTTP |
| `src/main/java/puce/verterinaria/VerterinariaApplication.java` | Clase principal |
| `src/main/resources/application.properties` | Puerto y conexión a PostgreSQL |
| `src/test/java/puce/verterinaria/` | Pruebas de servicios y de contexto |
| `database/script.sql` | Creación de base, usuario, tablas y datos de ejemplo |
| `postman/veterinaria.postman_collection.json` | Colección de 43 peticiones |

## Requisitos

- JDK 27 y variable `JAVA_HOME` apuntando al JDK.
- PostgreSQL en ejecución y cliente `psql` disponible.
- Acceso a Internet para descargar Maven y las dependencias en la primera ejecución.
- Postman para ejecutar la colección incluida.

Ejecutar los comandos desde la carpeta que contiene `pom.xml`.

## Preparar PostgreSQL

Para una instalación nueva, ejecutar con una cuenta administradora de PostgreSQL:

```powershell
psql -U postgres -d postgres -f database/script.sql
```

El script utiliza comandos de **psql**, como `\connect` y `\gexec`; no se debe pegar completo en el Query Tool de pgAdmin.

El script prepara la base `veterinaria`, el usuario de aplicación `kevin` y las tablas `mascota`, `medicamento` y `veterinario`. Incluye cinco registros por tabla para una base vacía. Si una tabla ya tiene registros, omite los ejemplos de esa tabla. Si el usuario ya existe, conserva su contraseña.

La cuenta `postgres` se utiliza para preparar la base; la aplicación se conecta como `kevin`.

## Configuración

La configuración local de práctica está en `src/main/resources/application.properties`:

```properties
spring.application.name=verterinaria
spring.jpa.hibernate.ddl-auto=update
server.port=9090
spring.datasource.url=jdbc:postgresql://localhost:5432/veterinaria
spring.datasource.username=kevin
spring.datasource.password=123
```

La contraseña `123` corresponde al ejemplo de práctica del script SQL. Si se cambia, actualizar también la configuración de conexión. El proyecto utiliza PostgreSQL y no incluye dependencias de H2.

## Ejecutar

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

También se puede abrir `pom.xml` como proyecto Maven en IntelliJ IDEA y ejecutar `VerterinariaApplication`.

La URL base configurada es `http://localhost:9090`. Consultas iniciales:

- [Mascotas](http://localhost:9090/api/mascota)
- [Medicamentos](http://localhost:9090/api/medicamento)
- [Veterinarios](http://localhost:9090/api/veterinario)

## Modelos

Cada entidad tiene un `id` entero autogenerado.

| Modelo | Campos |
| --- | --- |
| Mascota | `nombre: String`, `especie: String`, `edad: int`, `dueno: String` |
| Medicamento | `nombre: String`, `precio: double`, `stock: int`, `requiereReceta: boolean` |
| Veterinario | `nombre: String`, `especialidad: String`, `aniosExperiencia: int`, `disponible: boolean` |

## Endpoints CRUD

| Método | Mascota | Medicamento | Veterinario | Respuestas |
| --- | --- | --- | --- | --- |
| GET | `/api/mascota` | `/api/medicamento` | `/api/veterinario` | `200` |
| GET | `/api/mascota/{id}` | `/api/medicamento/{id}` | `/api/veterinario/{id}` | `200`, `404` |
| POST | `/api/mascota` | `/api/medicamento` | `/api/veterinario` | `201` |
| PUT | `/api/mascota/{id}` | `/api/medicamento/{id}` | `/api/veterinario/{id}` | `200`, `404` |
| DELETE | `/api/mascota/{id}` | `/api/medicamento/{id}` | `/api/veterinario/{id}` | `204`, `404` |

`POST` y `PUT` reciben JSON con `Content-Type: application/json`. Para crear registros, enviar los campos del modelo sin `id`.

## Consultas y operaciones especiales

| Método | Endpoint | Comportamiento | Respuestas |
| --- | --- | --- | --- |
| GET | `/api/mascota/especie/{especie}` | Filtra por especie sin distinguir mayúsculas | `200` |
| GET | `/api/mascota/conteo-especies` | Devuelve un objeto JSON con el conteo por especie | `200` |
| GET | `/api/mascota/dueno/{dueno}` | Filtra por dueño sin distinguir mayúsculas | `200` |
| GET | `/api/mascota/cachorros` | Mascotas con edad menor o igual a un año | `200` |
| GET | `/api/medicamento/stock-bajo?minimo=5` | Stock estrictamente menor al mínimo; valor predeterminado: 5 | `200` |
| GET | `/api/medicamento/venta-libre` | Medicamentos que no requieren receta | `200` |
| PUT | `/api/medicamento/{id}/despachar?cantidad=2` | Descuenta unidades del stock | `200`, `400`, `404`, `409` |
| GET | `/api/veterinario/especialidad/{especialidad}` | Veterinarios disponibles de esa especialidad, sin distinguir mayúsculas | `200` |
| GET | `/api/veterinario/mas-experimentado` | Mayor experiencia; en empate, menor ID | `200`, `404` |
| PUT | `/api/veterinario/{id}/disponibilidad?valor=false` | Actualiza la disponibilidad | `200`, `404`, `409` |

### Reglas de negocio

- Despachar una cantidad menor o igual a cero devuelve `400 Bad Request`.
- Despachar más unidades que el stock disponible devuelve `409 Conflict`.
- Despachar un medicamento inexistente con una cantidad válida devuelve `404 Not Found`.
- Solicitar la disponibilidad que el veterinario ya tiene devuelve `409 Conflict`.
- Cambiar la disponibilidad de un veterinario inexistente devuelve `404 Not Found`.
- Consultar el más experimentado cuando no hay veterinarios devuelve `404 Not Found`.

Estas reglas se implementan en los servicios. Los controladores acceden a los servicios, y los servicios a los repositorios.

## Ejemplos de creación

### Mascota

`POST /api/mascota`

```json
{
  "nombre": "Firulais",
  "especie": "perro",
  "edad": 4,
  "dueno": "Ana Pérez"
}
```

### Medicamento

`POST /api/medicamento`

```json
{
  "nombre": "Amoxicilina 250 mg",
  "precio": 8.75,
  "stock": 30,
  "requiereReceta": true
}
```

### Veterinario

`POST /api/veterinario`

```json
{
  "nombre": "Dr. Luis Cevallos",
  "especialidad": "cirugía",
  "aniosExperiencia": 7,
  "disponible": true
}
```

Los filtros ignoran mayúsculas, pero los ejemplos del script usan `cirugia` sin tilde. Para consultar esos datos, usar `/api/veterinario/especialidad/cirugia`.

## Pruebas con Postman

1. Iniciar la API.
2. Importar [la colección](postman/veterinaria.postman_collection.json).
3. Comprobar que la variable `baseUrl` sea `http://localhost:9090`.
4. Ejecutar la colección completa con Collection Runner, respetando el orden de las peticiones.

La colección contiene una carpeta por modelo. Cada carpeta crea un registro temporal, conserva su ID en una variable, ejecuta consultas y operaciones y borra ese registro al final. Incluye comprobaciones de códigos HTTP, stock y disponibilidad, además de casos `404` por modelo y casos `400` y `409` en las operaciones especiales.

Las peticiones de una carpeta dependen de su orden. Una ejecución interrumpida puede dejar un registro temporal. El caso de veterinarios sin registros se comprueba por separado en una base de prueba vacía.

## Pruebas automatizadas

Ejecutar únicamente las pruebas de servicios, sin conexión a la base:

```powershell
.\mvnw.cmd "-Dtest=MedicamentoServiceTests,VeterinarioServiceTests" test
```

Ejecutar las pruebas de servicios y de los tres controladores, sin PostgreSQL:

```powershell
.\mvnw.cmd "-Dtest=ApiControllerTests,MedicamentoServiceTests,VeterinarioServiceTests" test
```

`ApiControllerTests` utiliza Spring MVC y los servicios reales con repositorios dobles en memoria.
Comprueba las rutas HTTP, los cuerpos JSON, el CRUD, los filtros y los errores `400`, `404` y `409`.
Estas pruebas no comprueban las consultas JPA ni la persistencia en PostgreSQL.

Ejecutar todas las pruebas, incluida la carga del contexto con PostgreSQL configurado:

```powershell
.\mvnw.cmd test
```

Para generar el archivo JAR con las pruebas completas:

```powershell
.\mvnw.cmd package
```

### Estado de validación

| Comprobación | Estado |
| --- | --- |
| Compilación del código de aplicación y de pruebas | Correcta |
| Pruebas unitarias de `MedicamentoService` | 4 aprobadas |
| Pruebas unitarias de `VeterinarioService` | 3 aprobadas |
| Pruebas HTTP de los tres controladores con repositorios en memoria | 3 aprobadas |
| Sintaxis del JSON de Postman y XML de `pom.xml` | Validada |
| Ejecución del SQL en una base vacía | Pendiente |
| Ejecución completa de Postman contra PostgreSQL | Pendiente |
| Prueba de carga del contexto con PostgreSQL | Intentada; falló al obtener metadatos JDBC en este entorno |

La compilación y las siete pruebas de servicios se ejecutaron con una copia local de Maven en modo offline. La primera descarga mediante el wrapper estuvo bloqueada por resolución del nombre `repo.maven.apache.org`.

Revisión del 18 de noviembre de 2026: diez pruebas aprobadas, sin fallos ni errores, al ejecutar servicios y controladores.
La comprobación directa con `psql` y la petición a `localhost:9090` fueron rechazadas por permisos de sockets
del entorno de revisión. No se confirmó aquí el arranque completo ni se ejecutó el script SQL.

## Evidencias de la tarea

Pendiente de incorporar las capturas reales solicitadas:

- Tabla `mascota` con datos en el gestor de PostgreSQL.
- Tabla `medicamento` con datos en el gestor de PostgreSQL.
- Tabla `veterinario` con datos en el gestor de PostgreSQL.
- Tres respuestas de Postman, una por modelo.

Guardar las imágenes en `docs/capturas/` y agregarlas a esta sección después de obtenerlas.

## Contenido para GitHub

Usar como raíz del repositorio esta carpeta, que contiene `pom.xml`, `src/`, `database/`, `postman/` y `README.md`. El archivo `.gitignore` incluye `target/` y archivos del IDE.

En la revisión se encontraron archivos de `target/`, `.idea/` y `output/` ya registrados en Git.
Agregar `.gitignore` no retira archivos que Git ya sigue. Antes de publicar, revisar `git ls-files` y
retirar del índice los archivos de compilación, del IDE y las salidas ajenas al proyecto; conservar las copias locales que se necesiten.

Antes de entregar, verificar la ejecución desde un clon nuevo y una base de prueba vacía, y completar las evidencias pendientes.
