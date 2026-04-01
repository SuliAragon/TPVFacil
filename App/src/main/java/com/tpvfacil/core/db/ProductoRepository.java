package com.tpvfacil.core.db;

import com.tpvfacil.core.modelo.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para la entidad Producto.
 */
public class ProductoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductoRepository.class);

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    /** Busca un producto por su ID. */
    public Optional<Producto> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM productos WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar producto por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Busca un producto por su código de barras. */
    public Optional<Producto> findByCodigoBarras(String codigoBarras) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM productos WHERE codigo_barras = ? AND activo = 1")) {
            stmt.setString(1, codigoBarras);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar producto por código de barras", e);
        }
        return Optional.empty();
    }

    /** Devuelve todos los productos activos. */
    public List<Producto> findAll() {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM productos WHERE activo = 1 ORDER BY nombre")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todos los productos", e);
        }
        return lista;
    }

    /** Busca productos cuyo nombre contenga el texto dado (para buscador del TPV). */
    public List<Producto> findByNombre(String texto) {
        List<Producto> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM productos WHERE activo = 1 AND LOWER(nombre) LIKE LOWER(?) ORDER BY nombre")) {
            stmt.setString(1, "%" + texto + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar productos por nombre", e);
        }
        return lista;
    }

    /**
     * Guarda un nuevo producto y devuelve el ID generado.
     */
    public int save(Producto p) {
        String sql = "INSERT INTO productos (nombre, precio, iva_porcentaje, categoria, codigo_barras, stock, activo) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setDouble(3, p.getIvaPorcentaje());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getCodigoBarras());
            stmt.setInt(6, p.getStock());
            stmt.setInt(7, p.isActivo() ? 1 : 0);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                p.setId(id);
                return id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar producto", e);
        }
        return -1;
    }

    /** Actualiza un producto existente. */
    public void update(Producto p) {
        String sql = "UPDATE productos SET nombre=?, precio=?, iva_porcentaje=?, categoria=?, codigo_barras=?, stock=?, activo=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setDouble(2, p.getPrecio());
            stmt.setDouble(3, p.getIvaPorcentaje());
            stmt.setString(4, p.getCategoria());
            stmt.setString(5, p.getCodigoBarras());
            stmt.setInt(6, p.getStock());
            stmt.setInt(7, p.isActivo() ? 1 : 0);
            stmt.setInt(8, p.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar producto", e);
        }
    }

    /** Desactiva un producto (borrado lógico). */
    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE productos SET activo = 0 WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar producto: {}", id, e);
        }
    }

    /** Decrementa el stock de un producto en la cantidad indicada. */
    public void decrementarStock(int productoId, int cantidad) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "UPDATE productos SET stock = stock - ? WHERE id = ? AND stock >= 0")) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, productoId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al decrementar stock del producto: {}", productoId, e);
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecio(rs.getDouble("precio"));
        p.setIvaPorcentaje(rs.getDouble("iva_porcentaje"));
        p.setCategoria(rs.getString("categoria"));
        p.setCodigoBarras(rs.getString("codigo_barras"));
        p.setStock(rs.getInt("stock"));
        p.setActivo(rs.getInt("activo") == 1);
        return p;
    }
}
