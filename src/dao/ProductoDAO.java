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
import modelo.ObjetoCombo;

/**
 *
 * @author USUARIO
 */
public class ProductoDAO {

    private Connection cn = ConexioDB.getConexion();

    private static final String SQL_BASE = "SELECT * FROM VW_PRODUCTOS_COMPLETO ";

    public List<Object[]> listarProductos() {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "ORDER BY Id_producto, Id_presentacion";

        try (PreparedStatement pst = cn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Object[]> buscarPorDescripcion(String descripcion) {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "WHERE descripcion LIKE ? ORDER BY Id_producto, Id_presentacion";

        try (PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setString(1, "%" + descripcion + "%");
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Object[] mapearFila(ResultSet rs) throws SQLException {
        Object[] fila = new Object[15];
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
        fila[13] = rs.getInt("stock_actual");
        fila[14] = rs.getInt("stock_minimo");
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

        String sqlStock = "INSERT INTO ALMACEN_STOCK (Id_producto, stock_actual, stock_minimo) VALUES (?, 0, ?)";

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
            pstStock.setInt(2, producto.getStockMinimo());
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

    public boolean actualizarStockMinimo(int idProducto, int stockMinimo) {
        String sql = "MERGE INTO ALMACEN_STOCK AS Destino "
                + "USING (SELECT ? AS Id_producto) AS Origen "
                + "ON (Destino.Id_producto = Origen.Id_producto) "
                + "WHEN MATCHED THEN UPDATE SET stock_minimo = ? "
                + "WHEN NOT MATCHED THEN INSERT (Id_producto, stock_actual, stock_minimo) VALUES (Origen.Id_producto, 0, ?);";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            ps.setInt(2, stockMinimo);
            ps.setInt(3, stockMinimo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al actualizar stock mínimo: " + e.getMessage());
            return false;
        }
    }

    //MOSTRAR TODAS LAS PRESENTACIONES POR PRODUCTO
    public List<Object[]> listarPresentacionesPorCodigoBarras(String codigo) {
        List<Object[]> lista = new ArrayList<>();
        String sql = SQL_BASE + "WHERE codigo_barras = ? ORDER BY Id_presentacion ASC";

        try (PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setString(1, codigo.trim());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[7];
                    fila[0] = rs.getInt("Id_producto");
                    fila[1] = rs.getString("descripcion") + " " + rs.getString("nombre_marca");
                    fila[2] = rs.getInt("Id_presentacion");
                    fila[3] = rs.getString("nombre_unidad");
                    fila[4] = rs.getInt("multiplo");
                    fila[5] = rs.getDouble("precio_venta");
                    fila[6] = rs.getBoolean("aplica_igv");
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

//PARA EDITAR PRODUCTOS
    public Object[] cargarCabeceraPorId(int idProducto) {
        Object[] datos = null;
        String sql = "SELECT p.Id_producto, p.codigo_barras, p.descripcion, "
                + "p.Id_marca, p.Id_principio, p.Id_concentracion, "
                + "p.Id_forma, p.fecha_vencimiento, p.Id_condicion, "
                + "ISNULL(a.stock_minimo, 0) AS stock_minimo "
                + "FROM PRODUCTO p "
                + "LEFT JOIN ALMACEN_STOCK a ON p.Id_producto = a.Id_producto "
                + "WHERE p.Id_producto = ?";
        try (PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idProducto);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    datos = new Object[10];
                    datos[0] = rs.getInt("Id_producto");
                    datos[1] = rs.getString("codigo_barras");
                    datos[2] = rs.getString("descripcion");
                    datos[3] = rs.getInt("Id_marca");
                    datos[4] = rs.getInt("Id_principio");
                    datos[5] = rs.getInt("Id_concentracion");
                    datos[6] = rs.getInt("Id_forma");
                    datos[7] = rs.getDate("fecha_vencimiento");
                    datos[8] = rs.getInt("Id_condicion");
                    datos[9] = rs.getInt("stock_minimo"); // NUEVO
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return datos;
    }

    public List<Object[]> cargarPresentacionesPorId(int idProducto) {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT pp.Id_presentacion, pp.Id_unidad, um.nombre_unidad, "
                + "um.multiplo_defecto, pp.multiplo, pp.aplica_igv, "
                + "pp.precio_venta, pp.precio_compra "
                + "FROM PRODUCTO_PRESENTACION pp "
                + "INNER JOIN UNIDAD_MEDIDA um ON pp.Id_unidad = um.Id_unidad "
                + "WHERE pp.Id_producto = ? ORDER BY pp.Id_presentacion ASC";
        try (PreparedStatement pst = cn.prepareStatement(sql)) {
            pst.setInt(1, idProducto);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Object[] fila = new Object[8];
                    fila[0] = rs.getInt("Id_presentacion");
                    fila[1] = rs.getInt("Id_unidad");
                    fila[2] = rs.getString("nombre_unidad");
                    fila[3] = rs.getInt("multiplo_defecto");
                    fila[4] = rs.getInt("multiplo");
                    fila[5] = rs.getBoolean("aplica_igv");
                    fila[6] = rs.getDouble("precio_venta");
                    fila[7] = rs.getDouble("precio_compra");
                    lista.add(fila);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizarProductoConPresentaciones(ProductoDTO producto, List<PresentacionDTO> presentaciones) {
        Connection con = null;
        try {
            con = ConexioDB.getConexion();
            con.setAutoCommit(false);

            // 1. Actualizar cabecera PRODUCTO
            String sqlProd = "UPDATE PRODUCTO SET "
                    + "codigo_barras=?, descripcion=?, Id_marca=?, Id_principio=?, "
                    + "Id_concentracion=?, Id_forma=?, fecha_vencimiento=?, Id_condicion=? "
                    + "WHERE Id_producto=?";
            try (PreparedStatement pst = con.prepareStatement(sqlProd)) {
                pst.setString(1, producto.getCodigoBarras());
                pst.setString(2, producto.getDescripcion());
                pst.setInt(3, producto.getIdMarca());
                pst.setInt(4, producto.getIdPrincipio());
                pst.setInt(5, producto.getIdConcentracion());
                pst.setInt(6, producto.getIdForma());
                pst.setDate(7, new java.sql.Date(producto.getFechaVencimiento().getTime()));
                pst.setInt(8, producto.getIdCondicion());
                pst.setInt(9, producto.getIdProducto()); // necesitas este getter
                pst.executeUpdate();
            }

            // 2. Eliminar presentaciones anteriores
            String sqlDel = "DELETE FROM PRODUCTO_PRESENTACION WHERE Id_producto=?";
            try (PreparedStatement pst = con.prepareStatement(sqlDel)) {
                pst.setInt(1, producto.getIdProducto());
                pst.executeUpdate();
            }

            // 3. Reinsertar presentaciones
            String sqlPres = "INSERT INTO PRODUCTO_PRESENTACION "
                    + "(Id_producto, Id_unidad, multiplo, aplica_igv, precio_venta, precio_compra) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = con.prepareStatement(sqlPres)) {
                for (PresentacionDTO pres : presentaciones) {
                    pst.setInt(1, producto.getIdProducto());
                    pst.setInt(2, pres.getIdUnidad());
                    pst.setInt(3, pres.getMultiplo());
                    pst.setBoolean(4, pres.isAplicaIgv());
                    pst.setDouble(5, pres.getPrecioVenta());
                    pst.setDouble(6, pres.getPrecioCompra());
                    pst.addBatch();
                }
                pst.executeBatch();
            }
            // 4. Actualizar stock mínimo (no tocar stock_actual, eso lo maneja AlmacenDAO)
            String sqlStockMin = "UPDATE ALMACEN_STOCK SET stock_minimo = ? WHERE Id_producto = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlStockMin)) {
                pst.setInt(1, producto.getStockMinimo());
                pst.setInt(2, producto.getIdProducto());
                pst.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("--- ERROR AL ACTUALIZAR PRODUCTO ---");
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
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<ObjetoCombo> listarUnidadesMedida() {
        List<ObjetoCombo> lista = new ArrayList<>();
        String sql = "SELECT Id_unidad, nombre_unidad, multiplo_defecto "
                + "FROM UNIDAD_MEDIDA ORDER BY nombre_unidad ASC";

        try (PreparedStatement pst = cn.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                lista.add(new ObjetoCombo(
                        rs.getInt("Id_unidad"),
                        rs.getString("nombre_unidad"),
                        rs.getInt("multiplo_defecto")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public int insertarUnidadMedida(String nombre, int multiploDefecto) {
        String sql = "INSERT INTO UNIDAD_MEDIDA (nombre_unidad, multiplo_defecto) VALUES (?, ?)";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setInt(2, multiploDefecto);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar unidad de medida: " + e.getMessage());
        }
        return -1;
    }
}
