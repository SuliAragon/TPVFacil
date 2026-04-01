package com.tpvfacil.core.db;

import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.FormaPago;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.core.modelo.TipoNegocio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para Factura y LineaFactura.
 */
public class FacturaRepository {

    private static final Logger log = LoggerFactory.getLogger(FacturaRepository.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    /** Busca una factura por su ID (incluye sus líneas). */
    public Optional<Factura> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM facturas WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Factura f = mapear(rs);
                f.setLineas(findLineasByFacturaId(id));
                return Optional.of(f);
            }
        } catch (SQLException e) {
            log.error("Error al buscar factura por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Devuelve todas las facturas (sin líneas para eficiencia). */
    public List<Factura> findAll() {
        List<Factura> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM facturas ORDER BY fecha DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todas las facturas", e);
        }
        return lista;
    }

    /**
     * Cuenta las facturas emitidas hoy (no anuladas).
     * Usado por ModoDemo para verificar el límite diario.
     */
    public int contarPorFecha(LocalDate fecha) {
        String fechaStr = fecha.toString();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT COUNT(*) FROM facturas WHERE DATE(fecha) = ? AND anulada = 0")) {
            stmt.setString(1, fechaStr);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error al contar facturas por fecha", e);
        }
        return 0;
    }

    /** Obtiene el siguiente número de factura para una serie. */
    public int siguienteNumero(String serie) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT COALESCE(MAX(numero), 0) + 1 FROM facturas WHERE serie = ?")) {
            stmt.setString(1, serie);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("Error al calcular siguiente número de factura", e);
        }
        return 1;
    }

    /**
     * Guarda una factura y todas sus líneas en una transacción.
     * Devuelve el ID generado.
     */
    public int save(Factura f) {
        String sqlFactura = """
            INSERT INTO facturas (serie, numero, fecha, cliente_id, base_imponible,
                cuota_iva, total, forma_pago, efectivo_entregado, cambio, tipo_negocio, anulada)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try {
            conexion().setAutoCommit(false);

            try (PreparedStatement stmt = conexion().prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, f.getSerie());
                stmt.setInt(2, f.getNumero());
                stmt.setString(3, f.getFecha().format(FMT));
                stmt.setInt(4, f.getClienteId());
                stmt.setDouble(5, f.getBaseImponible());
                stmt.setDouble(6, f.getCuotaIva());
                stmt.setDouble(7, f.getTotal());
                stmt.setString(8, f.getFormaPago().name());
                stmt.setDouble(9, f.getEfectivoEntregado());
                stmt.setDouble(10, f.getCambio());
                stmt.setString(11, f.getTipoNegocio().name());
                stmt.setInt(12, f.isAnulada() ? 1 : 0);
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int id = keys.getInt(1);
                    f.setId(id);
                    saveLineas(f.getLineas(), id);
                    conexion().commit();
                    return id;
                }
            }
            conexion().rollback();
        } catch (SQLException e) {
            try { conexion().rollback(); } catch (SQLException ignored) {}
            log.error("Error al guardar factura", e);
        } finally {
            try { conexion().setAutoCommit(true); } catch (SQLException ignored) {}
        }
        return -1;
    }

    /** Marca una factura como anulada. */
    public void anular(int facturaId) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE facturas SET anulada = 1 WHERE id = ?")) {
            stmt.setInt(1, facturaId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al anular factura: {}", facturaId, e);
        }
    }

    /** Elimina una factura (uso interno, normalmente se anula). */
    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM facturas WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar factura: {}", id, e);
        }
    }

    /** Actualiza una factura existente (sin tocar sus líneas). */
    public void update(Factura f) {
        String sql = "UPDATE facturas SET anulada=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setInt(1, f.isAnulada() ? 1 : 0);
            stmt.setInt(2, f.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar factura", e);
        }
    }

    // --- Líneas de factura ---

    private void saveLineas(List<LineaFactura> lineas, int facturaId) throws SQLException {
        String sql = "INSERT INTO lineas_factura (factura_id, producto_id, descripcion, cantidad, precio_unitario, iva_porcentaje, subtotal) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (LineaFactura l : lineas) {
                stmt.setInt(1, facturaId);
                stmt.setInt(2, l.getProductoId());
                stmt.setString(3, l.getDescripcion());
                stmt.setDouble(4, l.getCantidad());
                stmt.setDouble(5, l.getPrecioUnitario());
                stmt.setDouble(6, l.getIvaPorcentaje());
                stmt.setDouble(7, l.getSubtotal());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) l.setId(keys.getInt(1));
                l.setFacturaId(facturaId);
            }
        }
    }

    private List<LineaFactura> findLineasByFacturaId(int facturaId) throws SQLException {
        List<LineaFactura> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM lineas_factura WHERE factura_id = ?")) {
            stmt.setInt(1, facturaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapearLinea(rs));
        }
        return lista;
    }

    private Factura mapear(ResultSet rs) throws SQLException {
        Factura f = new Factura();
        f.setId(rs.getInt("id"));
        f.setSerie(rs.getString("serie"));
        f.setNumero(rs.getInt("numero"));
        f.setFecha(LocalDateTime.parse(rs.getString("fecha"), FMT));
        f.setClienteId(rs.getInt("cliente_id"));
        f.setBaseImponible(rs.getDouble("base_imponible"));
        f.setCuotaIva(rs.getDouble("cuota_iva"));
        f.setTotal(rs.getDouble("total"));
        f.setFormaPago(FormaPago.valueOf(rs.getString("forma_pago")));
        f.setEfectivoEntregado(rs.getDouble("efectivo_entregado"));
        f.setCambio(rs.getDouble("cambio"));
        f.setTipoNegocio(TipoNegocio.valueOf(rs.getString("tipo_negocio")));
        f.setAnulada(rs.getInt("anulada") == 1);
        return f;
    }

    private LineaFactura mapearLinea(ResultSet rs) throws SQLException {
        LineaFactura l = new LineaFactura();
        l.setId(rs.getInt("id"));
        l.setFacturaId(rs.getInt("factura_id"));
        l.setProductoId(rs.getInt("producto_id"));
        l.setDescripcion(rs.getString("descripcion"));
        l.setCantidad(rs.getDouble("cantidad"));
        l.setPrecioUnitario(rs.getDouble("precio_unitario"));
        l.setIvaPorcentaje(rs.getDouble("iva_porcentaje"));
        l.setSubtotal(rs.getDouble("subtotal"));
        return l;
    }
}
