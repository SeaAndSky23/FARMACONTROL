/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import java.sql.*;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import modelo.ObjetoCombo;
import conexion.ConexioDB;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import DTO.PresentacionDTO;
import dao.ProductoDAO;
import DTO.ProductoDTO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;

/**
 *
 * @author USUARIO
 */
public class pmercaderia extends javax.swing.JPanel {

    private DefaultTableModel modeloPresentaciones;

    /**
     * Creates new form pmercaderia
     */
    public pmercaderia() {
        initComponents();
        llenarCombo(cbMarca, "MARCA", "Id_marca", "nombre_marca");
        llenarCombo(cbPactivo, "PRINCIPIO_ACTIVO", "Id_principio", "nombre_principio");
        llenarCombo(cbConcentracion, "CONCENTRACION", "Id_concentracion", "valor_concentracion");
        llenarCombo(cbFfarma, "FORMA_FARMACEUTICA", "Id_forma", "nombre_forma");
        llenarCombo(cbCondventa, "CONDICION_VENTA", "Id_condicion", "nombre_condicion");
        configurarTablaPresentaciones();
    }

    @SuppressWarnings("unchecked")
    public void llenarCombo(JComboBox combo, String tabla, String campoId, String campoNombre) {
        combo.removeAllItems();

        // 1. Agregamos una opción vacía al inicio
        combo.addItem(new ObjetoCombo(0, ""));

        String sql = "SELECT " + campoId + ", " + campoNombre + " FROM " + tabla + " ORDER BY " + campoNombre + " ASC";

        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt(campoId);
                String nombre = rs.getString(campoNombre);

                // Agregamos las instancias reales de la base de datos
                combo.addItem(new ObjetoCombo(id, nombre));
            }

