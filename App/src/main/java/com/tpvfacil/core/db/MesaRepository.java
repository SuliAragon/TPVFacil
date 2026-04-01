package com.tpvfacil.core.db;

import com.tpvfacil.hosteleria.modelo.EstadoMesa;
import com.tpvfacil.hosteleria.modelo.Mesa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para la entidad Mesa.
 */
public class MesaRepository {

    private static final Logger log = LoggerFactory.getLogger(MesaRepository.class);

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    public Optional<Mesa> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM mesas WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar mesa por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Devuelve todas las mesas activas ordenadas por zona y nombre. */
    public List<Mesa> findAll() {
        List<Mesa> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM mesas WHERE activa = 1 ORDER BY zona, nombre")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todas las mesas", e);
        }
        return lista;
    }

    public int save(Mesa m) {
        String sql = "INSERT INTO mesas (nombre, capacidad, zona, estado, activa) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, m.getNombre());
            stmt.setInt(2, m.getCapacidad());
            stmt.setString(3, m.getZona());
            stmt.setString(4, m.getEstado().name());
            stmt.setInt(5, m.isActiva() ? 1 : 0);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                m.setId(id);
                return id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar mesa", e);
        }
        return -1;
    }

    public void update(Mesa m) {
        String sql = "UPDATE mesas SET nombre=?, capacidad=?, zona=?, estado=?, activa=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, m.getNombre());
            stmt.setInt(2, m.getCapacidad());
            stmt.setString(3, m.getZona());
            stmt.setString(4, m.getEstado().name());
            stmt.setInt(5, m.isActiva() ? 1 : 0);
            stmt.setInt(6, m.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar mesa", e);
        }
    }

    /** Actualiza únicamente el estado de una mesa. */
    public void actualizarEstado(int mesaId, EstadoMesa estado) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE mesas SET estado = ? WHERE id = ?")) {
            stmt.setString(1, estado.name());
            stmt.setInt(2, mesaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar estado de mesa: {}", mesaId, e);
        }
    }

    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE mesas SET activa = 0 WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar mesa: {}", id, e);
        }
    }

    private Mesa mapear(ResultSet rs) throws SQLException {
        Mesa m = new Mesa();
        m.setId(rs.getInt("id"));
        m.setNombre(rs.getString("nombre"));
        m.setCapacidad(rs.getInt("capacidad"));
        m.setZona(rs.getString("zona"));
        m.setEstado(EstadoMesa.valueOf(rs.getString("estado")));
        m.setActiva(rs.getInt("activa") == 1);
        return m;
    }
}
