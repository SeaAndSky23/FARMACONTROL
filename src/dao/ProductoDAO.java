/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import java.sql.Connection;
import modelo.ProductoDTO;
import modelo.PresentacionDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
            + "um.nombre_unidad, pp.multiplo, pp.aplica_igv, pp.precio_venta, pp.precio_compra "
            + "FROM PRODUCTO p "
            + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
            + "INNER JOIN PRINCIPIO_ACTIVO pa ON p.Id_principio = pa.Id_principio "
            + "INNER JOIN CONCENTRACION c ON p.Id_concentracion = c.Id_concentracion "
            + "INNER JOIN FORMA_FARMACEUTICA f ON p.Id_forma = f.Id_forma "
            + "INNER JOIN CONDICION_VENTA cv ON p.Id_condicion = cv.Id_condicion "
            + "LEFT JOIN PRODUCTO_PRESENTACION pp ON p.Id_producto = pp.Id_producto "
            + "LEFT JOIN UNIDAD_MEDIDA um ON pp.Id_unidad = um.Id_unidad ";

    public List<Object[]> listarProductos() {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "ORDER BY p.Id_producto, pp.Id_presentacion";

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
        String sql = SQL_BASE + "WHERE p.descripcion LIKE ? ORDER BY p.Id_producto, pp.Id_presentacion";

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
        Object[] fila = new Object[13];
        fila[0] = rs.getInt("Id_producto");
        fila[1] = rs.getString("codigo_barras");
        fila[2] = rs.getString("descripcion");
        fila[3] = rs.getString("nombre_marca");
        fila[4] = rs.getString("nombre_principio");
        fila[5] = rs.getString("valor_concentracion");
        fila[6] = rs.getString("nombre_forma");
        fila[7] = rs.getDate("fecha_vencimiento");
        fila[8] = rs.getString("nombre_condicion");
        fila[9] = rs.getString("nombre_unidad");
        fila[10] = rs.getObject("multiplo");
        fila[11] = rs.getDouble("precio_venta");
        fila[12] = rs.getDouble("precio_compra");
        return fila;
    }

    /**
     * Busca producto por código de barras leyendo SIEMPRE la presentación base
     * (la primera registrada) para evitar traer precios ambiguos cuando hay
     * varias presentaciones.
     */
    public Object[] buscarPorCodigoBarras(String codigo) {
        Object[] producto = null;
        String sql = "SELECT TOP 1 p.Id_producto, p.codigo_barras, "
                + "(p.descripcion + ' ' + m.nombre_marca) AS desc_com, "
                + "um.nombre_unidad AS medida, pp.precio_venta, pp.aplica_igv "
                + "FROM PRODUCTO p "
                + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
                + "INNER JOIN PRODUCTO_PRESENTACION pp ON p.Id_producto = pp.Id_producto "
                + "INNER JOIN UNIDAD_MEDIDA um ON pp.Id_unidad = um.Id_unidad "
                + "WHERE p.codigo_barras = ? "
                + "ORDER BY pp.Id_presentacion ASC";
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

    /**
     * Registra un producto y todas sus presentaciones (precios) en una sola
     * transacción.
     */
    public boolean guardarProductoConPresentaciones(ProductoDTO producto, List<PresentacionDTO> presentaciones) {
        Connection con = null;
        PreparedStatement pstProd = null;
        PreparedStatement pstPres = null;
        PreparedStatement pstStock = null;
        ResultSet rs = null;

        String sqlProd = "INSERT INTO PRODUCTO "
                + "(codigo_barras, descripcion, Id_marca, Id_principio, Id_concentracion, Id_forma, fecha_vencimiento, Id_condicion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlPres = "INSERT INTO PRODUCTO_PRESENTACION "
                + "(Id_producto, Id_unidad, multiplo, aplica_igv, precio_venta, precio_compra) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String sqlStock = "INSERT INTO ALMACEN_STOCK (Id_producto, stock_actual) VALUES (?, 0)";

        try {
            con = ConexioDB.getConexion();
            con.setAutoCommit(false);

            // 1. Insertar PRODUCTO
            pstProd = con.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS);
            pstProd.setString(1, producto.getCodigoBarras());
            pstProd.setString(2, producto.getDescripcion());
            pstProd.setInt(3, producto.getIdMarca());
            pstProd.setInt(4, producto.getIdPrincipio());
            pstProd.setInt(5, producto.getIdConcentracion());
            pstProd.setInt(6, producto.getIdForma());
            pstProd.setDate(7, new java.sql.Date(producto.getFechaVencimiento().getTime()));
            pstProd.setInt(8, producto.getIdCondicion());

            int filas = pstProd.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo registrar el producto.");
            }

            rs = pstProd.getGeneratedKeys();
            int idProductoGenerado = 0;
            if (rs.next()) {
                idProductoGenerado = rs.getInt(1);
            }

            // 2. Insertar cada PRESENTACIÓN (precio venta/compra) en lote
            pstPres = con.prepareStatement(sqlPres);
            for (PresentacionDTO pres : presentaciones) {
                pstPres.setInt(1, idProductoGenerado);
                pstPres.setInt(2, pres.getIdUnidad());
                pstPres.setInt(3, pres.getMultiplo());
                pstPres.setBoolean(4, pres.isAplicaIgv());
                pstPres.setDouble(5, pres.getPrecioVenta());
                pstPres.setDouble(6, pres.getPrecioCompra());
                pstPres.addBatch();
            }
            pstPres.executeBatch();

            // 3. Inicializar stock en 0
            pstStock = con.prepareStatement(sqlStock);
            pstStock.setInt(1, idProductoGenerado);
            pstStock.executeUpdate();

            con.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("--- ERROR AL REGISTRAR PRODUCTO ---");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstProd != null) {
                    pstProd.close();
                }
                if (pstPres != null) {
                    pstPres.close();
                }
                if (pstStock != null) {
                    pstStock.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                System.out.println("Error al cerrar componentes: " + e.getMessage());
            }
        }
    }

    //MOSTRAR TODAS LAS PRESENTACIONES POR PRODUCTO
    public List<Object[]> listarPresentacionesPorCodigoBarras(String codigo) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT p.Id_producto, "
                + "(p.descripcion + ' ' + m.nombre_marca) AS desc_com, "
                + "pp.Id_presentacion, um.nombre_unidad, pp.multiplo, "
                + "pp.precio_venta, pp.aplica_igv "
                + "FROM PRODUCTO p "
                + "INNER JOIN MARCA m ON p.Id_marca = m.Id_marca "
                + "INNER JOIN PRODUCTO_PRESENTACION pp ON p.Id_producto = pp.Id_producto "
                + "INNER JOIN UNIDAD_MEDIDA um ON pp.Id_unidad = um.Id_unidad "
                + "WHERE p.codigo_barras = ? "
                + "ORDER BY pp.Id_presentacion ASC";

        try {
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, codigo);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Object[] fila = new Object[7];
                fila[0] = rs.getInt("Id_producto");
                fila[1] = rs.getString("desc_com");
                fila[2] = rs.getInt("Id_presentacion");
                fila[3] = rs.getString("nombre_unidad");
                fila[4] = rs.getInt("multiplo");
                fila[5] = rs.getDouble("precio_venta");
                fila[6] = rs.getBoolean("aplica_igv");
                lista.add(fila);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar presentaciones: " + e.getMessage());
        }
        return lista;
    }
}
