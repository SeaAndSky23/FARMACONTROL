/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
import conexion.ConexioDB;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class ProductoDAO {

    private Connection cn = ConexioDB.getConexion();
    private static final String SQL_BASE
            = "SELECT p.Id_producto, p.codigo_barras, p.descripcion, "
            + "m.nombre_marca, pa.nombre_principio, c.valor_concentracion, "
            + "f.nombre_forma, p.fecha_vencimiento, cv.nombre_condicion, "
            + "p.precio_venta, p.precio_compra "
            + "FROM PRODUCTO p "
            + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
            + "INNER JOIN PRINCIPIO_ACTIVO pa ON p.Id_principio = pa.Id_principio "
            + "INNER JOIN CONCENTRACION c ON p.Id_concentracion = c.Id_concentracion "
            + "INNER JOIN FORMA_FARMACEUTICA f ON p.Id_forma = f.Id_forma "
            + "INNER JOIN CONDICION_VENTA cv ON p.Id_condicion = cv.Id_condicion ";

    public List<Object[]> listarProductos() {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "ORDER BY p.Id_producto";

        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    public List<Object[]> buscarPorDescripcion(String descripcion) {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "WHERE p.descripcion LIKE ? ORDER BY p.Id_producto";

        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, "%" + descripcion + "%");
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar producto por descripción: " + e.getMessage());
        }
        return lista;
    }

    private Object[] mapearFila(ResultSet rs) throws SQLException {
        Object[] fila = new Object[11];
        fila[0] = rs.getInt("Id_producto");
        fila[1] = rs.getString("codigo_barras");
        fila[2] = rs.getString("descripcion");
        fila[3] = rs.getString("nombre_marca");
        fila[4] = rs.getString("nombre_principio");
        fila[5] = rs.getString("valor_concentracion");
        fila[6] = rs.getString("nombre_forma");
        fila[7] = rs.getDate("fecha_vencimiento");
        fila[8] = rs.getString("nombre_condicion");
        fila[9] = rs.getDouble("precio_venta");   
        fila[10] = rs.getDouble("precio_compra"); 
        return fila;
    }

    public Object[] buscarPorCodigoBarras(String codigo) {
        Object[] producto = null;
        String sql = "SELECT p.Id_producto, p.codigo_barras, "
                + "(p.descripcion + ' ' + m.nombre_marca) AS desc_com, "
                + "pp.medida, pp.precio_venta, pp.aplica_igv "
                + "FROM PRODUCTO p "
                + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
                + "INNER JOIN PRODUCTO_PRESENTACION pp ON p.Id_producto = pp.Id_producto "
                + "WHERE p.codigo_barras = ?";
        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, codigo);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                producto = new Object[6];
                producto[0] = rs.getInt("Id_producto");
                producto[1] = rs.getString("codigo_barras");
                producto[2] = rs.getString("desc_com");
                producto[3] = rs.getString("medida");
                producto[4] = rs.getDouble("precio_venta");
                producto[5] = rs.getBoolean("aplica_igv");
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar producto: " + e.getMessage());
        }
        return producto;
    }
}
