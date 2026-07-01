/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import dao.ProductoDAO;

/**
 *
 * @author USUARIO
 */
public class JDproducto extends javax.swing.JDialog {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private Object[] productoSeleccionado = null;
    private String codigoBarrasSeleccionado = null;
    private Integer idProductoSeleccionado = null;

    public String getCodigoBarrasSeleccionado() {
        return codigoBarrasSeleccionado;
    }

    public Object[] getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public Integer getIdProductoSeleccionado() {
        return idProductoSeleccionado;
    }

    private final String[] columnas = {
        "ID", "Código de Barras", "Descripción", "Marca", "Principio Activo",
        "Concentración", "Forma Farmacéutica", "Fecha Vencimiento",
        "Condición de Venta", "Medida", "Múltiplo", "Precio Venta", "Precio Compra",
        "Stock Actual", "Stock Mínimo"
    };
    private static final int COL_MEDIDA = 9;
    private static final int COL_MULTIPLO = 10;
    private static final int COL_STOCK_ACTUAL = 13;
    private static final int COL_STOCK_MINIMO = 14;

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JDproducto.class.getName());

    /**
     * Creates new form JDproducto
     */
    public JDproducto(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setTitle("Catálogo de Productos");
        setLocationRelativeTo(parent);
        configurarResaltadoStock();
        cargarProductos();

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                cargarProductos();
            }
        });

        txtbuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                buscarProducto();
            }
        });

        tproducto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int fila = tproducto.getSelectedRow();
                    if (fila >= 0) {
                        idProductoSeleccionado = (Integer) tproducto.getValueAt(fila, 0); // columna 0 = Id_producto
                        codigoBarrasSeleccionado = tproducto.getValueAt(fila, 1).toString();
                        dispose();
                    }
                }
            }
        });
    }

    private void configurarResaltadoStock() {
        final java.awt.Color ROJO_ALERTA = new java.awt.Color(255, 190, 190);       // stock negativo
        final java.awt.Color CELESTE_GRIS = new java.awt.Color(231,245,246);      // stock bajo (0 < actual <= mínimo)

        tproducto.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                java.awt.Color colorFondo = java.awt.Color.WHITE;
                try {
                    Object actualObj = table.getModel().getValueAt(row, COL_STOCK_ACTUAL);
                    Object minimoObj = table.getModel().getValueAt(row, COL_STOCK_MINIMO);
                    if (actualObj != null && minimoObj != null) {
                        int actual = ((Number) actualObj).intValue();
                        int minimo = ((Number) minimoObj).intValue();

                        if (actual < 0) {
                            colorFondo = ROJO_ALERTA;
                        } else if (actual > 0 && actual <= minimo) {
                            colorFondo = CELESTE_GRIS;
                        }
                    }
                } catch (Exception ex) {
                    // fila sin datos numéricos válidos, ignorar
                }

                if (!isSelected) {
                    c.setBackground(colorFondo);
                } else {
                    c.setBackground(colorFondo.equals(java.awt.Color.WHITE)
                            ? table.getSelectionBackground()
                            : colorFondo.darker());
                }
                return c;
            }
        });
    }

    public void ajustarAnchosTabla(JTable tabla) {
        int[] anchos = {20, 120, 200, 140, 140, 130, 100, 90, 110, 90, 70, 85, 85, 90, 90};
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            if (i < anchos.length) {
                tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
            }
        }
    }

    private void cargarProductos() {
        List<Object[]> productos = productoDAO.listarProductos();
        llenarTabla(productos);
    }

    private void buscarProducto() {
        String texto = txtbuscar.getText().trim();

        if (texto.isEmpty()) {
            cargarProductos();
            return;
        }

        List<Object[]> productos = productoDAO.buscarPorDescripcion(texto);
        llenarTabla(productos);
    }

    private void ocultarColumnas(JTable tabla) {
        int[] colsOcultas = {COL_MEDIDA, COL_MULTIPLO};
        for (int col : colsOcultas) {
            if (col < tabla.getColumnCount()) {
                tabla.getColumnModel().getColumn(col).setMinWidth(0);
                tabla.getColumnModel().getColumn(col).setMaxWidth(0);
                tabla.getColumnModel().getColumn(col).setPreferredWidth(0);
            }
        }
    }

    private void llenarTabla(List<Object[]> productos) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        java.util.Set<Integer> idsAgregados = new java.util.HashSet<>();

        for (Object[] fila : productos) {
            Integer idProducto = (Integer) fila[0];
            if (!idsAgregados.contains(idProducto)) {
                idsAgregados.add(idProducto);
                modelo.addRow(fila);
            }
        }

        tproducto.setModel(modelo);
        ajustarAnchosTabla(tproducto);
        ocultarColumnas(tproducto);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tproducto = new javax.swing.JTable();
        txtbuscar = new javax.swing.JTextField();
        btnbuscar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tproducto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tproducto);

        txtbuscar.addActionListener(this::txtbuscarActionPerformed);

        btnbuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/lupab.png"))); // NOI18N
        btnbuscar.setBorder(null);
        btnbuscar.setBorderPainted(false);
        btnbuscar.setContentAreaFilled(false);
        btnbuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnbuscar.setFocusPainted(false);
        btnbuscar.addActionListener(this::btnbuscarActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(167, 167, 167)
                .addComponent(txtbuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(btnbuscar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1236, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(57, 57, 57)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtbuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnbuscar))
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 685, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1260, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbuscarActionPerformed
        buscarProducto();
    }//GEN-LAST:event_btnbuscarActionPerformed

    private void txtbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtbuscarActionPerformed
        buscarProducto();
    }//GEN-LAST:event_txtbuscarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JDproducto dialog = new JDproducto(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnbuscar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tproducto;
    private javax.swing.JTextField txtbuscar;
    // End of variables declaration//GEN-END:variables
}
