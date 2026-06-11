/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.*;
import java.util.ArrayList;

/**
 *
 * @author USUARIO
 */
  

    public class VentaDAO {

        public boolean registrarVentaCompleta(VentaDTO venta, ArrayList<DetalleVentaDTO> detalles) {
            Connection cn = null;
            PreparedStatement pstVenta = null;
            PreparedStatement pstDetalle = null;
            PreparedStatement pstStock = null;
            PreparedStatement pstCajaAct = null;
            ResultSet rs = null;

            String sqlVenta = "INSERT INTO VENTA (Id_caja, Id_usuario, Tipo_comprobante, Serie_comprobante, Numero_comprobante, Total, Metodo_pago) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String sqlDetalle = "INSERT INTO DETALLE_VENTA (Id_venta, Id_producto, Cantidad, Precio_unitario) VALUES (?, ?, ?, ?)";
            String sqlStock = "UPDATE ALMACEN_STOCK SET stock_actual = stock_actual - ? WHERE Id_producto = ?";
            String sqlActualizarMontoCaja = "UPDATE APERTURA_CIERRE_CAJA SET Monto_ventas_calculado = Monto_ventas_calculado + ? WHERE Id_caja = ?";

            try {
                // 1. Obtener la conexión real de tu proyecto
                cn = ConexioDB.getConexion();

                // ¡MUY IMPORTANTE!: Desactivar el autocommit para manejar la transacción manualmente
                cn.setAutoCommit(false);

                // 2. Insertar la Cabecera de la Venta (Se pide el ID generado)
                pstVenta = cn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
                pstVenta.setInt(1, venta.getIdCaja());
                pstVenta.setInt(2, venta.getIdUsuario());
                pstVenta.setString(3, venta.getTipoComprobante());
                pstVenta.setString(4, venta.getSerieComprobante());
                pstVenta.setString(5, venta.getNumeroComprobante());
                pstVenta.setDouble(6, venta.getTotal());
                pstVenta.setString(7, venta.getMetodoPago());

                int filasAfectadas = pstVenta.executeUpdate();
                if (filasAfectadas == 0) {
                    throw new SQLException("Error: No se pudo guardar la cabecera de la venta.");
                }

                // Recuperar el Id_venta autogenerado
                rs = pstVenta.getGeneratedKeys();
                int idVentaGenerado = 0;
                if (rs.next()) {
                    idVentaGenerado = rs.getInt(1);
                }

                // 3. Preparar los Statements para el bucle del Detalle y Stock
                pstDetalle = cn.prepareStatement(sqlDetalle);
                pstStock = cn.prepareStatement(sqlStock);

                // Recorrer la lista de productos enviados desde la JTable
                for (DetalleVentaDTO det : detalles) {
                    // Registrar Detalle
                    pstDetalle.setInt(1, idVentaGenerado);
                    pstDetalle.setInt(2, det.getIdProducto());
                    pstDetalle.setInt(3, det.getCantidad());
                    pstDetalle.setDouble(4, det.getPrecioUnitario());
                    pstDetalle.addBatch(); // Se acumula para enviarse en conjunto (más rápido)

                    // Restar Stock
                    pstStock.setInt(1, det.getCantidad());
                    pstStock.setInt(2, det.getIdProducto());
                    pstStock.addBatch();
                }

                // Ejecutar las inserciones y actualizaciones en lote
                pstDetalle.executeBatch();
                pstStock.executeBatch();

                // 4. Actualizar el monto acumulado en la caja abierta actual
                pstCajaAct = cn.prepareStatement(sqlActualizarMontoCaja);
                pstCajaAct.setDouble(1, venta.getTotal());
                pstCajaAct.setInt(2, venta.getIdCaja());
                pstCajaAct.executeUpdate();

                // SI TODO SALIÓ BIEN, SE GUARDAN LOS CAMBIOS PERMANENTEMENTE
                cn.commit();
                return true;

            } catch (SQLException e) {
                System.out.println("--- ERROR CRÍTICO EN LA TRANSACCIÓN ---");
                e.printStackTrace(); // Esto te dirá la línea exacta y la razón en la consola

                // Muestra el mensaje real del motor de base de datos en pantalla
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Error de Base de Datos: " + e.getMessage(),
                        "Error Interno",
                        javax.swing.JOptionPane.ERROR_MESSAGE);

                if (cn != null) {
                    try {
                        cn.rollback();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                return false;
            }

            finally {
            // Cerrar todas las conexiones de forma segura
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstVenta != null) {
                    pstVenta.close();
                }
                if (pstDetalle != null) {
                    pstDetalle.close();
                }
                if (pstStock != null) {
                    pstStock.close();
                }
                if (pstCajaAct != null) {
                    pstCajaAct.close();
                }
                if (cn != null) {
                    cn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar componentes: " + e.getMessage());
            }
        }
    }

    // Método auxiliar indispensable para verificar si hay una caja abierta y obtener su ID
    public int obtenerIdCajaAbierta() {
        int idCaja = 0;
        String sql = "SELECT TOP 1 Id_caja FROM APERTURA_CIERRE_CAJA WHERE Estado = 1 ORDER BY Fecha_apertura DESC";
        try (Connection cn = ConexioDB.getConexion(); PreparedStatement pst = cn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                idCaja = rs.getInt("Id_caja");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar caja abierta: " + e.getMessage());
        }
        return idCaja;
    }
}
