/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import javax.swing.table.DefaultTableModel;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import conexion.ConexioDB;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Usuario
 */
public class palmacen extends javax.swing.JPanel {

    DefaultTableModel modeloAlmacen;

    /**
     * Creates new form PanelMercaderia
     */
    public palmacen() {
        initComponents();
        configurarTablaAlmacen();
        inicializarFilaVacia();
    }

    public void configurarTablaAlmacen() {
        String[] titulos = {"CÓDIGO DE BARRAS", "DESCRIPCIÓN", "UNIDAD", "CANTIDAD", "MULTIPLO", "ID"};

        modeloAlmacen = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 3;
            }
        };
        talmacen_ingreso.setModel(modeloAlmacen);

        // Ocultar MULTIPLO (col 4) e ID (col 5)
        talmacen_ingreso.getColumnModel().getColumn(4).setMinWidth(0);
        talmacen_ingreso.getColumnModel().getColumn(4).setMaxWidth(0);
        talmacen_ingreso.getColumnModel().getColumn(4).setPreferredWidth(0);
        talmacen_ingreso.getColumnModel().getColumn(5).setMinWidth(0);
        talmacen_ingreso.getColumnModel().getColumn(5).setMaxWidth(0);
        talmacen_ingreso.getColumnModel().getColumn(5).setPreferredWidth(0);

        modeloAlmacen.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                if (column == 0) {
                    Object cbObj = modeloAlmacen.getValueAt(row, 0);
                    if (cbObj != null && !cbObj.toString().trim().isEmpty()) {
                        buscarProductoPorCodigo(cbObj.toString().trim(), row);
                    }
                }
            }
        });
    }

    private void inicializarFilaVacia() {
        if (modeloAlmacen != null) {
            modeloAlmacen.addRow(new Object[]{"", "", "", "", "", ""});
        }
    }

    private void buscarProductoPorCodigo(String codigo, int fila) {
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
            List<Object[]> presentaciones = new ArrayList<>();

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    presentaciones.add(new Object[]{
                        rs.getInt("Id_producto"),
                        rs.getString("desc_com"),
                        rs.getString("nombre_unidad"),
                        rs.getInt("multiplo")
                    });
                }
            }

            if (presentaciones.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "El código '" + codigo + "' no existe en el sistema.",
                        "Producto no encontrado", JOptionPane.WARNING_MESSAGE);
                java.awt.EventQueue.invokeLater(()
                        -> modeloAlmacen.setValueAt("", fila, 0));
                return;
            }

            Object[] presSeleccionada;
            if (presentaciones.size() == 1) {
                presSeleccionada = presentaciones.get(0);
            } else {
                String[] opciones = new String[presentaciones.size()];
                for (int i = 0; i < presentaciones.size(); i++) {
                    Object[] p = presentaciones.get(i);
                    opciones[i] = String.format("%s (x%d)", p[2], p[3]);
                }
                String seleccion = (String) JOptionPane.showInputDialog(
                        this,
                        "Este producto tiene varias presentaciones.\nSeleccione la que está ingresando:",
                        "Seleccionar presentación",
                        JOptionPane.QUESTION_MESSAGE,
                        null, opciones, opciones[0]);

                if (seleccion == null) {
                    java.awt.EventQueue.invokeLater(()
                            -> modeloAlmacen.setValueAt("", fila, 0));
                    return;
                }
                int indice = java.util.Arrays.asList(opciones).indexOf(seleccion);
                presSeleccionada = presentaciones.get(indice);
            }

            final Object[] pres = presSeleccionada;
            java.awt.EventQueue.invokeLater(() -> {
                modeloAlmacen.setValueAt(pres[1], fila, 1); // Descripción
                modeloAlmacen.setValueAt(pres[2], fila, 2); // Unidad
                modeloAlmacen.setValueAt(1, fila, 3); // Cantidad = 1
                modeloAlmacen.setValueAt(pres[3], fila, 4); // Multiplo (oculto)
                modeloAlmacen.setValueAt(pres[0], fila, 5); // Id_producto (oculto)
            });

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        talmacen_ingreso = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        cbTipoMovimiento = new javax.swing.JComboBox<>();
        btnagregar = new javax.swing.JButton();
        btnguardar = new javax.swing.JButton();
        btnmostrar = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(808, 609));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setMinimumSize(new java.awt.Dimension(0, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(985, 985));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "INGRESAR MERCADERIA AL ALMACEN", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18))); // NOI18N

        talmacen_ingreso.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6"
            }
        ));
        jScrollPane1.setViewportView(talmacen_ingreso);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tipo Movimiento", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        cbTipoMovimiento.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AJUSTE", "INVENTARIO POR PRODUCTO" }));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addComponent(cbTipoMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(cbTipoMovimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        btnagregar.setText("+");
        btnagregar.addActionListener(this::btnagregarActionPerformed);

        btnguardar.setText("GUARDAR");
        btnguardar.addActionListener(this::btnguardarActionPerformed);

        btnmostrar.setText("MOSTRAR TODOS LOS PRODUCTOS");
        btnmostrar.addActionListener(this::btnmostrarActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(62, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 661, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnagregar)
                .addGap(32, 32, 32))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(176, 176, 176)
                        .addComponent(btnguardar))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(btnmostrar, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(111, 111, 111)
                        .addComponent(btnguardar))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addComponent(btnmostrar)
                .addGap(44, 44, 44)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnagregar)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41))
        );

        jButton6.setText("CERRAR");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 887, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 692, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        java.awt.Container padre = this.getParent();

        if (padre != null) {
            padre.remove(this);

            padre.revalidate();
            padre.repaint();
        }
    }//GEN-LAST:event_jButton6ActionPerformed

    private void btnguardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnguardarActionPerformed
        // Validar que la tabla contenga información procesable
        if (modeloAlmacen.getRowCount() == 0 || modeloAlmacen.getValueAt(0, 3).toString().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La tabla está vacía o no tiene productos válidos cargados.", "Datos insuficientes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener el tipo de movimiento seleccionado en el ComboBox
        String tipoMovimiento = cbTipoMovimiento.getSelectedItem().toString();

        Connection con = null;
        PreparedStatement psReset = null;
        PreparedStatement psUpdate = null;
        PreparedStatement psHistorial = null;

        try {
            con = ConexioDB.getConexion();
            con.setAutoCommit(false); // Transacción atómica segura

            // Definición de consultas SQL transaccionales
            String sqlReset = "UPDATE ALMACEN_STOCK SET stock_actual = 0 WHERE Id_producto = ?";

            String sqlUpdate = "MERGE INTO ALMACEN_STOCK AS Destino "
            + "USING (SELECT ? AS Id_producto) AS Origen "
            + "ON (Destino.Id_producto = Origen.Id_producto) "
            + "WHEN MATCHED THEN "
            + "    UPDATE SET stock_actual = Destino.stock_actual + ? "
            + "WHEN NOT MATCHED THEN "
            + "    INSERT (Id_producto, stock_actual) VALUES (Origen.Id_producto, ?);";

            String sqlHistorial = "INSERT INTO HISTORIAL_INVENTARIO (Id_producto, tipo_movimiento, cantidad, fecha_movimiento) VALUES (?, ?, ?, GETDATE())";

            psReset = con.prepareStatement(sqlReset);
            psUpdate = con.prepareStatement(sqlUpdate);
            psHistorial = con.prepareStatement(sqlHistorial);

            // --- PASO 1: Si es INVENTARIO POR PRODUCTO, borrón y cuenta nueva a 0 de los ítems en lista ---
            if (tipoMovimiento.equalsIgnoreCase("Inventario por Producto")) {
                for (int i = 0; i < modeloAlmacen.getRowCount(); i++) {
                    if (modeloAlmacen.getValueAt(i, 5) != null && !modeloAlmacen.getValueAt(i, 5).toString().isEmpty()) {
                        int idProd = Integer.parseInt(modeloAlmacen.getValueAt(i, 5).toString());
                        psReset.setInt(1, idProd);
                        psReset.addBatch();
                    }
                }
                psReset.executeBatch();
            }

            // --- PASO 2: Inyección de stock y generación de Kárdex/Historial ---
            for (int i = 0; i < modeloAlmacen.getRowCount(); i++) {
                if (modeloAlmacen.getValueAt(i, 5) == null || modeloAlmacen.getValueAt(i, 5).toString().isEmpty()
                    || modeloAlmacen.getValueAt(i, 3) == null || modeloAlmacen.getValueAt(i, 2).toString().isEmpty()) {
                    continue; // Ignora filas que se encuentren incompletas
                }

                int idProd = Integer.parseInt(modeloAlmacen.getValueAt(i, 5).toString());
                int cantidad = Integer.parseInt(modeloAlmacen.getValueAt(i, 3).toString());
                Object multiploObj = modeloAlmacen.getValueAt(i, 4);
                int multiplo = (multiploObj != null && !multiploObj.toString().isEmpty())
                ? Integer.parseInt(multiploObj.toString()) : 1;
                int cantidadStock = cantidad * multiplo;

                if (cantidad <= 0) {
                    con.rollback(); // Cancela la operación completa ante inconsistencias
                    JOptionPane.showMessageDialog(this, "Error en fila " + (i + 1) + ": La cantidad de ingreso debe ser mayor a 0.", "Valor inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Parámetros de actualización de Stock
                psUpdate.setInt(1, idProd);
                psUpdate.setInt(2, cantidadStock);
                psUpdate.setInt(3, cantidadStock);
                psUpdate.addBatch();

                // Parámetros de Auditoría/Historial
                psHistorial.setInt(1, idProd);
                psHistorial.setString(2, tipoMovimiento.toUpperCase());
                psHistorial.setInt(3, cantidadStock);
                psHistorial.addBatch();
            }

            // Ejecución por bloques de comandos (Batch processing)
            psUpdate.executeBatch();
            psHistorial.executeBatch();

            con.commit(); // Todo se guardó correctamente sin fallos
            JOptionPane.showMessageDialog(this, "¡Movimiento de Almacén (" + tipoMovimiento + ") procesado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);

            // Reestructuración y reinicio limpio de la interfaz gráfica
            modeloAlmacen.setRowCount(0);
            inicializarFilaVacia();

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Error crítico al procesar el ingreso de almacén: " + e.getMessage(), "Error de Transacción", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (psReset != null) {
                    psReset.close();
                }
                if (psUpdate != null) {
                    psUpdate.close();
                }
                if (psHistorial != null) {
                    psHistorial.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }//GEN-LAST:event_btnguardarActionPerformed

    private void btnagregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnagregarActionPerformed
        modeloAlmacen.addRow(new Object[]{"", "", "", "", "", ""});
    }//GEN-LAST:event_btnagregarActionPerformed

    private void btnmostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmostrarActionPerformed
       
    }//GEN-LAST:event_btnmostrarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnagregar;
    private javax.swing.JButton btnguardar;
    private javax.swing.JButton btnmostrar;
    private javax.swing.JComboBox<String> cbTipoMovimiento;
    private javax.swing.JButton jButton6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable talmacen_ingreso;
    // End of variables declaration//GEN-END:variables
}
