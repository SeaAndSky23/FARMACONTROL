/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;

/**
 *
 * @author USUARIO
 */
public class ClienteDAO {

    public Object[] buscarPorDni(String dni) {
        String sql = "SELECT id_cliente, nombre_completo, celular FROM cliente WHERE dni = ?";
        try (java.sql.Connection con = ConexioDB.getConexion(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Object[]{
                    rs.getInt("id_cliente"),
                    rs.getString("nombre_completo"),
                    rs.getString("celular")
                };
            }
        } catch (Exception e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }
    // AGREGA ESTO DENTRO DE TU CLASE ClienteDAO

    public boolean registrarCliente(String dni, String nombre, String celular) {
        String sql = "INSERT INTO cliente (dni, nombre_completo, celular) VALUES (?, ?, ?)";
        // Nota: Asegúrate de que los nombres de la tabla y columnas coincidan con tu Base de Datos

        try (java.sql.Connection con = ConexioDB.getConexion(); java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setString(3, celular);

            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0; // Retorna true si se insertó correctamente

        } catch (Exception e) {
            System.out.println("Error al registrar cliente: " + e.getMessage());
            return false;
        }
    }
}
