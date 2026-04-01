package com.tpvfacil.core.util;

import com.tpvfacil.core.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BackupManager {

    private static final Logger log = LoggerFactory.getLogger(BackupManager.class);
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * Devuelve la ruta donde se alojan los backups automáticos
     */
    private File getBackupFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String ruta;
        if (os.contains("win")) {
            ruta = System.getenv("LOCALAPPDATA") + File.separator + "TPVFacil" + File.separator + "backups";
        } else if (os.contains("mac")) {
            ruta = System.getProperty("user.home") + "/Library/Application Support/TPVFacil/backups";
        } else {
            ruta = System.getProperty("user.home") + "/.tpvfacil/backups";
        }
        
        File dir = new File(ruta);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
    
    private File getControlFile() {
        String os = System.getProperty("os.name").toLowerCase();
        String ruta;
        if (os.contains("win")) {
            ruta = System.getenv("LOCALAPPDATA") + File.separator + "TPVFacil" + File.separator + "tpvfacil.db";
        } else if (os.contains("mac")) {
            ruta = System.getProperty("user.home") + "/Library/Application Support/TPVFacil/tpvfacil.db";
        } else {
            ruta = System.getProperty("user.home") + "/.tpvfacil/tpvfacil.db";
        }
        return new File(ruta);
    }

    /**
     * Crea un backup diario y limpia backups antiguos (> 7 días)
     */
    public void realizarBackupDiario() {
        try {
            File source = getControlFile();
            if (!source.exists()) return;

            String fechaActual = LocalDate.now().format(FMT_FECHA);
            File backupDir = getBackupFolder();
            File dest = new File(backupDir, "tpvfacil_" + fechaActual + ".db");
            
            DatabaseManager.getInstance().cerrar();
            
            // Esperar un momento a que sqlite libere locks (idealmente la app estaría bloqueada para uso)
            Thread.sleep(500);
            
            Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("Backup diario creado exitosamente en: " + dest.getAbsolutePath());
            
            // Reconectar DB
            DatabaseManager.getInstance().inicializar();
            
            limpiarAntiguos();
            
        } catch (Exception e) {
            log.error("Fallo durante el backup automático", e);
            // Intentar reabrir en caso de fallo
            try {
                DatabaseManager.getInstance().inicializar();
            } catch (Exception ignored) {}
        }
    }
    
    private void limpiarAntiguos() {
        File dir = getBackupFolder();
        File[] files = dir.listFiles((d, name) -> name.startsWith("tpvfacil_") && name.endsWith(".db"));
        if (files == null) return;
        
        long hace7dias = System.currentTimeMillis() - (7L * 24 * 3600 * 1000);
        
        for (File f : files) {
            if (f.lastModified() < hace7dias) {
                if(f.delete()) {
                    log.info("Backup antiguo eliminado: " + f.getName());
                }
            }
        }
    }

    public void exportarBackup(Path destino) throws IOException {
        DatabaseManager.getInstance().cerrar();
        try {
            Thread.sleep(500);
            Files.copy(getControlFile().toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            DatabaseManager.getInstance().inicializar();
        }
    }

    public boolean restaurarBackup(Path origen) {
        try {
            // Validar SQLite minimamente (Los primeros 16 bytes de un DB SQLite 3 dicen "SQLite format 3\000")
            // Asumimos que la seleccion del usuario con extension .db estara bien por ahora
            
            DatabaseManager.getInstance().cerrar();
            Thread.sleep(1000);
            
            Files.copy(origen, getControlFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            DatabaseManager.getInstance().inicializar();
            return true;
        } catch (Exception e) {
            log.error("Error al restaurar base de datos", e);
            DatabaseManager.getInstance().inicializar();
            return false;
        }
    }

    public List<BackupInfo> listarBackupsDisponibles() {
        File dir = getBackupFolder();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".db"));
        if (files == null) return new ArrayList<>();
        
        return Arrays.stream(files)
                .map(f -> new BackupInfo(f.getName(), f.length(), f.getAbsolutePath()))
                .collect(Collectors.toList());
    }

    public static class BackupInfo {
        private String fileName;
        private long sizeBytes;
        private String fullPath;

        public BackupInfo(String fileName, long sizeBytes, String fullPath) {
            this.fileName = fileName;
            this.sizeBytes = sizeBytes;
            this.fullPath = fullPath;
        }

        public String getFileName() { return fileName; }
        public long getSizeBytes() { return sizeBytes; }
        public String getFullPath() { return fullPath; }
        public String getFormattedSize() {
            return String.format("%.2f MB", sizeBytes / 1024.0 / 1024.0);
        }
    }
}
