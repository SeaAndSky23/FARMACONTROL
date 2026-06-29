/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author USUARIO
 */
public class AlmacenDAO {

    public List<Object[]> buscarPresentacionesPorCodigo(String codigo) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT p.Id_producto, "
                + "(p.descripcion + ' ' + m.nombre_marca) AS desc_com, "
                + "um.nombre_unidad, pp.multiplo "
                + "FROM PRODUCTO p "
                + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
                + "INNER JOIN PRODUCTO_PRESENTACION pp ON p.Id_producto = pp.Id_producto "
                + "INNER JOIN UNIDAD_MEDIDA um ON pp.Id_unidad = um.Id_unidad "
                + "WHERE p.codigo_barras = ? "
                + "ORDER BY pp.Id_presentacion ASC";

        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Object[]{
                        rs.getInt("Id_producto"),
                        rs.getString("desc_com"),
                        rs.getString("nombre_unidad"),
                        rs.getInt("multiplo")
                    });
                }
            }
        } catch (SQLException e) {
            System.out.println("AlmacenDAO – buscar: " + e.getMessage());
        }
        return lista;
    }

    // ─── GUARDAR MOVIMIENTO ───────────────────────────────────────────────────
    /**
     * Procesa un movimiento de almacén en transacción atómica.
     *
     * @param tipoMovimiento Texto del ComboBox ("Ingreso", "Inventario por
     * Producto", etc.)
     * @param filas Filas válidas de la tabla. Cada Object[] tiene: [3] int –
     * cantidad ingresada [4] int – multiplo [5] int – Id_producto
     */
    public boolean guardarMovimientoStock(String tipoMovimiento, List<Object[]> filas) {
        String sqlReset = "UPDATE ALMACEN_STOCK SET stock_actual = 0 WHERE Id_producto = ?";

        String sqlUpdate = "MERGE INTO ALMACEN_STOCK AS Destino "
                + "USING (SELECT ? AS Id_producto) AS Origen "
                + "ON (Destino.Id_producto = Origen.Id_producto) "
                + "WHEN MATCHED THEN "
                + "    UPDATE SET stock_actual = Destino.stock_actual + ? "
                + "WHEN NOT MATCHED THEN "
                + "    INSERT (Id_producto, stock_actual) VALUES (Origen.Id_producto, ?);";

        String sqlHistorial = "INSERT INTO HISTORIAL_INVENTARIO "
                + "(Id_producto, tipo_movimiento, cantidad, fecha_movimiento) "
                + "VALUES (?, ?, ?, GETDATE())";

        Connection con = null;
        try {
            con = ConexioDB.getConexion();
            con.setAutoCommit(false);

            // PASO 1: Si es inventario físico, resetear stock de los productos listados
            if (tipoMovimiento.equalsIgnoreCase("Inventario por Producto")) {
                try (PreparedStatement psReset = con.prepareStatement(sqlReset)) {
                    for (Object[] fila : filas) {
                        psReset.setInt(1, toInt(fila[5]));
                        psReset.addBatch();
                    }
                    psReset.executeBatch();
                }
            }

            // PASO 2: Actualizar stock + registrar en historial/kárdex
            try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate); PreparedStatement psHistorial = con.prepareStatement(sqlHistorial)) {

                for (int i = 0; i < filas.size(); i++) {
                    Object[] fila = filas.get(i);
                    int idProd = toInt(fila[5]);
                    int cantidad = toInt(fila[3]);
                    int multiplo = toInt(fila[4]);
                    int cantidadStock = cantidad * multiplo;

                    if (cantidad <= 0) {
                        con.rollback();
                        JOptionPane.showMessageDialog(null,
                                "Error en fila " + (i + 1) + ": la cantidad debe ser mayor a 0.",
                                "Valor inválido", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    psUpdate.setInt(1, idProd);
                    psUpdate.setInt(2, cantidadStock);
                    psUpdate.setInt(3, cantidadStock);
                    psUpdate.addBatch();

                    psHistorial.setInt(1, idProd);
                    psHistorial.setString(2, tipoMovimiento.toUpperCase());
                    psHistorial.setInt(3, cantidadStock);
                    psHistorial.addBatch();
                }

                psUpdate.executeBatch();
                psHistorial.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("AlmacenDAO – transacción: " + e.getMessage());
            if (con != null) try {
                con.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null,
                    "Error de Base de Datos: " + e.getMessage(),
                    "Error Interno", JOptionPane.ERROR_MESSAGE);
            return false;
        } finally {
            if (con != null) try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ─── UTILITARIO ──────────────────────────────────────────────────────────
    private int toInt(Object obj) {
        if (obj == null || obj.toString().trim().isEmpty()) {
            return 0;
        }
        return Integer.parseInt(obj.toString().trim());
    }
}
