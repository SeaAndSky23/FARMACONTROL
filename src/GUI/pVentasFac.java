/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.ArrayList;
import dao.ProductoDAO;
import dao.VentaDAO;
import java.awt.Frame;
import modelo.VentaDTO;
import modelo.DetalleVentaDTO;
import modelo.Sesion;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author Usuario
 */
  

    public class pVentasFac extends javax.swing.JPanel {

        private Integer idClienteSeleccionado = null;

        /**
         * Creates new form pVentasFac
         */
        public pVentasFac() {
            initComponents();
            configurarTablaVentas();
            this.revalidate();
            mostrarCajeroLogueado();
            configurarEventoTipoComprobante();
            actualizarSiguienteNumeroEnPantalla();
            this.repaint();
            configurarEventoDni();
            aplicarModo(0);
        }

        private DefaultTableModel modeloTabla;
        private int idClienteActual = 0;
        private int modoActual = 0;

        public void configurarTablaVentas() {
            // 2. Definir las columnas de la tabla (incluyendo ID oculto para la BD)
            String[] columnas = {"Código Barras", "Descripción", "Unidad", "Cantidad", "Precio Unit.", "IGV", "Subtotal", "Multiplo", "Id_Prod"};

            // 3. Sobrescribir DefaultTableModel para controlar la edición de celdas
            modeloTabla = new DefaultTableModel(null, columnas) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    // Solo se permite editar Código de Barras (0) y Cantidad (3)
                    return column == 0 || column == 3;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    // Definir tipos de datos para correcto alineado y ordenamiento
                    switch (columnIndex) {
                        case 3:
                            return Integer.class; // Cantidad
                        case 4:
                            return Double.class;  // Precio
                        case 5:
                            return Double.class;  // IGV
                        case 6:
                            return Double.class;  // Subtotal
                        default:
                            return String.class;
                    }
                }
            };

            // 4. Asignar el modelo a tu JTable (reemplaza 'jTableVentas' por el nombre de tu variable)
            tblVenta.setModel(modeloTabla);

            // 5. Ocultar la columna 'Id_Prod' (columna index 7) para que no la vea el usuario, pero guarde el ID
            tblVenta.getColumnModel().getColumn(7).setMinWidth(0);
            tblVenta.getColumnModel().getColumn(7).setMaxWidth(0);
            tblVenta.getColumnModel().getColumn(7).setPreferredWidth(0);

            tblVenta.getColumnModel().getColumn(8).setMinWidth(0);
            tblVenta.getColumnModel().getColumn(8).setMaxWidth(0);
            tblVenta.getColumnModel().getColumn(8).setPreferredWidth(0);

            // 6. Optimización para Lectores de Códigos de Barras
            // Hace que la celda guarde el valor inmediatamente al presionar ENTER o cambiar de celda
            tblVenta.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
            tblVenta.setSurrendersFocusOnKeystroke(true);

            // 7. Agregar una fila inicial vacía para empezar a escanear
            modeloTabla.addRow(new Object[]{"", "", "", 0, 0.0, 0.0, 0.0, 1, ""});

            // 8. Agregar el Listener para detectar cuando se digita el código o cambia la cantidad
            agregarListenerTabla();
        }

        private void agregarListenerTabla() {
            modeloTabla.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    // Evitar procesar si la actualización viene de la estructura de la tabla
                    if (e.getType() != TableModelEvent.UPDATE) {
                        return;
                    }

                    int fila = e.getFirstRow();
                    int columna = e.getColumn();
                    ProductoDAO prodDAO = new ProductoDAO(); // Tu clase de conexión

                    // Removemos el listener temporalmente para evitar bucles infinitos al editar celdas mediante código
                    modeloTabla.removeTableModelListener(this);

                    try {
                        // ESCENARIO A: Se digitó o escaneó un Código de Barras (Columna 0)
                        if (columna == 0) {
                            String codigo = (String) modeloTabla.getValueAt(fila, 0);

                            if (codigo != null && !codigo.trim().isEmpty()) {
                                List<Object[]> presentaciones = prodDAO.listarPresentacionesPorCodigoBarras(codigo.trim());

                                if (presentaciones.isEmpty()) {
                                    javax.swing.JOptionPane.showMessageDialog(null, "El código de barras no existe.");
                                    modeloTabla.setValueAt("", fila, 0); // Limpiar celda errónea
                                } else {
                                    Object[] presSeleccionada = presentaciones.size() == 1
                                            ? presentaciones.get(0)
                                            : seleccionarPresentacion(presentaciones);

                                    if (presSeleccionada == null) {
                                        // El usuario canceló la selección de presentación
                                        modeloTabla.setValueAt("", fila, 0);
                                    } else {
                                        int idProducto = (int) presSeleccionada[0];
                                        String descripcion = (String) presSeleccionada[1];
                                        String unidad = (String) presSeleccionada[3];
                                        int multiplo = (int) presSeleccionada[4];
                                        double precio = (double) presSeleccionada[5];
                                        boolean aplicaIgv = (boolean) presSeleccionada[6];
                                        int cantidad = 1;

                                        // Calcular IGV (Perú 18%) y Subtotal del ítem
                                        double igvPorcentaje = aplicaIgv ? 0.18 : 0.0;
                                        double precioUnitarioBase = precio / (1 + igvPorcentaje);
                                        double igvCalculado = precio - precioUnitarioBase;
                                        double subtotal = cantidad * precio;

                                        // Insertar los datos en la fila actual
                                        modeloTabla.setValueAt(descripcion, fila, 1);
                                        modeloTabla.setValueAt(unidad, fila, 2);
                                        modeloTabla.setValueAt(cantidad, fila, 3);
                                        modeloTabla.setValueAt(precio, fila, 4);
                                        modeloTabla.setValueAt(igvCalculado * cantidad, fila, 5);
                                        modeloTabla.setValueAt(subtotal, fila, 6);
                                        modeloTabla.setValueAt(multiplo, fila, 7);
                                        modeloTabla.setValueAt(idProducto, fila, 8);

                                        if (fila == modeloTabla.getRowCount() - 1) {
                                            modeloTabla.addRow(new Object[]{"", "", "", 0, 0.0, 0.0, 0.0, 1, ""});
                                        }
                                    }
                                }
                            }
                        } else if (columna == 3) {
                            Integer cantidad = (Integer) modeloTabla.getValueAt(fila, 3);
                            Double precio = (Double) modeloTabla.getValueAt(fila, 4);
                            Integer idProd = (Integer) modeloTabla.getValueAt(fila, 8);

                            if (idProd != null && cantidad != null && cantidad > 0) {
                                double subtotal = cantidad * precio;

                                double subtotalFiltrado = subtotal;

                                modeloTabla.setValueAt(subtotalFiltrado, fila, 6);
                            } else if (cantidad != null && cantidad <= 0) {
                                javax.swing.JOptionPane.showMessageDialog(null, "La cantidad debe ser mayor a 0.");
                                modeloTabla.setValueAt(1, fila, 3);
                            }
                        }

                    } catch (Exception ex) {
                        System.out.println("Error en eventos de la tabla: " + ex.getMessage());
                    } finally {
                        // Volver a activar el Listener pase lo que pase
                        modeloTabla.addTableModelListener(this);

                        // Método global tuyo para sumar toda la columna 'Subtotal' y mostrarlo en tus JLabels/JTextFields del panel
                        calcularTotalesPanel();
                    }
                }
            });
        }

        private Object[] seleccionarPresentacion(List<Object[]> presentaciones) {
            String[] opciones = new String[presentaciones.size()];
            for (int i = 0; i < presentaciones.size(); i++) {
                Object[] p = presentaciones.get(i);
                String unidad = (String) p[3];
                int multiplo = (int) p[4];
                double precio = (double) p[5];
                opciones[i] = String.format("%s (x%d) - S/ %.2f", unidad, multiplo, precio);
            }

            String seleccion = (String) javax.swing.JOptionPane.showInputDialog(
                    null,
                    "Este producto tiene varias presentaciones. Seleccione una:",
                    "Seleccionar presentación",
                    javax.swing.JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[0]
            );

            if (seleccion == null) {
                return null;
            }

            int indice = java.util.Arrays.asList(opciones).indexOf(seleccion);
            return presentaciones.get(indice);
        }

        private void calcularTotalesPanel() {
            double totalGeneral = 0.0;
            double totalIgv = 0.0;
            double netoGeneral = 0.0;

            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                Object subtotalObj = modeloTabla.getValueAt(i, 6);
                Object igvObj = modeloTabla.getValueAt(i, 5);

                if (subtotalObj != null && (double) subtotalObj > 0) {
                    totalGeneral += (double) subtotalObj;
                    totalIgv += (double) igvObj;
                }
            }

            netoGeneral = totalGeneral - totalIgv;

            // Setear valores en tus JTextFields de la interfaz gráfica formateados a 2 decimales
            txtNeto.setText(String.format("%.2f", netoGeneral));
            txtIgvTotal.setText(String.format("%.2f", totalIgv));
            txtTotalVenta.setText(String.format("%.2f", totalGeneral));
        }

        //TIPO COMPROVANTE DE VENTA
        private void configurarEventoTipoComprobante() {
            cboMetodoPago1.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    actualizarSiguienteNumeroEnPantalla();
                }
            });
        }

        private void actualizarSiguienteNumeroEnPantalla() {
            VentaDAO ventaDAO = new VentaDAO();
            String tipoComprobante = obtenerTipoComprobanteSeleccionado();
            String siguienteNumero = ventaDAO.generarSiguienteNumeroComprobante(tipoComprobante);
            txtNumeroBoleta.setText(siguienteNumero);
        }

        private String obtenerTipoComprobanteSeleccionado() {
            if (cboMetodoPago1.getSelectedItem() == null) {
                return "BOLETA";
            }
            return cboMetodoPago1.getSelectedItem().toString().trim();
        }

        //CAJERO
        private void mostrarCajeroLogueado() {
            // Validamos que la sesión no esté vacía
            if (Sesion.nombreUsuario != null && !Sesion.nombreUsuario.isEmpty()) {

                tcajero.setText(Sesion.nombreUsuario);

                tcajero.setEditable(false);
            } else {
                tcajero.setText("SIN CAJERO ACTIVO");
            }
        }

        //CLIENTES
        private void configurarEventoDni() {
            // Evento cuando el usuario presiona ENTER en el campo DNI
            txtdni.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    buscarORegistrarCliente();
                }
            });

            // Evento opcional: cuando el usuario cambia de casilla (pierde el foco)
            txtdni.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent evt) {
                    buscarORegistrarCliente();
                }
            });
        }

        private void buscarORegistrarCliente() {
            String dni = txtdni.getText().trim();

            // Validar que el DNI tenga una longitud correcta (Ej: 8 dígitos en Perú)
            if (dni.isEmpty() || dni.length() != 8) {
                return;
            }

            // Instanciamos el DAO de clientes (Debes tener uno similar a ProductoDAO)
            dao.ClienteDAO clienteDAO = new dao.ClienteDAO();
            Object[] cliente = clienteDAO.buscarPorDni(dni);

            if (cliente != null) {
                idClienteSeleccionado = (int) cliente[0]; // Guardamos el ID oculto
                String nombreCompleto = (String) cliente[1];
                String celular = (String) cliente[2];

                txtcliente.setText(nombreCompleto);
                txtelef.setText(celular);
            } else {
                // Si NO existe, abrimos la ventana emergente
                int respuesta = javax.swing.JOptionPane.showConfirmDialog(
                        this,
                        "El cliente con DNI " + dni + " no está registrado.\n¿Desea registrarlo ahora?",
                        "Cliente no encontrado",
                        javax.swing.JOptionPane.YES_NO_OPTION,
                        javax.swing.JOptionPane.QUESTION_MESSAGE
                );

                if (respuesta == javax.swing.JOptionPane.YES_OPTION) {
                    // Abrir JDialog emergente de forma modal (bloquea la ventana de atrás hasta que termine)
                    FormularioClienteDialog dialog = new FormularioClienteDialog(null, true, dni);
                    dialog.setVisible(true);

                    // Al cerrar el diálogo, volvemos a buscar para cargar los datos recién creados
                    buscarORegistrarCliente();
                } else {
                    // Si cancela, limpiamos el DNI para evitar errores
                    txtdni.setText("");
                    txtcliente.setText("");
                    txtelef.setText("");
                }
            }
        }

        private void aplicarModo(int modo) {
            modoActual = modo;
            switch (modo) {
                case 0: // NUEVA VENTA
                    btncobrar.setEnabled(true);
                    btnbuscar.setEnabled(true);
                    btnsalir.setEnabled(true);
                    setFormularioEditable(true);
                    break;
                case 1: // LECTURA (venta consultada, solo ver)
                    btncobrar.setEnabled(false);
                    btnbuscar.setEnabled(true);
                    btnsalir.setEnabled(true);
                    setFormularioEditable(false);
                    break;
            }
        }

        private void setFormularioEditable(boolean editable) {
            txtdni.setEditable(editable);
            txtcliente.setEditable(editable);
            txtelef.setEditable(editable);
            cboMetodoPago.setEnabled(editable);
            cboMetodoPago1.setEnabled(editable);
            tblVenta.setEnabled(editable);
        }

        //LIMPIAR
        private void limpiarFormularioVenta() {
            txtNeto.setText("0.00");
            txtIgvTotal.setText("0.00");
            txtTotalVenta.setText("0.00");
            txtdni.setText("");
            txtcliente.setText("");
            txtelef.setText("");

            this.idClienteSeleccionado = null;
            this.idClienteActual = 0;

            while (modeloTabla.getRowCount() > 0) {
                modeloTabla.removeRow(0);
            }
            modeloTabla.addRow(new Object[]{"", "", "", 0, 0.0, 0.0, 0.0, ""});
        }

        /**
         * This method is called from within the constructor to initialize the
         * form. WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
        @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtNumeroBoleta = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cboMetodoPago = new javax.swing.JComboBox<>();
        cboMetodoPago1 = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtdni = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtcliente = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtelef = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        tcajero = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblVenta = new javax.swing.JTable();
        btneliminar = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        btnbuscar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        btncobrar = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        btnmostrar = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        btnsalir = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtNeto = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtIgvTotal = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtTotalVenta = new javax.swing.JTextField();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DATOS PRINCIPALES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel1.setText("DOCUMENTO");

        txtNumeroBoleta.setEditable(false);
        txtNumeroBoleta.setBackground(new java.awt.Color(204, 204, 204));
        txtNumeroBoleta.setText("B001");
        txtNumeroBoleta.setBorder(null);

        jLabel3.setText("NUMERO");

        jLabel6.setText("FORMA DE PAGO");

        cboMetodoPago.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "EFECTIVO", "BILLETERA DIGITAL" }));

        cboMetodoPago1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BOLETA", "FACTURA", "NOTA DE VENTA" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1))
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cboMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboMetodoPago1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNumeroBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(52, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cboMetodoPago1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtNumeroBoleta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cboMetodoPago, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DATOS DEL CLIENTE", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel7.setText("DNI");

        jLabel8.setText("CLIENTE");

        jLabel9.setText("TELEFONO");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(txtdni, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtcliente, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addGap(18, 18, 18)
                        .addComponent(txtelef, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtdni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtcliente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtelef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DATOS DEL PERSONAL", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N

        jLabel10.setText("CAJERO");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(tcajero, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(172, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(tcajero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        tblVenta.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblVenta);

        btneliminar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/menos.png"))); // NOI18N
        btneliminar.setBorder(null);
        btneliminar.setBorderPainted(false);
        btneliminar.setContentAreaFilled(false);
        btneliminar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btneliminar.setFocusPainted(false);
        btneliminar.addActionListener(this::btneliminarActionPerformed);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        btnbuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/buscarprod.png"))); // NOI18N
        btnbuscar.setBorder(null);
        btnbuscar.setBorderPainted(false);
        btnbuscar.setContentAreaFilled(false);
        btnbuscar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnbuscar.setFocusPainted(false);
        btnbuscar.addActionListener(this::btnbuscarActionPerformed);

        jLabel4.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel4.setText("BUSCAR PRODUCTO");

        btncobrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/guardar.png"))); // NOI18N
        btncobrar.setBorder(null);
        btncobrar.setBorderPainted(false);
        btncobrar.setContentAreaFilled(false);
        btncobrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btncobrar.setFocusPainted(false);
        btncobrar.addActionListener(this::btncobrarActionPerformed);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel12.setText("GUARDAR");

        btnmostrar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/busdocu.png"))); // NOI18N
        btnmostrar.setBorder(null);
        btnmostrar.setBorderPainted(false);
        btnmostrar.setContentAreaFilled(false);
        btnmostrar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnmostrar.setFocusPainted(false);
        btnmostrar.addActionListener(this::btnmostrarActionPerformed);

        jLabel13.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jLabel13.setText("MOSTRAR VENTAS");

        btnsalir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/IMAGENES/salir.png"))); // NOI18N
        btnsalir.setText("SALIR");
        btnsalir.setBorder(null);
        btnsalir.setBorderPainted(false);
        btnsalir.setContentAreaFilled(false);
        btnsalir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnsalir.setFocusPainted(false);
        btnsalir.addActionListener(this::btnsalirActionPerformed);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(btnbuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(74, 74, 74)
                        .addComponent(jLabel4)))
                .addGap(113, 113, 113)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel12))
                    .addComponent(btncobrar, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 154, Short.MAX_VALUE)
                        .addComponent(btnmostrar, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(117, 117, 117)
                        .addComponent(btnsalir, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(101, 101, 101))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addComponent(jLabel13)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(btncobrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel12))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(btnsalir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel5Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(btnbuscar, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)))
                .addContainerGap(11, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnmostrar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel13)
                .addContainerGap())
        );

        jLabel2.setText("TOTAL NETO");

        jLabel5.setText("IGV");

        jLabel11.setText("TOTAL");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(txtIgvTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2)
                    .addComponent(txtNeto, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNeto, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtIgvTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(jLabel11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtTotalVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(27, 27, 27))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 740, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 50, Short.MAX_VALUE)
                                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btneliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))))))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btneliminar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(46, 46, 46)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(123, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void btncobrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncobrarActionPerformed
        VentaDAO ventaDAO = new VentaDAO();

        // 1. Validar si hay una caja abierta antes de vender
        int idCajaActiva = ventaDAO.obtenerIdCajaAbierta();
        if (idCajaActiva == 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "No puede cobrar. Debe realizar primero la APERTURA DE CAJA.", "Caja Cerrada", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. Validar que la tabla tenga al menos un producto válido
        boolean hayProductoValido = false;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Object idProdObj = modeloTabla.getValueAt(i, 8);
            Object cantObj = modeloTabla.getValueAt(i, 3);

            if (idProdObj != null && !idProdObj.toString().trim().isEmpty()) {
                int cant = (cantObj != null) ? Integer.parseInt(cantObj.toString()) : 0;
                if (cant > 0) {
                    hayProductoValido = true;
                    break;
                }
            }
        }

        if (!hayProductoValido) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe seleccionar al menos un producto",
                    "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 3. Capturar datos de la cabecera de la interfaz
            String tipoComprobante = obtenerTipoComprobanteSeleccionado();
            String metodoPago = cboMetodoPago.getSelectedItem().toString();
            double totalVenta = Double.parseDouble(txtTotalVenta.getText().replace(",", "."));

            // Generar número de comprobante automático desde el DAO para evitar duplicados
            String numeroComp = ventaDAO.generarSiguienteNumeroComprobante(tipoComprobante);

            int idUsuarioLogueado = Sesion.idUsuario;

            VentaDTO nuevaVenta = new VentaDTO();
            nuevaVenta.setIdCaja(idCajaActiva);
            nuevaVenta.setIdUsuario(idUsuarioLogueado);

            // ASIGNACIÓN CRÍTICA: Forzamos la lectura de la variable de clase
            nuevaVenta.setIdCliente(this.idClienteSeleccionado);

            nuevaVenta.setTipoComprobante(tipoComprobante);
            nuevaVenta.setNumeroComprobante(numeroComp);
            nuevaVenta.setTotal(totalVenta);
            nuevaVenta.setMetodoPago(metodoPago);

            // 4. Capturar los detalles recorriendo las filas de la JTable
            ArrayList<DetalleVentaDTO> listaDetalles = new ArrayList<>();

            for (int i = 0; i < tblVenta.getRowCount(); i++) {
                Object idProdObj = tblVenta.getValueAt(i, 8);
                if (idProdObj != null && !idProdObj.toString().trim().isEmpty()) {

                    int idProducto = Integer.parseInt(idProdObj.toString());
                    int cantidad = Integer.parseInt(tblVenta.getValueAt(i, 3).toString());
                    double precioUnit = Double.parseDouble(tblVenta.getValueAt(i, 4).toString());
                    int multiplo = Integer.parseInt(tblVenta.getValueAt(i, 7).toString());

                    DetalleVentaDTO detalle = new DetalleVentaDTO();
                    detalle.setIdProducto(idProducto);
                    detalle.setCantidad(cantidad);
                    detalle.setPrecioUnitario(precioUnit);
                    detalle.setCantidadStock(cantidad * multiplo);
                    listaDetalles.add(detalle);
                }
            }

            // 5. Enviar a la base de datos a través del DAO
            boolean exito = ventaDAO.registrarVentaCompleta(nuevaVenta, listaDetalles);

            if (exito) {
                javax.swing.JOptionPane.showMessageDialog(this, "¡Venta registrada con éxito!\nComprobante N°: " + numeroComp, "Sistema de Ventas", javax.swing.JOptionPane.INFORMATION_MESSAGE);

                // 6. Limpiar la interfaz para la siguiente venta y actualizar el correlativo visual
                limpiarFormularioVenta();
                actualizarSiguienteNumeroEnPantalla();
                aplicarModo(0);

            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Hubo un error interno. La venta no fue procesada.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Error de datos: " + e.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btncobrarActionPerformed

    private void btnbuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnbuscarActionPerformed
        // 1. Abrir el diálogo
        JDproducto dialog = new JDproducto(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setVisible(true);
        String codigo = dialog.getCodigoBarrasSeleccionado();
        if (codigo == null) {
            return;
        }

        // 2. Encontrar la última fila vacía de la tabla de ventas
        int filaDestino = -1;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            Object val = modeloTabla.getValueAt(i, 0);
            if (val == null || val.toString().trim().isEmpty()) {
                filaDestino = i;
                break;
            }
        }
        if (filaDestino == -1) {
            // Si no hay fila vacía, agrega una nueva
            modeloTabla.addRow(new Object[]{"", "", "", 0, 0.0, 0.0, 0.0, ""});
            filaDestino = modeloTabla.getRowCount() - 1;
        }

        // 4. Escribir el código en la celda — el TableModelListener 
        //    existente lo detecta y completa el resto automáticamente
        modeloTabla.setValueAt(codigo, filaDestino, 0);
    }//GEN-LAST:event_btnbuscarActionPerformed

    private void btnmostrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnmostrarActionPerformed
        // 1. Abrir el diálogo de ventas
        JDventas dialog = new JDventas(
                (java.awt.Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setVisible(true);
    }//GEN-LAST:event_btnmostrarActionPerformed

    private void btneliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btneliminarActionPerformed
        int filaSeleccionada = tblVenta.getSelectedRow();

        if (filaSeleccionada == -1) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Seleccione una fila para eliminar.",
                    "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // No permitir eliminar la fila vacía final
        Object idProd = modeloTabla.getValueAt(filaSeleccionada, 8);
        if (idProd == null || idProd.toString().trim().isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No puede eliminar una fila vacía.",
                    "Aviso", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmar = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Desea eliminar el producto: " + modeloTabla.getValueAt(filaSeleccionada, 1) + "?",
                "Confirmar eliminación",
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (confirmar == javax.swing.JOptionPane.YES_OPTION) {
            modeloTabla.removeRow(filaSeleccionada);
            calcularTotalesPanel();
        }
    }//GEN-LAST:event_btneliminarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnbuscar;
    private javax.swing.JButton btncobrar;
    private javax.swing.JButton btneliminar;
    private javax.swing.JButton btnmostrar;
    private javax.swing.JButton btnsalir;
    private javax.swing.JComboBox<String> cboMetodoPago;
    private javax.swing.JComboBox<String> cboMetodoPago1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblVenta;
    private javax.swing.JTextField tcajero;
    private javax.swing.JTextField txtIgvTotal;
    private javax.swing.JTextField txtNeto;
    private javax.swing.JTextField txtNumeroBoleta;
    private javax.swing.JTextField txtTotalVenta;
    private javax.swing.JTextField txtcliente;
    private javax.swing.JTextField txtdni;
    private javax.swing.JTextField txtelef;
    // End of variables declaration//GEN-END:variables
}
