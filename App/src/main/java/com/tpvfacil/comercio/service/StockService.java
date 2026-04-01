package com.tpvfacil.comercio.service;

import com.tpvfacil.core.db.DatabaseManager;
import com.tpvfacil.core.db.ProductoRepository;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.core.modelo.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para el control de stock del módulo Comercio.
 * Gestiona el descuento, reposición y ajuste masivo de inventario.
 */
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);
    private final ProductoRepository productoRepository = new ProductoRepository();

    /**
     * Descuenta stock basándose en las líneas de una factura cobrada.
     */
    public void descontarStock(Factura factura) {
        if (factura.getLineas() == null) return;
        for (LineaFactura linea : factura.getLineas()) {
            if (linea.getProductoId() > 0) {
                // Sólo descuenta si el producto tiene control de stock (esto deberia validarlo el repositorio)
                productoRepository.decrementarStock(linea.getProductoId(), (int) linea.getCantidad());
            }
        }
    }

    /**
     * Repone el stock (por ejemplo, tras una devolución o inventario manual).
     */
    public void reponerStock(int productoId, int cantidad) {
        if (cantidad <= 0) return;
        try (Connection conn = DatabaseManager.getInstance().getConexion();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE productos SET stock = stock + ? WHERE id = ? AND stock >= 0")) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al reponer stock del producto {}", productoId, e);
        }
    }

    /**
     * Actualiza el stock de múltiples productos en una única transacción.
     * key: id_producto, value: nuevo_stock
     */
    public void ajustarStock(Map<Integer, Integer> stockPorId) {
        if (stockPorId.isEmpty()) return;
        Connection conn = DatabaseManager.getInstance().getConexion();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE productos SET stock = ? WHERE id = ?")) {
                for (Map.Entry<Integer, Integer> entry : stockPorId.entrySet()) {
                    stmt.setInt(1, entry.getValue());
                    stmt.setInt(2, entry.getKey());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            log.error("Error en transacción de ajuste de stock", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException throwables) {
                log.error("Error al restaurar autocommit", throwables);
            }
        }
    }

    /**
     * Obtiene productos cuyo stock es mayor o igual a 0 pero menor o igual al umbral.
     */
    public List<Producto> getProductosBajoStock(int umbral) {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStock() >= 0 && p.getStock() <= umbral)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene productos con stock exactamente igual a 0.
     */
    public List<Producto> getProductosAgotados() {
        return productoRepository.findAll().stream()
                .filter(p -> p.getStock() == 0)
                .collect(Collectors.toList());
    }
}
