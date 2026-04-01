package com.tpvfacil.core.db;

import com.tpvfacil.core.modelo.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio CRUD para la entidad Cliente.
 */
public class ClienteRepository {

    private static final Logger log = LoggerFactory.getLogger(ClienteRepository.class);

    private Connection conexion() {
        return DatabaseManager.getInstance().getConexion();
    }

    /** Busca un cliente por su ID. */
    public Optional<Cliente> findById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM clientes WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar cliente por id: {}", id, e);
        }
        return Optional.empty();
    }

    /** Busca un cliente por su NIF. */
    public Optional<Cliente> findByNif(String nif) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM clientes WHERE nif = ?")) {
            stmt.setString(1, nif);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al buscar cliente por NIF", e);
        }
        return Optional.empty();
    }

    /** Devuelve todos los clientes. */
    public List<Cliente> findAll() {
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement stmt = conexion().prepareStatement(
                "SELECT * FROM clientes ORDER BY nombre")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            log.error("Error al obtener todos los clientes", e);
        }
        return lista;
    }

    /**
     * Guarda un nuevo cliente y devuelve el ID generado.
     */
    public int save(Cliente c) {
        String sql = "INSERT INTO clientes (nombre, nif, direccion, codigo_postal, ciudad, telefono, email) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getNif());
            stmt.setString(3, c.getDireccion());
            stmt.setString(4, c.getCodigoPostal());
            stmt.setString(5, c.getCiudad());
            stmt.setString(6, c.getTelefono());
            stmt.setString(7, c.getEmail());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                c.setId(id);
                return id;
            }
        } catch (SQLException e) {
            log.error("Error al guardar cliente", e);
        }
        return -1;
    }

    /** Actualiza un cliente existente. */
    public void update(Cliente c) {
        String sql = "UPDATE clientes SET nombre=?, nif=?, direccion=?, codigo_postal=?, ciudad=?, telefono=?, email=? WHERE id=?";
        try (PreparedStatement stmt = conexion().prepareStatement(sql)) {
            stmt.setString(1, c.getNombre());
            stmt.setString(2, c.getNif());
            stmt.setString(3, c.getDireccion());
            stmt.setString(4, c.getCodigoPostal());
            stmt.setString(5, c.getCiudad());
            stmt.setString(6, c.getTelefono());
            stmt.setString(7, c.getEmail());
            stmt.setInt(8, c.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al actualizar cliente", e);
        }
    }

    /** Elimina un cliente por ID. */
    public void deleteById(int id) {
        try (PreparedStatement stmt = conexion().prepareStatement(
                "DELETE FROM clientes WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error al eliminar cliente: {}", id, e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNombre(rs.getString("nombre"));
        c.setNif(rs.getString("nif"));
        c.setDireccion(rs.getString("direccion"));
        c.setCodigoPostal(rs.getString("codigo_postal"));
        c.setCiudad(rs.getString("ciudad"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        return c;
    }
}
