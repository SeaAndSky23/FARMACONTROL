/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.*;
import modelo.Caja;

/**
 *
 * @author USUARIO
 */
public class CajaDAO {

    public boolean aperturarCaja(Caja caja) {
        String sql = "INSERT INTO APERTURA_CIERRE_CAJA (Id_usuario, Fecha_apertura, Monto_apertura, Estado) VALUES (?, GETDATE(), ?, 1)";
        try (Connection cn = ConexioDB.getConexion(); // Ajusta según el método real de tu ConexioDB
                 PreparedStatement pst = cn.prepareStatement(sql)) {

            pst.setInt(1, caja.getIdUsuario());
            pst.setDouble(2, caja.getMontoApertura());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al aperturar caja: " + e.getMessage());
            return false;
        }
    }

    // Método 2: Verificar si el usuario ya tiene una caja abierta (Estado = 1)
    public Caja obtenerCajaActiva(int idUsuario) {
        String sql = "SELECT * FROM APERTURA_CIERRE_CAJA WHERE Id_usuario = ? AND Estado = 1";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {

            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    Caja caja = new Caja();
                    caja.setIdCaja(rs.getInt("Id_caja"));
                    caja.setIdUsuario(rs.getInt("Id_usuario"));
                    caja.setMontoApertura(rs.getDouble("Monto_apertura"));
                    caja.setFechaApertura(rs.getTimestamp("Fecha_apertura"));
                    caja.setEstado(rs.getBoolean("Estado"));
                    return caja; // Retorna la caja activa encontrada
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar caja activa: " + e.getMessage());
        }
        return null; // Si no hay cajas abiertas, retorna null
    }

    public int obtenerCajaAbierta(int idUsuario) {

        String sql = """
        SELECT TOP 1 Id_caja
        FROM APERTURA_CIERRE_CAJA
        WHERE Id_usuario = ?
        AND Estado = 1
    """;

        try (Connection cn = ConexioDB.getConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("Id_caja");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
