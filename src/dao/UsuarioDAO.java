/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import modelo.Rol;
import modelo.Trabajador;
import java.util.ArrayList;
import java.sql.Statement;

/**
 *
 * @author USUARIO
 */
public class UsuarioDAO {

    public String login(String usuario, String contrasena) {
        
        String sql = "SELECT R.Nombre_rol FROM USUARIOS U "
                + "INNER JOIN ROLES R ON U.Id_rol = R.Id_rol "
                + "WHERE LTRIM(RTRIM(U.Nombre_usuario)) = LTRIM(RTRIM(?)) "
                + "AND LTRIM(RTRIM(U.Contrasena)) = LTRIM(RTRIM(?)) "
                + "AND U.Estado = 1";

        Connection con = ConexioDB.getConexion();
        if (con == null) {
            System.out.println("❌ No se pudo establecer la conexión a la base de datos.");
            return null;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Nombre_rol"); // Login Exitoso
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en login: " + e.getMessage());
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println("Error al cerrar conexión: " + ex.getMessage());
            }
        }
        return null; // Credenciales incorrectas o error
    }

    // 2. Registrar un nuevo usuario asociado a un trabajador
    public boolean registrarUsuario(int idTrabajador, String nombreUsuario, String contrasena, int idRol) {
        String sql = "INSERT INTO USUARIOS (Id_trabajador, Nombre_usuario, Contrasena, Id_rol, Estado) "
                + "VALUES (?, ?, ?, ?, 1)";

        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTrabajador);
            ps.setString(2, nombreUsuario);
            ps.setString(3, contrasena);
            ps.setInt(4, idRol);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // 3. Obtener trabajadores que NO tienen una cuenta de usuario asignada aún
    public ArrayList<Trabajador> obtenerTrabajadoresDisponibles() {
        ArrayList<Trabajador> lista = new ArrayList<>();
        String sql = "SELECT t.id_trabajador, t.NOMBRES, t.AP_PATERNO, t.AP_MATERNO " +
                     "FROM TRABAJADORES t " +
                     "LEFT JOIN USUARIOS u ON t.id_trabajador = u.id_trabajador " +
                     "WHERE u.id_trabajador IS NULL " +
                     "ORDER BY t.AP_PATERNO, t.NOMBRES";

        try (Connection con = ConexioDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Trabajador t = new Trabajador();
                // Mapeas los datos que vienen de la consulta a tu objeto Modelo
                t.setIdTrabajador(rs.getInt("id_trabajador"));
                t.setNombres(rs.getString("NOMBRES"));
                t.setApPaterno(rs.getString("AP_PATERNO"));
                t.setApMaterno(rs.getString("AP_MATERNO"));
                
                lista.add(t);
            }
            
            // Línea de depuración para tu consola de NetBeans
            System.out.println("DEBUG: Trabajadores disponibles cargados: " + lista.size());

        } catch (SQLException e) {
            System.out.println("Error en obtenerTrabajadoresDisponibles: " + e.getMessage());
        }
        
        return lista;
    }

    // 4. Obtener la lista de Roles para el ComboBox
    public ArrayList<Rol> obtenerRoles() {
        ArrayList<Rol> lista = new ArrayList<>();
        String sql = "SELECT Id_rol, Nombre_rol FROM ROLES";

        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Rol(rs.getInt("Id_rol"), rs.getString("Nombre_rol")));
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener roles: " + e.getMessage());
        }
        return lista;
    }
    
    //mostrar usuarios en tabla
    public ArrayList<Object[]> listarUsuariosParaTabla() {
        ArrayList<Object[]> lista = new ArrayList<>();
        String sql = "SELECT u.Id_usuario, "
                   + "(t.NOMBRES + ' ' + t.AP_PATERNO + ' ' + t.AP_MATERNO) AS Trabajador, "
                   + "u.Nombre_usuario, r.Nombre_rol, "
                   + "CASE WHEN u.Estado = 1 THEN 'Activo' ELSE 'Inactivo' END AS Estado "
                   + "FROM USUARIOS u "
                   + "INNER JOIN TRABAJADORES t ON u.Id_trabajador = t.Id_trabajador "
                   + "INNER JOIN ROLES r ON u.Id_rol = r.Id_rol "
                   + "ORDER BY u.Id_usuario DESC"; // Los últimos creados aparecen primero

        try (Connection con = ConexioDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] fila = new Object[5];
                fila[0] = rs.getInt("Id_usuario");
                fila[1] = rs.getString("Trabajador");
                fila[2] = rs.getString("Nombre_usuario");
                fila[3] = rs.getString("Nombre_rol");
                fila[4] = rs.getString("Estado");
                
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error en listarUsuariosParaTabla: " + e.getMessage());
        }
        return lista;
    }
    
    // Eliminar un usuario por su ID
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM USUARIOS WHERE Id_usuario = ?";

        try (Connection con = ConexioDB.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }
}