            // para que el combo apunte a la opción vacía apenas cargue
            combo.setSelectedIndex(0);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error al llenar combo " + tabla + ": " + e.getMessage());
        }
    }

    public void agregarNuevoElementoMaestro(JComboBox combo, String tabla, String columna, String tituloVentana) {
        String nuevoValor = JOptionPane.showInputDialog(this, "Ingrese el nuevo valor para " + tituloVentana + ":", tituloVentana, JOptionPane.QUESTION_MESSAGE);

        if (nuevoValor != null && !nuevoValor.trim().isEmpty()) {
            String sql = "INSERT INTO " + tabla + " (" + columna + ") VALUES (?)";

            try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, nuevoValor.trim());
                ps.executeUpdate();

                // Refrescamos el combo usando tu función existente
                llenarCombo(combo, tabla, combo.getItemAt(0) != null ? "Id_" + columna.split("_")[1] : "Id", columna);

                // Seleccionamos el elemento recién creado
                for (int i = 0; i < combo.getItemCount(); i++) {
                    ObjetoCombo item = (ObjetoCombo) combo.getItemAt(i);
                    if (item.getNombre().equalsIgnoreCase(nuevoValor.trim())) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "El registro ya existe o hubo un error: " + e.getMessage());
            }
        }
    }

    private DefaultComboBoxModel<ObjetoCombo> modeloComboMedida;

    private void configurarTablaPresentaciones() {
        String[] columnas = {"Medida", "Múltiplo", "Aplica IGV", "P. Venta", "P. Compra"};

        modeloPresentaciones = new DefaultTableModel(null, columnas) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1:
                        return Integer.class;
                    case 2:
                        return Boolean.class;
                    case 3:
                        return Double.class;
                    case 4:
                        return Double.class;
                    default:
                        return Object.class; // Medida ahora guarda ObjetoCombo
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 1;
            }
        };

        tpresentacion.setModel(modeloPresentaciones);

        // --- Configurar combo editor para la columna 0 (Medida) ---
        JComboBox<ObjetoCombo> comboMedida = new JComboBox<>();
        cargarComboMedida(comboMedida);

        comboMedida.addActionListener(e -> {
            ObjetoCombo seleccionado = (ObjetoCombo) comboMedida.getSelectedItem();
            if (seleccionado != null && seleccionado.getId() == -1) {
                // Opción especial "+ Nueva unidad..."
                abrirDialogoNuevaUnidad(comboMedida);
            }
        });

        tpresentacion.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboMedida));

        // Renderer para que se vea el texto correctamente cuando no se está editando
        tpresentacion.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof ObjetoCombo) {
                    setText(((ObjetoCombo) value).getNombre());
                } else {
                    setText("");
                }
                return this;
            }
        });

        // Fila inicial vacía
        modeloPresentaciones.addRow(new Object[]{null, 1, true, 0.0, 0.0});

        comboMedida.addActionListener(e -> {
            ObjetoCombo seleccionado = (ObjetoCombo) comboMedida.getSelectedItem();
            if (seleccionado == null) {
                return;
            }

            if (seleccionado.getId() == -1) {
                abrirDialogoNuevaUnidad(comboMedida);
                return;
            }

            // Autocompletar el múltiplo en la fila que se está editando
            int filaActual = tpresentacion.getEditingRow();
            if (filaActual >= 0 && seleccionado.getId() > 0) {
                modeloPresentaciones.setValueAt(seleccionado.getMultiploDefecto(), filaActual, 1);
            }
        });
    }

    private void cargarComboMedida(JComboBox<ObjetoCombo> combo) {
        combo.removeAllItems();
        combo.addItem(new ObjetoCombo(0, "", 1)); // opción vacía

        String sql = "SELECT Id_unidad, nombre_unidad, multiplo_defecto FROM UNIDAD_MEDIDA ORDER BY nombre_unidad ASC";
        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                combo.addItem(new ObjetoCombo(
                        rs.getInt("Id_unidad"),
                        rs.getString("nombre_unidad"),
                        rs.getInt("multiplo_defecto")
                ));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar unidades de medida: " + e.getMessage());
        }

        combo.addItem(new ObjetoCombo(-1, "+ Nueva unidad...", 1));
    }

    private void abrirDialogoNuevaUnidad(JComboBox<ObjetoCombo> combo) {
        JTextField campoNombre = new JTextField();
        JTextField campoMultiplo = new JTextField("1");

        JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Nombre de la unidad:"));
        panel.add(campoNombre);
        panel.add(new JLabel("Múltiplo por defecto:"));
        panel.add(campoMultiplo);

        int resultado = JOptionPane.showConfirmDialog(this, panel,
                "Nueva Unidad de Medida", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado != JOptionPane.OK_OPTION) {
            combo.setSelectedIndex(0);
            return;
        }

        String nombre = campoNombre.getText().trim();
        String multiploTexto = campoMultiplo.getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            combo.setSelectedIndex(0);
            return;
        }

        int multiplo;
        try {
            multiplo = Integer.parseInt(multiploTexto);
            if (multiplo <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El múltiplo debe ser un número entero mayor a 0.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            combo.setSelectedIndex(0);
            return;
        }

        String sql = "INSERT INTO UNIDAD_MEDIDA (nombre_unidad, multiplo_defecto) VALUES (?, ?)";

        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            ps.setInt(2, multiplo);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int nuevoId = 0;
            if (rs.next()) {
                nuevoId = rs.getInt(1);
            }

            // Recargar el combo y seleccionar la nueva unidad
            cargarComboMedida(combo);
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).getId() == nuevoId) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "La unidad ya existe o hubo un error: " + e.getMessage());
            combo.setSelectedIndex(0);
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
        jPanel2 = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtCodigoBarras = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cbCondventa = new javax.swing.JComboBox<>();
        btncventa = new javax.swing.JButton();
        txtFechaVencimiento = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbFfarma = new javax.swing.JComboBox<>();
        btnfarma = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbMarca = new javax.swing.JComboBox<>();
        cbPactivo = new javax.swing.JComboBox<>();
        cbConcentracion = new javax.swing.JComboBox<>();
        btnconcentracion = new javax.swing.JButton();
        btnpactivo = new javax.swing.JButton();
        btnagmarca = new javax.swing.JButton();
        btnmostrar = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpresentacion = new javax.swing.JTable();
        btnagre = new javax.swing.JButton();
        btnelimi = new javax.swing.JButton();
        btnguardar = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jButton4.setText("CERRAR");
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.addActionListener(this::jButton4ActionPerformed);

        jPanel3.setBackground(new java.awt.Color(239, 239, 239));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CREAR PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18))); // NOI18N

        jLabel2.setText("DESCRIPCION");

        jLabel9.setText("CODIGO");

        jLabel8.setText("CONDICION DE VENTA");

        cbCondventa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btncventa.setText("+");
        btncventa.addActionListener(this::btncventaActionPerformed);

        txtFechaVencimiento.setModel(new javax.swing.SpinnerDateModel());

        jLabel7.setText("FECHA VENCIMIENTO");

        jLabel6.setText("FORMA FARMACEUTICA");

        cbFfarma.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnfarma.setText("+");
        btnfarma.addActionListener(this::btnfarmaActionPerformed);

        jLabel3.setText("MARCA");

        jLabel4.setText("PRINCIPIO ACTIVO");

        jLabel5.setText("CONCENTRACION");

        cbMarca.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbPactivo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        cbConcentracion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btnconcentracion.setText("+");
        btnconcentracion.addActionListener(this::btnconcentracionActionPerformed);

        btnpactivo.setText("+");
        btnpactivo.addActionListener(this::btnpactivoActionPerformed);

        btnagmarca.setText("+");
        btnagmarca.addActionListener(this::btnagmarcaActionPerformed);

        btnmostrar.setText("MOSTRAR TODOS LOS PRODUCTOS");
        btnmostrar.addActionListener(this::btnmostrarActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnmostrar, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(151, 151, 151)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 497, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addGap(50, 50, 50)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cbConcentracion, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnconcentracion))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(cbPactivo, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnpactivo))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(cbMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnagmarca))))))
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addGap(27, 27, 27)
                            .addComponent(cbCondventa, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btncventa))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addGap(18, 18, 18)
                            .addComponent(cbFfarma, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnfarma)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(32, 32, 32)
                        .addComponent(txtFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(61, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnagmarca))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbPactivo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4)
                            .addComponent(btnpactivo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbConcentracion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5)
                            .addComponent(btnconcentracion))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbFfarma, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnfarma))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(cbCondventa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btncventa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addComponent(btnmostrar)
                .addContainerGap(27, Short.MAX_VALUE))
        );

        tpresentacion.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tpresentacion);

        btnagre.setText("+");
        btnagre.addActionListener(this::btnagreActionPerformed);

        btnelimi.setText("-");
        btnelimi.addActionListener(this::btnelimiActionPerformed);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 625, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnagre)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnelimi)
                .addGap(52, 52, 52))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnagre)
                    .addComponent(btnelimi))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
        );

        btnguardar.setText("GUARDAR");
        btnguardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnguardar.addActionListener(this::btnguardarActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                .addGap(123, 123, 123)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 109, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnguardar)
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(71, 71, 71))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnguardar, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(72, 72, 72))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        java.awt.Container padre = this.getParent();

        if (padre != null) {
            padre.remove(this);

            padre.revalidate();
            padre.repaint();
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void btnguardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnguardarActionPerformed
        // Validación: al menos una presentación
        if (modeloPresentaciones.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe agregar al menos una presentación con su precio.",
                    "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 1. Armar el ProductoDTO desde los campos del formulario
            ProductoDTO producto = new ProductoDTO();
            producto.setCodigoBarras(txtCodigoBarras.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            producto.setIdMarca(((ObjetoCombo) cbMarca.getSelectedItem()).getId());
            producto.setIdPrincipio(((ObjetoCombo) cbPactivo.getSelectedItem()).getId());
            producto.setIdConcentracion(((ObjetoCombo) cbConcentracion.getSelectedItem()).getId());
            producto.setIdForma(((ObjetoCombo) cbFfarma.getSelectedItem()).getId());
            producto.setIdCondicion(((ObjetoCombo) cbCondventa.getSelectedItem()).getId());
            producto.setFechaVencimiento((java.util.Date) txtFechaVencimiento.getValue());

            // 2. Recorrer la tabla y armar la lista de presentaciones
            List<PresentacionDTO> presentaciones = new ArrayList<>();
            for (int i = 0; i < modeloPresentaciones.getRowCount(); i++) {
                Object valorMedida = modeloPresentaciones.getValueAt(i, 0);

                // saltar filas vacías o sin unidad seleccionada
                if (valorMedida == null || !(valorMedida instanceof ObjetoCombo)) {
                    continue;
                }

                ObjetoCombo unidadSeleccionada = (ObjetoCombo) valorMedida;
                if (unidadSeleccionada.getId() == 0) {
                    continue;
                }

                PresentacionDTO pres = new PresentacionDTO();
                pres.setIdUnidad(unidadSeleccionada.getId());
                pres.setMultiplo((Integer) modeloPresentaciones.getValueAt(i, 1));
                pres.setAplicaIgv((Boolean) modeloPresentaciones.getValueAt(i, 2));
                pres.setPrecioVenta((Double) modeloPresentaciones.getValueAt(i, 3));
                pres.setPrecioCompra((Double) modeloPresentaciones.getValueAt(i, 4));

                int multiploEsperado = unidadSeleccionada.getMultiploDefecto();
                int multiploIngresado = (Integer) modeloPresentaciones.getValueAt(i, 1);

                if (multiploIngresado != multiploEsperado) {
                    JOptionPane.showMessageDialog(this,
                            "El múltiplo de '" + unidadSeleccionada.getNombre() + "' debe ser " + multiploEsperado + ".",
                            "Múltiplo inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                presentaciones.add(pres);
            }

            if (presentaciones.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Complete al menos una presentación válida.",
                        "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Guardar todo en una sola transacción
            ProductoDAO dao = new ProductoDAO();
            boolean exito = dao.guardarProductoConPresentaciones(producto, presentaciones);

            if (exito) {
                javax.swing.JOptionPane.showMessageDialog(this, "Producto registrado con éxito.");
                limpiarFormulario();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al registrar el producto.",
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de datos: " + e.getMessage(),
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnguardarActionPerformed

    private void limpiarTablaPresentaciones() {
        // 1. Eliminar todas las filas existentes
        modeloPresentaciones.setRowCount(0);

        // 2. Agregar una fila inicial vacía para que el usuario pueda seguir registrando
        modeloPresentaciones.addRow(new Object[]{null, 1, true, 0.0, 0.0});
    }

    private void limpiarFormulario() {
        txtDescripcion.setText("");
        txtCodigoBarras.setText("");

        limpiarTablaPresentaciones();

        // Al poner el índice en 0, regresarán a mostrar el campo vacío automáticamente
        if (cbMarca.getItemCount() > 0) {
            cbMarca.setSelectedIndex(0);
        }
        if (cbPactivo.getItemCount() > 0) {
            cbPactivo.setSelectedIndex(0);
        }
        if (cbConcentracion.getItemCount() > 0) {
            cbConcentracion.setSelectedIndex(0);
        }
        if (cbFfarma.getItemCount() > 0) {
            cbFfarma.setSelectedIndex(0);
        }
        if (cbCondventa.getItemCount() > 0) {
            cbCondventa.setSelectedIndex(0);
        }
    }
    private void btnagmarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnagmarcaActionPerformed
        agregarNuevoElementoMaestro(cbMarca, "MARCA", "nombre_marca", "Marca");
    }//GEN-LAST:event_btnagmarcaActionPerformed

    private void btnpactivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnpactivoActionPerformed
        agregarNuevoElementoMaestro(cbPactivo, "PRINCIPIO_ACTIVO", "nombre_principio", "Principio Activo");
    }//GEN-LAST:event_btnpactivoActionPerformed

    private void btnconcentracionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnconcentracionActionPerformed
        agregarNuevoElementoMaestro(cbConcentracion, "CONCENTRACION", "valor_concentracion", "Concentración");
    }//GEN-LAST:event_btnconcentracionActionPerformed

    private void btnfarmaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnfarmaActionPerformed
        agregarNuevoElementoMaestro(cbFfarma, "FORMA_FARMACEUTICA", "nombre_forma", "Forma Farmacéutica");
    }//GEN-LAST:event_btnfarmaActionPerformed

    private void btncventaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncventaActionPerformed
        agregarNuevoElementoMaestro(cbCondventa, "CONDICION_VENTA", "nombre_condicion", "Condición de Venta");
    }//GEN-LAST:event_btncventaActionPerformed

    private void btnmostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmostrarActionPerformed
        JDproducto dialogo = new JDproducto((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialogo.setVisible(true);
    }//GEN-LAST:event_btnmostrarActionPerformed

    private void btnagreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnagreActionPerformed
        modeloPresentaciones.addRow(new Object[]{"", 1, true, 0.0, 0.0});
    }//GEN-LAST:event_btnagreActionPerformed

    private void btnelimiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnelimiActionPerformed
        int fila = tpresentacion.getSelectedRow();
        if (fila != -1) {
            modeloPresentaciones.removeRow(fila);
        }
    }//GEN-LAST:event_btnelimiActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnagmarca;
    private javax.swing.JButton btnagre;
    private javax.swing.JButton btnconcentracion;
    private javax.swing.JButton btncventa;
    private javax.swing.JButton btnelimi;
    private javax.swing.JButton btnfarma;
    private javax.swing.JButton btnguardar;
    private javax.swing.JButton btnmostrar;
    private javax.swing.JButton btnpactivo;
    private javax.swing.JComboBox<String> cbConcentracion;
    private javax.swing.JComboBox<String> cbCondventa;
    private javax.swing.JComboBox<String> cbFfarma;
    private javax.swing.JComboBox<String> cbMarca;
    private javax.swing.JComboBox<String> cbPactivo;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tpresentacion;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JTextField txtDescripcion;
    private javax.swing.JSpinner txtFechaVencimiento;
    // End of variables declaration//GEN-END:variables
}
