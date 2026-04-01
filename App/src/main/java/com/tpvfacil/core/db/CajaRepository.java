package com.tpvfacil.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

/**
 * Repositorio para la tabla caja (apertura/cierre del turno).
 */
public class CajaRepository {

    private static final Logger log = LoggerFactory.getLogger(CajaRepository.class);

    public static class Caja {
        public int id;
        public String fechaApertura;
        public String fechaCierre;
        public double efectivoInicial;
        public double totalEfectivo;
        public double totalTarjeta;
        public int totalVentas;
        public String estado;   // ABIERTA, CERRADA
    }

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    public Optional<Caja> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM caja WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar caja por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Devuelve la caja actualmente abierta (si existe). */
    public Optional<Caja> findAbierta() {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM caja WHERE estado = 'ABIERTA' LIMIT 1")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar caja abierta", e);
        }
        return Optional.empty();
    }

    public int save(Caja c) {
        String sql = "INSERT INTO caja (fecha_apertura, efectivo_inicial, estado) VALUES (?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.fechaApertura);
            stmt.setDouble(2, c.efectivoInicial);
            stmt.setString(3, "ABIERTA");
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                c.id = keys.getInt(1);
                return c.id;
            }
        } catch (SQLException e) {
            log.error("Error al abrir caja", e);
        }
        return -1;
    }

    public void update(Caja c) {
        String sql = "UPDATE caja SET fecha_cierre=?, total_efectivo=?, total_tarjeta=?, total_ventas=?, estado=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, c.fechaCierre);
            stmt.setDouble(2, c.totalEfectivo);
            stmt.setDouble(3, c.totalTarjeta);
            stmt.setInt(4, c.totalVentas);
            stmt.setString(5, c.estado);
            stmt.setInt(6, c.id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar caja", e);
        }
    }

    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM caja WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar caja: {}", id, e);
        }
    }

    private Caja mapear(ResultSet rs) throws SQLException {
        Caja c = new Caja();
        c.id = rs.getInt("id");
        c.fechaApertura = rs.getString("fecha_apertura");
        c.fechaCierre = rs.getString("fecha_cierre");
        c.efectivoInicial = rs.getDouble("efectivo_inicial");
        c.totalEfectivo = rs.getDouble("total_efectivo");
        c.totalTarjeta = rs.getDouble("total_tarjeta");
        c.totalVentas = rs.getInt("total_ventas");
        c.estado = rs.getString("estado");
        return c;
    }
}
