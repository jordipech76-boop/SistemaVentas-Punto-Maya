package com.puntomaya.service;

import com.puntomaya.dao.UsuarioDAO;
import com.puntomaya.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio relacionadas a usuarios: login y administración de cuentas.
 * Usada por LoginController para validar el acceso al sistema.
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Intenta iniciar sesión. Regresa el usuario si las credenciales son correctas.
     */
    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new IllegalArgumentException("Debes escribir tu usuario");
        }
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("Debes escribir tu contraseña");
        }
        return usuarioDAO.autenticar(nombreUsuario, contrasena);
    }

    public List<Usuario> listar() {
        return usuarioDAO.listar();
    }

    public void guardar(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().length() < 4) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 4 caracteres");
        }
        usuarioDAO.guardar(usuario);
    }

    public void actualizar(Usuario usuario) {
        usuarioDAO.actualizar(usuario);
    }

    public void eliminar(int id) {
        usuarioDAO.eliminar(id);
    }
}
