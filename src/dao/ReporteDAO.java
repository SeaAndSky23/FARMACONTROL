/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    // RF10 - Listado de ventas filtrado por fecha o rango de fechas
    public List<Object[]> listarVentasPorRango(Date desde, Date hasta) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT v.Fecha_venta, v.Numero_comprobante, tc.nombre_tipo, "
                + "ISNULL(c.nombre_completo, 'SIN CLIENTE') AS cliente, "
                + "mp.nombre_metodo, u.Nombre_usuario, v.Total "
                + "FROM VENTA v "
                + "INNER JOIN TIPO_COMPROBANTE tc ON v.Id_tipo_comprobante = tc.Id_tipo_comprobante "
                + "INNER JOIN METODO_PAGO mp      ON v.Id_metodo_pago      = mp.Id_metodo_pago "
                + "INNER JOIN USUARIOS u          ON v.Id_usuario          = u.Id_usuario "
                + "LEFT  JOIN CLIENTE c           ON v.id_cliente          = c.id_cliente "
                + "WHERE CAST(v.Fecha_venta AS DATE) BETWEEN ? AND ? "
                + "ORDER BY v.Fecha_venta ASC";

        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setDate(1, desde);
            pst.setDate(2, hasta);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[7];
                    fila[0] = rs.getTimestamp("Fecha_venta");
                    fila[1] = rs.getString("Numero_comprobante");
                    fila[2] = rs.getString("nombre_tipo");
                    fila[3] = rs.getString("cliente");
                    fila[4] = rs.getString("nombre_metodo");
                    fila[5] = rs.getString("Nombre_usuario");
                    fila[6] = rs.getDouble("Total");
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar ventas por rango: " + e.getMessage());
        }
        return lista;
    }

    /**
     * RF11 - Calcula Ingresos y Egresos del periodo. Egresos: se identifica
     * para cada línea vendida la presentación cuyo precio_venta coincide con el
     * Precio_unitario registrado, y se toma su precio_compra.
     *
     * @return double[]{ingresos, egresos}
     */
    public double[] calcularBalance(Date desde, Date hasta) {
        double ingresos = 0;
        double egresos = 0;

        String sqlIngresos = "SELECT ISNULL(SUM(v.Total),0) AS ingresos "
                + "FROM VENTA v "
                + "WHERE CAST(v.Fecha_venta AS DATE) BETWEEN ? AND ?";

        String sqlEgresos = "SELECT ISNULL(SUM(dv.Cantidad * pp.precio_compra),0) AS egresos "
                + "FROM DETALLE_VENTA dv "
                + "INNER JOIN VENTA v ON dv.Id_venta = v.Id_venta "
                + "INNER JOIN PRODUCTO_PRESENTACION pp "
                + "        ON pp.Id_producto = dv.Id_producto "
                + "       AND pp.precio_venta = dv.Precio_unitario "
                + "WHERE CAST(v.Fecha_venta AS DATE) BETWEEN ? AND ?";

        try (Connection cn = ConexioDB.getConexion()) {

            try (PreparedStatement pst = cn.prepareStatement(sqlIngresos)) {
                pst.setDate(1, desde);
                pst.setDate(2, hasta);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        ingresos = rs.getDouble("ingresos");
                    }
                }
            }

            try (PreparedStatement pst = cn.prepareStatement(sqlEgresos)) {
                pst.setDate(1, desde);
                pst.setDate(2, hasta);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        egresos = rs.getDouble("egresos");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al calcular balance: " + e.getMessage());
        }

        return new double[]{ingresos, egresos};
    }
}
