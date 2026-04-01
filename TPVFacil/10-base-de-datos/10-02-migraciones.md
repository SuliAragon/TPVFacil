# 10-02 — Migraciones de Base de Datos

#base-de-datos
Relacionado: [[CLAUDE.md]] | [[10-01-esquema-completo]] | [[03-app-core/03-02-base-de-datos-sqlite]]

---

## Estrategia

Versión de BD guardada en `configuracion` con clave `db_version`. Al arrancar, se ejecutan las migraciones pendientes en orden.

```java
// En DatabaseManager.inicializar()
private static final int VERSION_ACTUAL = 1;

private void ejecutarMigraciones() {
    int instalada = Integer.parseInt(
        ConfiguracionRepository.get("db_version", "0")
    );
    if (instalada < 1) migrar1(); // Esquema inicial completo
    // if (instalada < 2) migrar2(); // Para versiones futuras
    ConfiguracionRepository.set("db_version", String.valueOf(VERSION_ACTUAL));
}
```

## Reglas

1. **Solo aditivas:** `ADD COLUMN`, `CREATE TABLE`, `CREATE INDEX`
2. **Nunca** `DROP` ni `ALTER COLUMN` (perderías datos)
3. **Siempre** `IF NOT EXISTS`
4. Si falla: lanzar `RuntimeException` (no continuar con BD corrupta)

## Migración 1 (v1.0)

Crea todas las tablas de [[10-01-esquema-completo]] + valores iniciales en `configuracion`:
```sql
INSERT OR IGNORE INTO configuracion (clave, valor) VALUES
  ('db_version', '1'),
  ('ancho_papel_mm', '80'),
  ('verifactu_sandbox', 'false'),
  ('web_url_compra', 'https://tpvfacil.es/precios');
```
