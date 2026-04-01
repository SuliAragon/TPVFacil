package com.tpvfacil.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio para la tabla configuracion (clave-valor).
 */
public class ConfiguracionRepository {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionRepository.class);

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    /**
     * Obtiene el valor de una clave de configuración.
     * Devuelve el valor por defecto si la clave no existe.
     */
    public String get(String clave, String valorPorDefecto) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT valor FROM configuracion WHERE clave = ?")) {
            stmt.setString(1, clave);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("valor");
            }
        } catch (SQLException e) {
            log.error("Error al leer configuración: {}", clave, e);
        }
        return valorPorDefecto;
    }

    /** Obtiene el valor de una clave (null si no existe). */
    public String get(String clave) {
        return get(clave, null);
    }

    /** Guarda o actualiza el valor de una clave de configuración. */
    public void set(String clave, String valor) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)")) {
            stmt.setString(1, clave);
            stmt.setString(2, valor);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al guardar configuración: {} = {}", clave, valor, e);
        }
    }

    /** Devuelve todas las claves y valores de configuración. */
    public Map<String, String> getAll() {
        Map<String, String> config = new HashMap<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT clave, valor FROM configuracion")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                config.put(rs.getString("clave"), rs.getString("valor"));
            }
        } catch (SQLException e) {
            log.error("Error al leer toda la configuración", e);
        }
        return config;
    }

    /** Elimina una clave de configuración. */
    public void delete(String clave) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM configuracion WHERE clave = ?")) {
            stmt.setString(1, clave);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar configuración: {}", clave, e);
        }
    }
}
