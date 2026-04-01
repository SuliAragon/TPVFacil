# 10-03 — Backup

#base-de-datos
Relacionado: [[CLAUDE.md]] | [[03-app-core/03-06-cierre-caja]] | [[10-01-esquema-completo]]

---

## Backup Automático

Se realiza al hacer el [[03-app-core/03-06-cierre-caja|cierre de caja]] diario.

```
Ruta: %LOCALAPPDATA%\TPVFacil\backups\tpvfacil_YYYYMMDD.db
Retención: últimos 7 días (los más antiguos se eliminan automáticamente)
```

## Backup Manual

Menú → Configuración → Backup → "Exportar ahora"
FileChooser para elegir dónde guardar el `.db`.

## Restauración

Menú → Configuración → Backup → "Restaurar"
1. Seleccionar archivo `.db`
2. Confirmar (⚠ reemplaza TODOS los datos actuales)
3. Cerrar conexión → copiar → reabrir → ejecutar migraciones

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera BackupManager.java en com.tpvfacil.core.util:
- realizarBackupDiario(): copia BD con fecha, limpia backups > 7 días
- exportarBackup(Path destino)
- restaurarBackup(Path origen): valida que es SQLite, reemplaza, reconecta
- listarBackupsDisponibles(): lista con fecha y tamaño

Genera pantalla-backup.fxml + BackupController.java:
- Lista de backups automáticos con botón "Restaurar" por cada uno
- Botón "Exportar ahora" con FileChooser
- Botón "Restaurar desde archivo" con diálogo de confirmación

Integrar realizarBackupDiario() en CierreCajaController.
```
