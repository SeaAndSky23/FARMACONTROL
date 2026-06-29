/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package GUI;

import dao.CajaDAO;
import modelo.Caja;
import modelo.Sesion;
import java.text.SimpleDateFormat;

public class pcierre extends javax.swing.JPanel {

    private CajaDAO cajaDAO = new CajaDAO();
    private Caja cajaActiva = null;

    public pcierre() {
        initComponents();
        cargarDatosCierre();
    }

    private void cargarDatosCierre() {
        int idUsuario = Sesion.getIdUsuario();
        cajaActiva = cajaDAO.obtenerCajaActiva(idUsuario);

        if (cajaActiva == null) {
            // Mostrar en el panel, NO con JOptionPane
            txtcodape.setText("—");
            txtusua.setText(Sesion.getNombreUsuario());
            txtfeape.setText("—");
            txthora.setText("—");
            txtefectiv.setText("0.00");
            txtbilldig.setText("0.00");
            txtventas.setText("0.00");
            txtmontoap.setText("0.00");
            txtefec.setText("0.00");
            txttoefe.setText("0.00");
            configurarCuadreCaja();
            btncerrarcaja.setEnabled(false);
            return;
        }

        // ── PANEL: DATOS DE APERTURA ──
        txtcodape.setText(String.valueOf(cajaActiva.getIdCaja()));   // COD_APERTURA

        txtusua.setText(Sesion.getNombreUsuario());                // USUARIO

        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");
        txtfeape.setText(sdfFecha.format(cajaActiva.getFechaApertura())); // FECHA
        txthora.setText(sdfHora.format(cajaActiva.getFechaApertura()));  // HORA

        // ── PANEL: VENTAS ──
        int idCaja = cajaActiva.getIdCaja();
        double totalEfectivo = cajaDAO.obtenerTotalEfectivoPorCaja(idCaja);
        double totalBilletera = cajaDAO.obtenerTotalBilleteraPorCaja(idCaja);
        double totalVentas = totalEfectivo + totalBilletera;

        txtefectiv.setText(String.format("%.2f", totalEfectivo));   // EFECTIVO
        txtbilldig.setText(String.format("%.2f", totalBilletera));  // BILLETERA DIGITAL
        txtventas.setText(String.format("%.2f", totalVentas));     // TOTAL VENTAS

        // ── PANEL: CUADRAR CAJA ──
        double montoApertura = cajaActiva.getMontoApertura();
        double totalEfectivoFisico = montoApertura + totalEfectivo;
        //  ^^ Lo que DEBE haber físicamente en caja:
        //     dinero inicial que puso el cajero + todo lo cobrado en efectivo

        txtmontoap.setText(String.format("%.2f", montoApertura));          // MONTO APERTURA
        txtefec.setText(String.format("%.2f", totalEfectivo));        // EFECTIVO (ventas)
        txttoefe.setText(String.format("%.2f", totalEfectivoFisico));  // TOTAL EFECTIVO FÍSICO
        configurarCuadreCaja();
    }

