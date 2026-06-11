/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import java.sql.*;
import conexion.ConexioDB;

/**
 *
 * @author USUARIO
 */
public class ProductoDAO {
    private Connection cn = ConexioDB.getConexion();
    
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
