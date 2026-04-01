# 03-02 — Base de Datos SQLite

#app-core #fase-1
Relacionado: [[CLAUDE.md]] | [[10-base-de-datos/10-01-esquema-completo]] | [[10-base-de-datos/10-02-migraciones]]
Estado: #pendiente

---

## DatabaseManager.java

Singleton thread-safe. Único punto de acceso a la BD.

```java
// Ruta de la BD:
Windows: %LOCALAPPDATA%\TPVFacil\tpvfacil.db
Mac:     ~/Library/Application Support/TPVFacil/tpvfacil.db
Linux:   ~/.tpvfacil/tpvfacil.db

// Al inicializar:
PRAGMA journal_mode=WAL;    // Mejor rendimiento concurrente
PRAGMA foreign_keys=ON;     // Integridad referencial
```

## Repositories

Cada entidad tiene su Repository con CRUD básico:

| Repository | Entidades | Doc |
|-----------|-----------|-----|
| ProductoRepository | Producto | [[03-03-modelos-datos]] |
| ClienteRepository | Cliente | [[03-03-modelos-datos]] |
| FacturaRepository | Factura, LineaFactura | [[03-03-modelos-datos]] |
| RegistroVerifactuRepository | RegistroFactura | [[02-verifactu/02-03-clases-java]] |
| MesaRepository | Mesa | [[04-tpv-hosteleria/04-02-gestion-mesas]] |
| ComandaRepository | Comanda, LineaComanda | [[04-tpv-hosteleria/04-03-comandas]] |
| ConfiguracionRepository | clave-valor | [[03-05-configuracion-negocio]] |
| CajaRepository | Caja | [[03-06-cierre-caja]] |

## Esquema completo → [[10-base-de-datos/10-01-esquema-completo]]

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[10-base-de-datos/10-01-esquema-completo]] y este documento.

Genera DatabaseManager.java como singleton con:
- Detección automática de OS para la ruta
- Creación del directorio AppData si no existe
- PRAGMA WAL y foreign_keys
- Ejecución de migraciones → [[10-base-de-datos/10-02-migraciones]]
- Método ejecutarSQL(String sql) para migraciones

Luego genera TODOS los Repositories con métodos:
- findById(int id)
- findAll()
- save(T entity): inserta y devuelve el id generado
- update(T entity)
- deleteById(int id)
- Métodos específicos donde aplique (ej: FacturaRepository.contarPorFecha)
```
