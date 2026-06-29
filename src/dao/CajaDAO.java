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

    // En CajaDAO — reemplaza tu método aperturarCaja actual
    public boolean aperturarCaja(Caja caja) {
        // Primero verificar que este usuario no tenga ya una caja abierta
        String sqlCheck = "SELECT COUNT(*) FROM APERTURA_CIERRE_CAJA "
                + "WHERE Id_usuario = ? AND Estado = 1";
        String sqlInsert = "INSERT INTO APERTURA_CIERRE_CAJA "
                + "(Id_usuario, Monto_apertura, Monto_ventas_calculado, Estado) "
                + "VALUES (?, ?, 0, 1)";
        try (Connection cn = ConexioDB.getConexion()) {
            // Verificar si ya tiene caja abierta
            try (PreparedStatement pstCheck = cn.prepareStatement(sqlCheck)) {
                pstCheck.setInt(1, caja.getIdUsuario());
                ResultSet rs = pstCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Ya tiene una caja abierta, no permitir otra
                    return false;
                }
            }
            // Insertar nueva apertura
            try (PreparedStatement pst = cn.prepareStatement(sqlInsert)) {
                pst.setInt(1, caja.getIdUsuario());
                pst.setDouble(2, caja.getMontoApertura());
                int filas = pst.executeUpdate();
                return filas > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error al aperturar caja: " + e.getMessage());
            return false;
        }
    }

    // Obtiene la caja activa del usuario logueado con todos sus datos de apertura
    public Caja obtenerCajaActiva(int idUsuario) {
        Caja caja = null;
        String sql = "SELECT TOP 1 Id_caja, Id_usuario, Fecha_apertura, Monto_apertura, "
                + "Monto_ventas_calculado, Estado "
                + "FROM APERTURA_CIERRE_CAJA "
                + "WHERE Id_usuario = ? AND Estado = 1 "
                + "ORDER BY Fecha_apertura DESC";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    caja = new Caja();
                    caja.setIdCaja(rs.getInt("Id_caja"));
                    caja.setIdUsuario(rs.getInt("Id_usuario"));
                    caja.setFechaApertura(rs.getTimestamp("Fecha_apertura"));
                    caja.setMontoApertura(rs.getDouble("Monto_apertura"));
                    caja.setMontoVentasCalculado(rs.getDouble("Monto_ventas_calculado"));
                    caja.setEstado(rs.getBoolean("Estado"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener caja activa: " + e.getMessage());
        }
        return caja;
    }

// Devuelve el total vendido en efectivo durante la caja activa
    public double obtenerTotalEfectivoPorCaja(int idCaja) {
        double total = 0.0;
        String sql = "SELECT ISNULL(SUM(v.Total), 0) AS total_efectivo "
                + "FROM VENTA v "
                + "INNER JOIN METODO_PAGO mp ON v.Id_metodo_pago = mp.Id_metodo_pago "
                + "WHERE v.Id_caja = ? AND UPPER(mp.nombre_metodo) = 'EFECTIVO'";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idCaja);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total_efectivo");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener total efectivo: " + e.getMessage());
        }
        return total;
    }

// Devuelve el total vendido con billetera digital durante la caja activa
    public double obtenerTotalBilleteraPorCaja(int idCaja) {
        double total = 0.0;
        // Ajusta el nombre del método según como lo tengas guardado en tu tabla METODO_PAGO
        String sql = "SELECT ISNULL(SUM(v.Total), 0) AS total_billetera "
                + "FROM VENTA v "
                + "INNER JOIN METODO_PAGO mp ON v.Id_metodo_pago = mp.Id_metodo_pago "
                + "WHERE v.Id_caja = ? AND UPPER(mp.nombre_metodo) != 'EFECTIVO'";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idCaja);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    total = rs.getDouble("total_billetera");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener total billetera: " + e.getMessage());
        }
        return total;
    }

// Ejecuta el cierre de caja registrando fecha, monto cierre y cambiando Estado a 0
    public boolean cerrarCaja(int idCaja, double montoCierre) {
        String sql = "UPDATE APERTURA_CIERRE_CAJA "
                + "SET Fecha_cierre = GETDATE(), Monto_cierre = ?, Estado = 0 "
                + "WHERE Id_caja = ? AND Estado = 1";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setDouble(1, montoCierre);
            pst.setInt(2, idCaja);
            int filas = pst.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.out.println("Error al cerrar caja: " + e.getMessage());
            return false;
        }
    }

    // método guardar resumen al cerrar
    public boolean guardarResumenCierre(int idCaja, double totalEfectivo,
            double totalBilletera, double totalVentas, double montoApertura,
            double efectivoFisico, double montoContado, double diferencia) {

        String sql = "INSERT INTO RESUMEN_CIERRE_CAJA "
                + "(Id_caja, total_efectivo, total_billetera, total_ventas, "
                + "monto_apertura, efectivo_fisico, monto_contado, diferencia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idCaja);
            pst.setDouble(2, totalEfectivo);
            pst.setDouble(3, totalBilletera);
            pst.setDouble(4, totalVentas);
            pst.setDouble(5, montoApertura);
            pst.setDouble(6, efectivoFisico);
            pst.setDouble(7, montoContado);
            pst.setDouble(8, diferencia);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al guardar resumen de cierre: " + e.getMessage());
            return false;
        }
    }
}
