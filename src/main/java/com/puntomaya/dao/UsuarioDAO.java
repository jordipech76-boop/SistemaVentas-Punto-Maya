package com.puntomaya.dao;

import com.puntomaya.model.RolUsuario;
import com.puntomaya.model.Usuario;
import com.puntomaya.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla USUARIO.
 */
public class UsuarioDAO {

    public List<Usuario> listar() {
        String sql = "SELECT * FROM usuario ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar usuarios", e);
        }
        return usuarios;
    }

    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por id", e);
        }
        return Optional.empty();
    }

    /**
     * Busca un usuario por su nombre de usuario y contraseña (login).
     * Solo regresa usuarios activos.
     */
    public Optional<Usuario> autenticar(String nombreUsuario, String contrasena) {
        String sql = "SELECT * FROM usuario WHERE nombre_usuario = ? AND contrasena = ? AND activo = TRUE";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, contrasena);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al autenticar usuario", e);
        }
        return Optional.empty();
    }

    public void guardar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, nombre_usuario, contrasena, rol, activo) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getNombreUsuario());
            ps.setString(3, usuario.getContrasena());
            ps.setString(4, usuario.getRol().name());
            ps.setBoolean(5, usuario.isActivo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario", e);
        }
    }

    public void actualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, nombre_usuario = ?, contrasena = ?, "
                + "rol = ?, activo = ? WHERE id_usuario = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getNombreUsuario());
            ps.setString(3, usuario.getContrasena());
            ps.setString(4, usuario.getRol().name());
            ps.setBoolean(5, usuario.isActivo());
            ps.setInt(6, usuario.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection con = Conexion.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(RolUsuario.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}
