# Capturas pendientes

Estas evidencias deben obtenerse después de ejecutar el proyecto contra PostgreSQL.

1. `01-socios.png`: tabla `socio` con al menos cinco registros en pgAdmin u otro gestor.
2. `02-clases.png`: tabla `clase_grupal` con al menos cinco registros.
3. `03-maquinas.png`: tabla `maquina` con al menos cinco registros.
4. `04-postman-socios.png`: respuesta de `GET /api/socio/ingresos`, con URL, cuerpo y código visibles.
5. `05-postman-clases.png`: respuesta de `GET /api/clase/con-cupos`, con URL, cuerpo y código visibles.
6. `06-postman-maquinas.png`: respuesta de `GET /api/maquina/operativas`, con URL, cuerpo y código visibles.

Consultas para visualizar los datos:

```sql
SELECT * FROM socio ORDER BY id;
SELECT * FROM clase_grupal ORDER BY id;
SELECT * FROM maquina ORDER BY id;
```

Después de guardar las imágenes, agrégalas al README principal con esta sintaxis:

```markdown
![Tabla de socios](docs/capturas/01-socios.png)
```

Repite el enlace para las otras cinco imágenes. La carpeta no contiene evidencias todavía.
