/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import modelo.ObjetoCombo;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import modelo.PresentacionDTO;
import dao.ProductoDAO;
import modelo.ProductoDTO;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import dao.MaestroDAO;

/**
 *
 * @author USUARIO
 */
public class pmercaderia extends javax.swing.JPanel {

    private DefaultTableModel modeloPresentaciones;
    private int idProductoEnEdicion = 0;
    private javax.swing.JButton btnEditar;

    public pmercaderia() {
        initComponents();
        envolverEnScrollPane();
        llenarCombo(cbMarca, "MARCA", "Id_marca", "nombre_marca");
        llenarCombo(cbPactivo, "PRINCIPIO_ACTIVO", "Id_principio", "nombre_principio");
        llenarCombo(cbConcentracion, "CONCENTRACION", "Id_concentracion", "valor_concentracion");
        llenarCombo(cbFfarma, "FORMA_FARMACEUTICA", "Id_forma", "nombre_forma");
        llenarCombo(cbCondventa, "CONDICION_VENTA", "Id_condicion", "nombre_condicion");
        configurarTablaPresentaciones();
        aplicarModo(0);
    }

    @SuppressWarnings("unchecked")
    public void llenarCombo(JComboBox combo, String tabla, String campoId, String campoNombre) {
        combo.removeAllItems();
        combo.addItem(new ObjetoCombo(0, ""));

        MaestroDAO maestroDAO = new MaestroDAO();
        for (ObjetoCombo item : maestroDAO.listarMaestro(tabla, campoId, campoNombre)) {
            combo.addItem(item);
        }
        combo.setSelectedIndex(0);
    }

