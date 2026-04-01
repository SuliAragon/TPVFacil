package com.tpvfacil.core.db;

import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.LineaComanda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para Comanda y LineaComanda.
 */
public class ComandaRepository {

    private static final Logger log = LoggerFactory.getLogger(ComandaRepository.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    public Optional<Comanda> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM comandas WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Comanda c = mapear(rs);
                c.setLineas(findLineasByComandaId(id));
                return Optional.of(c);
            }
        } catch (SQLException e) {
            log.error("Error al buscar comanda por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Devuelve la comanda abierta de una mesa (si existe). */
    public Optional<Comanda> findAbiertaByMesaId(int mesaId) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM comandas WHERE mesa_id = ? AND estado = 'ABIERTA' LIMIT 1")) {
            stmt.setInt(1, mesaId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Comanda c = mapear(rs);
                c.setLineas(findLineasByComandaId(c.getId()));
                return Optional.of(c);
            }
        } catch (SQLException e) {
            log.error("Error al buscar comanda abierta para mesa: {}", mesaId, e);
        }
        return Optional.empty();
    }

    public List<Comanda> findAll() {
        List<Comanda> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM comandas ORDER BY fecha_apertura DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todas las comandas", e);
        }
        return lista;
    }

    public int save(Comanda c) {
        String sql = "INSERT INTO comandas (mesa_id, fecha_apertura, num_comensales, estado, observaciones) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, c.getMesaId());
            stmt.setString(2, c.getFechaApertura().format(FMT));
            stmt.setInt(3, c.getNumComensales());
            stmt.setString(4, c.getEstado().name());
            stmt.setString(5, c.getObservaciones());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                c.setId(id);
                return id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar comanda", e);
        }
        return -1;
    }

    public void update(Comanda c) {
        String sql = "UPDATE comandas SET estado=?, fecha_cierre=?, factura_id=?, observaciones=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, c.getEstado().name());
            stmt.setString(2, c.getFechaCierre() != null ? c.getFechaCierre().format(FMT) : null);
            stmt.setInt(3, c.getFacturaId());
            stmt.setString(4, c.getObservaciones());
            stmt.setInt(5, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar comanda", e);
        }
    }

    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM comandas WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar comanda: {}", id, e);
        }
    }

    /** Añade una línea a una comanda existente. */
    public int saveLinea(LineaComanda l) {
        String sql = "INSERT INTO lineas_comanda (comanda_id, producto_id, cantidad, precio_unitario, enviado_cocina, estado, observaciones) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, l.getComandaId());
            stmt.setInt(2, l.getProductoId());
            stmt.setDouble(3, l.getCantidad());
            stmt.setDouble(4, l.getPrecioUnitario());
            stmt.setInt(5, l.isEnviadoCocina() ? 1 : 0);
            stmt.setString(6, l.getEstado().name());
            stmt.setString(7, l.getObservaciones());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                l.setId(id);
                return id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar línea de comanda", e);
        }
        return -1;
    }

    /** Actualiza el estado de una línea de comanda. */
    public void actualizarEstadoLinea(int lineaId, LineaComanda.Estado estado, boolean enviadoCocina) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE lineas_comanda SET estado=?, enviado_cocina=? WHERE id=?")) {
            stmt.setString(1, estado.name());
            stmt.setInt(2, enviadoCocina ? 1 : 0);
            stmt.setInt(3, lineaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar estado de línea: {}", lineaId, e);
        }
    }

    public List<LineaComanda> findLineasByComandaId(int comandaId) {
        List<LineaComanda> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT lc.*, p.nombre as nombre_producto FROM lineas_comanda lc " +
                "LEFT JOIN productos p ON lc.producto_id = p.id " +
                "WHERE lc.comanda_id = ?")) {
            stmt.setInt(1, comandaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapearLinea(rs));
        } catch (SQLException e) {
            log.error("Error al obtener líneas de comanda: {}", comandaId, e);
        }
        return lista;
    }

    private Comanda mapear(ResultSet rs) throws SQLException {
        Comanda c = new Comanda();
        c.setId(rs.getInt("id"));
        c.setMesaId(rs.getInt("mesa_id"));
        String fechaApertura = rs.getString("fecha_apertura");
        if (fechaApertura != null) c.setFechaApertura(LocalDateTime.parse(fechaApertura, FMT));
        String fechaCierre = rs.getString("fecha_cierre");
        if (fechaCierre != null) c.setFechaCierre(LocalDateTime.parse(fechaCierre, FMT));
        c.setNumComensales(rs.getInt("num_comensales"));
        c.setEstado(Comanda.Estado.valueOf(rs.getString("estado")));
        c.setFacturaId(rs.getInt("factura_id"));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }

    private LineaComanda mapearLinea(ResultSet rs) throws SQLException {
        LineaComanda l = new LineaComanda();
        l.setId(rs.getInt("id"));
        l.setComandaId(rs.getInt("comanda_id"));
        l.setProductoId(rs.getInt("producto_id"));
        l.setNombreProducto(rs.getString("nombre_producto"));
        l.setCantidad(rs.getDouble("cantidad"));
        l.setPrecioUnitario(rs.getDouble("precio_unitario"));
        l.setEnviadoCocina(rs.getInt("enviado_cocina") == 1);
        l.setEstado(LineaComanda.Estado.valueOf(rs.getString("estado")));
        l.setObservaciones(rs.getString("observaciones"));
        return l;
    }
}
