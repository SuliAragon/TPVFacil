package com.tpvfacil.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para los registros Verifactu (cadena de hash + estado AEAT).
 */
public class RegistroVerifactuRepository {

    private static final Logger log = LoggerFactory.getLogger(RegistroVerifactuRepository.class);

    public static class RegistroVerifactu {
        public int id;
        public int facturaId;
        public String huella;
        public String huellaAnterior;
        public String firma;
        public String xmlEnviado;
        public String csvAeat;
        public String estado;           // OK, PENDIENTE, ERROR
        public String errorDescripcion;
        public String fechaEnvio;
        public int intentos;
    }

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    public Optional<RegistroVerifactu> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM registros_verifactu WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar registro verifactu por id: {}", id, e);
        }
        return Optional.empty();
    }

    public Optional<RegistroVerifactu> findByFacturaId(int facturaId) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM registros_verifactu WHERE factura_id = ?")) {
            stmt.setInt(1, facturaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar registro verifactu por facturaId", e);
        }
        return Optional.empty();
    }

    /** Devuelve todos los registros con estado PENDIENTE o ERROR (para reenvío). */
    public List<RegistroVerifactu> findPendientes() {
        List<RegistroVerifactu> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM registros_verifactu WHERE estado IN ('PENDIENTE', 'ERROR') ORDER BY id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener registros pendientes", e);
        }
        return lista;
    }

    /** Devuelve el último registro OK (para encadenar el siguiente hash). */
    public Optional<RegistroVerifactu> findUltimo() {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM registros_verifactu WHERE estado = 'OK' ORDER BY id DESC LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener último registro verifactu", e);
        }
        return Optional.empty();
    }

    public List<RegistroVerifactu> findAll() {
        List<RegistroVerifactu> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM registros_verifactu ORDER BY id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todos los registros verifactu", e);
        }
        return lista;
    }

    public int save(RegistroVerifactu r) {
        String sql = """
            INSERT INTO registros_verifactu
              (factura_id, huella, huella_anterior, firma, xml_enviado, csv_aeat, estado, error_descripcion, fecha_envio, intentos)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, r.facturaId);
            stmt.setString(2, r.huella);
            stmt.setString(3, r.huellaAnterior);
            stmt.setString(4, r.firma);
            stmt.setString(5, r.xmlEnviado);
            stmt.setString(6, r.csvAeat);
            stmt.setString(7, r.estado);
            stmt.setString(8, r.errorDescripcion);
            stmt.setString(9, r.fechaEnvio);
            stmt.setInt(10, r.intentos);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                r.id = keys.getInt(1);
                return r.id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar registro verifactu", e);
        }
        return -1;
    }

    public void update(RegistroVerifactu r) {
        String sql = """
            UPDATE registros_verifactu
            SET csv_aeat=?, estado=?, error_descripcion=?, fecha_envio=?, intentos=?
            WHERE id=?
            """;
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, r.csvAeat);
            stmt.setString(2, r.estado);
            stmt.setString(3, r.errorDescripcion);
            stmt.setString(4, r.fechaEnvio);
            stmt.setInt(5, r.intentos);
            stmt.setInt(6, r.id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar registro verifactu", e);
        }
    }

    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM registros_verifactu WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar registro verifactu: {}", id, e);
        }
    }

    private RegistroVerifactu mapear(ResultSet rs) throws SQLException {
        RegistroVerifactu r = new RegistroVerifactu();
        r.id = rs.getInt("id");
        r.facturaId = rs.getInt("factura_id");
        r.huella = rs.getString("huella");
        r.huellaAnterior = rs.getString("huella_anterior");
        r.firma = rs.getString("firma");
        r.xmlEnviado = rs.getString("xml_enviado");
        r.csvAeat = rs.getString("csv_aeat");
        r.estado = rs.getString("estado");
        r.errorDescripcion = rs.getString("error_descripcion");
        r.fechaEnvio = rs.getString("fecha_envio");
        r.intentos = rs.getInt("intentos");
        return r;
    }
}