    public void agregarNuevoElementoMaestro(JComboBox combo, String tabla, String columna, String tituloVentana) {
        String nuevoValor = JOptionPane.showInputDialog(this,
                "Ingrese el nuevo valor para " + tituloVentana + ":",
                tituloVentana, JOptionPane.QUESTION_MESSAGE);

        if (nuevoValor != null && !nuevoValor.trim().isEmpty()) {
            MaestroDAO maestroDAO = new MaestroDAO();
            boolean ok = maestroDAO.insertarMaestro(tabla, columna, nuevoValor.trim());

            if (ok) {
                String campoId = "Id_" + columna.split("_")[1]; // heurística igual que antes
                llenarCombo(combo, tabla, campoId, columna);

                for (int i = 0; i < combo.getItemCount(); i++) {
                    ObjetoCombo item = (ObjetoCombo) combo.getItemAt(i);
                    if (item.getNombre().equalsIgnoreCase(nuevoValor.trim())) {
                        combo.setSelectedIndex(i);
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "El registro ya existe o hubo un error.");
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
                        return Object.class;
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
        combo.addItem(new ObjetoCombo(0, "", 1));

        ProductoDAO productoDAO = new ProductoDAO();
        for (ObjetoCombo u : productoDAO.listarUnidadesMedida()) {
            combo.addItem(u);
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

        ProductoDAO productoDAO = new ProductoDAO();
        int nuevoId = productoDAO.insertarUnidadMedida(nombre, multiplo);

        if (nuevoId > 0) {
            cargarComboMedida(combo);
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).getId() == nuevoId) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "La unidad ya existe o hubo un error.");
            combo.setSelectedIndex(0);
        }
    }

    private void cargarProductoParaEdicion(int idProducto) {
        ProductoDAO dao = new ProductoDAO();

        // --- Cabecera ---
        Object[] cab = dao.cargarCabeceraPorId(idProducto);
        if (cab == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el producto.");
            return;
        }

        idProductoEnEdicion = idProducto;

        txtCodigoBarras.setText((String) cab[1]);
        txtDescripcion.setText((String) cab[2]);

        seleccionarCombo(cbMarca, (int) cab[3]);
        seleccionarCombo(cbPactivo, (int) cab[4]);
        seleccionarCombo(cbConcentracion, (int) cab[5]);
        seleccionarCombo(cbFfarma, (int) cab[6]);
        seleccionarCombo(cbCondventa, (int) cab[8]);
        stockMinimo.setValue((int) cab[9]);

        // Fecha de vencimiento
        txtFechaVencimiento.setDate((java.util.Date) cab[7]);

        // --- Presentaciones ---
        modeloPresentaciones.setRowCount(0);
        List<Object[]> pres = dao.cargarPresentacionesPorId(idProducto);
        for (Object[] p : pres) {
            // Construir ObjetoCombo para la columna Medida
            ObjetoCombo unidad = new ObjetoCombo(
                    (int) p[1], // Id_unidad
                    (String) p[2], // nombre_unidad
                    (int) p[3] // multiplo_defecto
            );
            modeloPresentaciones.addRow(new Object[]{
                unidad,
                (int) p[4], // multiplo
                (boolean) p[5], // aplica_igv
                (double) p[6], // precio_venta
                (double) p[7] // precio_compra
            });
        }
        aplicarModo(1);
    }

    private void aplicarModo(int modo) {
        switch (modo) {
            case 0: // CREAR
                btnguardar.setEnabled(true);
                btneditar.setEnabled(false);
                btncerrar.setEnabled(false);
                btnuevo.setEnabled(false);
                btnbuscar.setEnabled(true);
                setFormularioEditable(true);
                break;
            case 1: // LECTURA
                btnguardar.setEnabled(false);
                btneditar.setEnabled(true);
                btncerrar.setEnabled(false);
                btnuevo.setEnabled(true);
                btnbuscar.setEnabled(true);
                setFormularioEditable(false);
                break;
            case 2: // EDITANDO
                btnguardar.setEnabled(true);
                btneditar.setEnabled(false);
                btncerrar.setEnabled(true);
                btnuevo.setEnabled(false);
                btnbuscar.setEnabled(false);
                setFormularioEditable(true);
                break;
        }
    }

    private void setFormularioEditable(boolean editable) {
        txtCodigoBarras.setEditable(editable);
        txtDescripcion.setEditable(editable);
        cbMarca.setEnabled(editable);
        cbPactivo.setEnabled(editable);
        cbConcentracion.setEnabled(editable);
        cbFfarma.setEnabled(editable);
        cbCondventa.setEnabled(editable);
        txtFechaVencimiento.setEnabled(editable);

        btnagmarca.setEnabled(editable);
        btnpactivo.setEnabled(editable);
        btnconcentracion.setEnabled(editable);
        btnfarma.setEnabled(editable);
        btncventa.setEnabled(editable);

        tpresentacion.setEnabled(editable);
        btnagre.setEnabled(editable);
        btnelimi.setEnabled(editable);
    }

    private void seleccionarCombo(javax.swing.JComboBox<?> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            ObjetoCombo item = (ObjetoCombo) combo.getItemAt(i);
            if (item.getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void envolverEnScrollPane() {
        this.remove(jPanel2);
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(jPanel2);
        scroll.setHorizontalScrollBarPolicy(
                javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(
                javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        this.setLayout(new java.awt.BorderLayout());
        this.add(scroll, java.awt.BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel2 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        btnuevo = new javax.swing.JButton();
        btneditar = new javax.swing.JButton();
        btnbuscar = new javax.swing.JButton();
        btncerrar = new javax.swing.JButton();
        btnsalir = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnguardar = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtDescripcion = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtCodigoBarras = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        cbCondventa = new javax.swing.JComboBox<>();
        btncventa = new javax.swing.JButton();
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
        txtFechaVencimiento = new com.toedter.calendar.JDateChooser();
        jLabel11 = new javax.swing.JLabel();
        stockMinimo = new javax.swing.JSpinner();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tpresentacion = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        btnagre = new javax.swing.JButton();
        btnelimi = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 47, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 380, Short.MAX_VALUE)
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        btnuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/nuevo1.png"))); // NOI18N
        btnuevo.setToolTipText("");
        btnuevo.setBorder(null);
        btnuevo.setBorderPainted(false);
        btnuevo.setContentAreaFilled(false);
        btnuevo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnuevo.setFocusPainted(false);
        btnuevo.addActionListener(this::btnuevoActionPerformed);

        btneditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/editar.png"))); // NOI18N
        btneditar.setBorderPainted(false);
        btneditar.setContentAreaFilled(false);
        btneditar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btneditar.setFocusPainted(false);
        btneditar.addActionListener(this::btneditarActionPerformed);

        btnbuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/buscarprod.png"))); // NOI18N
        btnbuscar.setBorder(null);
        btnbuscar.setBorderPainted(false);
        btnbuscar.setContentAreaFilled(false);
        btnbuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnbuscar.setFocusPainted(false);
        btnbuscar.addActionListener(this::btnbuscarActionPerformed);

        btncerrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/cancelar.png"))); // NOI18N
        btncerrar.setBorder(null);
        btncerrar.setBorderPainted(false);
        btncerrar.setContentAreaFilled(false);
        btncerrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btncerrar.setFocusPainted(false);
        btncerrar.addActionListener(this::btncerrarActionPerformed);

        btnsalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/salir.png"))); // NOI18N
        btnsalir.setText("SALIR");
        btnsalir.setBorder(null);
        btnsalir.setBorderPainted(false);
        btnsalir.setContentAreaFilled(false);
        btnsalir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnsalir.setFocusPainted(false);
        btnsalir.addActionListener(this::btnsalirActionPerformed);

        jLabel10.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel10.setText("BUSCAR PRODUCTO");

        jLabel13.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel13.setText("EDITAR");

        jLabel14.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel14.setText("NUEVO");

        jLabel15.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel15.setText("CANCELAR");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnguardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/guardar.png"))); // NOI18N
        btnguardar.setBorder(null);
        btnguardar.setBorderPainted(false);
        btnguardar.setContentAreaFilled(false);
        btnguardar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnguardar.setFocusPainted(false);
        btnguardar.addActionListener(this::btnguardarActionPerformed);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel12.setText("GUARDAR");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnguardar)
                    .addComponent(jLabel12))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnguardar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel12)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(195, 195, 195)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnuevo)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel14)))
                .addGap(70, 70, 70)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btneditar)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel13)))
                .addGap(54, 54, 54)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(btnbuscar))
                    .addComponent(jLabel10))
                .addGap(65, 65, 65)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btncerrar)
                    .addComponent(jLabel15))
                .addGap(82, 82, 82)
                .addComponent(btnsalir, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(118, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btnuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14)
                        .addGap(6, 6, 6))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(btncerrar)
                                .addGap(18, 18, 18))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(btnsalir)
                                .addGap(9, 9, 9)))
                        .addComponent(jLabel15)))
                .addGap(7, 7, 7))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btneditar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel13))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addComponent(btnbuscar)
                            .addGap(18, 18, 18)
                            .addComponent(jLabel10))))
                .addContainerGap())
        );

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CREAR PRODUCTO", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18))); // NOI18N

        jLabel2.setText("DESCRIPCION");

        jLabel9.setText("CODIGO");

        jLabel8.setText("CONDICION DE VENTA");

        cbCondventa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbCondventa.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btncventa.setText("+");
        btncventa.addActionListener(this::btncventaActionPerformed);

        jLabel7.setText("FECHA VENCIMIENTO");

        jLabel6.setText("FORMA FARMACEUTICA");

        cbFfarma.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbFfarma.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnfarma.setText("+");
        btnfarma.addActionListener(this::btnfarmaActionPerformed);

        jLabel3.setText("MARCA");

        jLabel4.setText("PRINCIPIO ACTIVO");

        jLabel5.setText("CONCENTRACION");

        cbMarca.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbMarca.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cbPactivo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbPactivo.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        cbConcentracion.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbConcentracion.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        btnconcentracion.setText("+");
        btnconcentracion.addActionListener(this::btnconcentracionActionPerformed);

        btnpactivo.setText("+");
        btnpactivo.addActionListener(this::btnpactivoActionPerformed);

        btnagmarca.setText("+");
        btnagmarca.addActionListener(this::btnagmarcaActionPerformed);

        jLabel11.setText("STOCK MINIMO");

        stockMinimo.setModel(new javax.swing.SpinnerNumberModel());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(149, 149, 149)
                        .addComponent(txtCodigoBarras, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel2)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(151, 151, 151)
                        .addComponent(txtDescripcion, javax.swing.GroupLayout.PREFERRED_SIZE, 497, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3)
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
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addGap(18, 18, 18)
                            .addComponent(cbFfarma, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnfarma))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(jLabel8)
                                    .addGap(27, 27, 27))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel7)
                                        .addComponent(jLabel11))
                                    .addGap(32, 32, 32)))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(cbCondventa, 0, 193, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btncventa))
                                .addComponent(txtFechaVencimiento, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(stockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)))))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(stockMinimo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(txtFechaVencimiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(450, 250));

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
        tpresentacion.setPreferredSize(new java.awt.Dimension(200, 80));
        jScrollPane1.setViewportView(tpresentacion);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));

        btnagre.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/mas.png"))); // NOI18N
        btnagre.setBorder(null);
        btnagre.setBorderPainted(false);
        btnagre.setContentAreaFilled(false);
        btnagre.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnagre.setFocusPainted(false);
        btnagre.addActionListener(this::btnagreActionPerformed);

        btnelimi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/menos.png"))); // NOI18N
        btnelimi.setBorder(null);
        btnelimi.setBorderPainted(false);
        btnelimi.setContentAreaFilled(false);
        btnelimi.setFocusPainted(false);
        btnelimi.addActionListener(this::btnelimiActionPerformed);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnagre)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnelimi))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnagre, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnelimi, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(212, 212, 212)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(40, 40, 40)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(126, 126, 126)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 12, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnsalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnsalirActionPerformed
        java.awt.Container padre = this.getParent();

        if (padre != null) {
            padre.remove(this);

            padre.revalidate();
            padre.repaint();
        }
    }//GEN-LAST:event_btnsalirActionPerformed

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
            producto.setFechaVencimiento(txtFechaVencimiento.getDate());
            producto.setStockMinimo((Integer) stockMinimo.getValue());

            // 2. Recorrer la tabla y armar la lista de presentaciones
            List<PresentacionDTO> presentaciones = new ArrayList<>();
            for (int i = 0; i < modeloPresentaciones.getRowCount(); i++) {
                Object valorMedida = modeloPresentaciones.getValueAt(i, 0);

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
                        "POR FAVOR LLENE TODOS LOS CAMPOS.",
                        "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (PresentacionDTO pres : presentaciones) {
                if (pres.getPrecioVenta() <= 0) {
                    JOptionPane.showMessageDialog(this,
                            "El precio de venta no puede ser 0 ni negativo.",
                            "Precio inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // 3. Guardar: CREAR o ACTUALIZAR según el modo
            ProductoDAO dao = new ProductoDAO();
            boolean exito;

            if (idProductoEnEdicion > 0) {
                producto.setIdProducto(idProductoEnEdicion);
                exito = dao.actualizarProductoConPresentaciones(producto, presentaciones);
            } else {
                exito = dao.guardarProductoConPresentaciones(producto, presentaciones);
            }

            if (exito) {
                javax.swing.JOptionPane.showMessageDialog(this,
                        idProductoEnEdicion > 0 ? "Producto actualizado con éxito." : "Producto registrado con éxito.");
                idProductoEnEdicion = 0;
                limpiarFormulario();
                aplicarModo(0);
                limpiarFormulario();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Error al guardar el producto.",
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error de datos: " + e.getMessage(),
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
        idProductoEnEdicion = 0;
        limpiarTablaPresentaciones();
        txtFechaVencimiento.setDate(null);
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
        stockMinimo.setValue(0);

    }

    public void resetear() {
        idProductoEnEdicion = 0;
        limpiarFormulario();
        aplicarModo(0);
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

    private void btnbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbuscarActionPerformed
        JDproducto dialogo = new JDproducto((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialogo.setVisible(true);

        Integer idSeleccionado = dialogo.getIdProductoSeleccionado();
        if (idSeleccionado == null) {
            return; // usuario cerró sin elegir
        }
        cargarProductoParaEdicion(idSeleccionado);
    }//GEN-LAST:event_btnbuscarActionPerformed

    private void btnagreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnagreActionPerformed
        modeloPresentaciones.addRow(new Object[]{"", 1, true, 0.0, 0.0});
    }//GEN-LAST:event_btnagreActionPerformed

    private void btnelimiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnelimiActionPerformed
        int fila = tpresentacion.getSelectedRow();
        if (fila != -1) {
            modeloPresentaciones.removeRow(fila);
        }
    }//GEN-LAST:event_btnelimiActionPerformed

    private void btneditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btneditarActionPerformed
        aplicarModo(2);
    }//GEN-LAST:event_btneditarActionPerformed

    private void btnuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnuevoActionPerformed
        limpiarFormulario();
        aplicarModo(0);
    }//GEN-LAST:event_btnuevoActionPerformed

    private void btncerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncerrarActionPerformed
        if (idProductoEnEdicion > 0) {
            // Estaba editando → volver a modo lectura sin perder los datos
            aplicarModo(1);
        } else {
            // Estaba creando → limpiar todo
            limpiarFormulario();
            aplicarModo(0);
        }
    }//GEN-LAST:event_btncerrarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnagmarca;
    private javax.swing.JButton btnagre;
    private javax.swing.JButton btnbuscar;
    private javax.swing.JButton btncerrar;
    private javax.swing.JButton btnconcentracion;
    private javax.swing.JButton btncventa;
    private javax.swing.JButton btneditar;
    private javax.swing.JButton btnelimi;
    private javax.swing.JButton btnfarma;
    private javax.swing.JButton btnguardar;
    private javax.swing.JButton btnpactivo;
    private javax.swing.JButton btnsalir;
    private javax.swing.JButton btnuevo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cbConcentracion;
    private javax.swing.JComboBox<String> cbCondventa;
    private javax.swing.JComboBox<String> cbFfarma;
    private javax.swing.JComboBox<String> cbMarca;
    private javax.swing.JComboBox<String> cbPactivo;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
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
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner stockMinimo;
    private javax.swing.JTable tpresentacion;
    private javax.swing.JTextField txtCodigoBarras;
    private javax.swing.JTextField txtDescripcion;
    private com.toedter.calendar.JDateChooser txtFechaVencimiento;
    // End of variables declaration//GEN-END:variables
}