    private void configurarCuadreCaja() {
        // Limpiar campo editable
        txtMontoContado.setText("");
        txtDiferencia.setText("0.00");

        // Listener en tiempo real mientras el cajero escribe
        txtMontoContado.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                calcularDiferencia();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                calcularDiferencia();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                calcularDiferencia();
            }
        }
        );
    }

    private void calcularDiferencia() {
        try {
            String texto = txtMontoContado.getText().trim();
            if (texto.isEmpty()) {
                txtDiferencia.setText("0.00");
                txtDiferencia.setForeground(java.awt.Color.BLACK);
                return;
            }

            double montoContado = Double.parseDouble(texto.replace(",", "."));
            double efectivoFisico = Double.parseDouble(txttoefe.getText().replace(",", "."));
            double diferencia = montoContado - efectivoFisico;

            txtDiferencia.setText(String.format("%.2f", diferencia));

            // Verde si cuadra o sobra, rojo si falta
            if (diferencia > 0) {
                txtDiferencia.setForeground(new java.awt.Color(255, 140, 0)); 
            } else if (diferencia < 0) {
                txtDiferencia.setForeground(java.awt.Color.RED);              
            } else {
                txtDiferencia.setForeground(new java.awt.Color(0, 150, 0));   
            }

        } catch (NumberFormatException ex) {
            txtDiferencia.setText("0.00");
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
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtusua = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtcodape = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtfeape = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txthora = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        txtefectiv = new javax.swing.JTextField();
        txtbilldig = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtventas = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        txtmontoap = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        txtefec = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        txttoefe = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        txtMontoContado = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        txtDiferencia = new javax.swing.JTextField();
        btncerrarcaja = new javax.swing.JButton();
        btnsalir = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(227, 234, 245));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        jLabel1.setText("CIERRE DE CAJA");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addContainerGap(701, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DATOS DE APERTURA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14))); // NOI18N

        jLabel4.setText("USUARIO");

        txtusua.setEnabled(false);

        jLabel2.setText("COD_APERTURA");

        txtcodape.setEnabled(false);

        jLabel6.setText("FECHA APERTURA");

        txtfeape.setEnabled(false);

        jLabel7.setText("HORA");

        txthora.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtusua, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addGap(26, 26, 26)
                        .addComponent(txthora, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtcodape, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(36, 36, 36)
                        .addComponent(jLabel6)
                        .addGap(26, 26, 26)
                        .addComponent(txtfeape, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtcodape, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtfeape, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtusua, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txthora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "VENTAS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14))); // NOI18N

        txtefectiv.setEnabled(false);

        txtbilldig.setEnabled(false);

        jLabel11.setText("EFECTIVO");

        jLabel12.setText("BILLETERA DIGITAL");

        jLabel16.setText("TOTAL VENTAS");

        txtventas.setEnabled(false);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtbilldig, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtefectiv, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtventas, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtefectiv, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(26, 26, 26)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtbilldig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtventas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(45, 45, 45))
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "CUADRAR CAJA", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Arial", 1, 14))); // NOI18N

        jLabel20.setText("MONTO APERTURA");

        txtmontoap.setEnabled(false);

        jLabel21.setText("EFECTIVO");

        txtefec.setEnabled(false);

        jLabel22.setText("TOTAL EFECTIVO");

        txttoefe.setEnabled(false);

        jLabel23.setText("MONTO CONTADO");

        txtMontoContado.setBackground(new java.awt.Color(255, 255, 204));
        txtMontoContado.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N

        jLabel24.setText("DIFERENCIA");

        txtDiferencia.setEditable(false);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addComponent(txtDiferencia))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel21)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtefec, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel6Layout.createSequentialGroup()
                                    .addComponent(jLabel20)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtmontoap, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel22)
                                    .addComponent(jLabel23)
                                    .addComponent(jLabel24))
                                .addGap(27, 27, 27)
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txttoefe, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                    .addComponent(txtMontoContado))))))
                .addContainerGap(32, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtmontoap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtefec, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txttoefe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(txtMontoContado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtDiferencia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addContainerGap(9, Short.MAX_VALUE))
        );

        btncerrarcaja.setText("CERRRAR CAJA");
        btncerrarcaja.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btncerrarcaja.addActionListener(this::btncerrarcajaActionPerformed);

        btnsalir.setText("SALIR");
        btnsalir.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnsalir.addActionListener(this::btnsalirActionPerformed);

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 771, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 70, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(120, 120, 120)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btncerrarcaja)
                        .addGap(38, 38, 38)
                        .addComponent(btnsalir)
                        .addGap(153, 153, 153))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btncerrarcaja)
                    .addComponent(btnsalir))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 45, Short.MAX_VALUE))
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

    private void btncerrarcajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btncerrarcajaActionPerformed
        if (cajaActiva == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "No hay caja activa para cerrar.", "Aviso",
                    javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar que el cajero haya ingresado el monto contado
        String textoContado = txtMontoContado.getText().trim();
        if (textoContado.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Debe ingresar el MONTO CONTADO antes de cerrar la caja.",
                    "Campo requerido", javax.swing.JOptionPane.WARNING_MESSAGE);
            txtMontoContado.requestFocus();
            return;
        }

        double montoContado;
        try {
            montoContado = Double.parseDouble(textoContado.replace(",", "."));
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "El monto contado no es válido.", "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        String textoDif = txtDiferencia.getText().trim();
        double diferencia = (textoDif.isEmpty() || textoDif.equals("—"))
                ? (montoContado - Double.parseDouble(txttoefe.getText().replace(",", ".")))
                : Double.parseDouble(textoDif.replace(",", "."));

        // Mostrar resumen antes de confirmar
        String mensajeDif = diferencia >= 0
                ? "SOBRANTE: S/ " + String.format("%.2f", diferencia)
                : "FALTANTE: S/ " + String.format("%.2f", Math.abs(diferencia));

        int confirmar = javax.swing.JOptionPane.showConfirmDialog(this,
                "¿Confirma el cierre de caja?\n\n"
                + "Efectivo esperado : S/ " + txttoefe.getText() + "\n"
                + "Monto contado     : S/ " + String.format("%.2f", montoContado) + "\n"
                + mensajeDif,
                "Confirmar cierre", javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        if (confirmar != javax.swing.JOptionPane.YES_OPTION) {
            return;
        }

        double totalEfectivo = Double.parseDouble(txtefectiv.getText().replace(",", "."));
        double totalBilletera = Double.parseDouble(txtbilldig.getText().replace(",", "."));
        double totalVentas = Double.parseDouble(txtventas.getText().replace(",", "."));
        double montoApertura = Double.parseDouble(txtmontoap.getText().replace(",", "."));
        double efectivoFisico = Double.parseDouble(txttoefe.getText().replace(",", "."));
        int idCaja = cajaActiva.getIdCaja();

        boolean resumenGuardado = cajaDAO.guardarResumenCierre(
                idCaja, totalEfectivo, totalBilletera, totalVentas,
                montoApertura, efectivoFisico, montoContado, diferencia
        );

        boolean cajaFirmada = cajaDAO.cerrarCaja(idCaja, montoContado);

        if (resumenGuardado && cajaFirmada) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Caja cerrada correctamente.\n" + mensajeDif,
                    "Cierre exitoso", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            btncerrarcaja.setEnabled(false);
            txtMontoContado.setEditable(false);
        } else {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Error al procesar el cierre. Intente nuevamente.",
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btncerrarcajaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btncerrarcaja;
    private javax.swing.JButton btnsalir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTextField txtDiferencia;
    private javax.swing.JTextField txtMontoContado;
    private javax.swing.JTextField txtbilldig;
    private javax.swing.JTextField txtcodape;
    private javax.swing.JTextField txtefec;
    private javax.swing.JTextField txtefectiv;
    private javax.swing.JTextField txtfeape;
    private javax.swing.JTextField txthora;
    private javax.swing.JTextField txtmontoap;
    private javax.swing.JTextField txttoefe;
    private javax.swing.JTextField txtusua;
    private javax.swing.JTextField txtventas;
    // End of variables declaration//GEN-END:variables
}
