/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 *
 * @author USUARIO
 */
public class PanelTablaUsuarios extends JPanel {

    private JTable tabla;
    private DefaultTableModel modelo;

    public PanelTablaUsuarios() {
        // 1. Definimos el diseño del JPanel y configuramos el modelo de la tabla
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel();
        modelo.addColumn("ID");
        modelo.addColumn("Nombre");
        modelo.addColumn("Correo Electrónico");

        tabla = new JTable(modelo);

        // 2. IMPORTANTE: Envolver la tabla en un JScrollPane
        JScrollPane scrollPane = new JScrollPane(tabla);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Cargar los datos desde la base de datos
        cargarDatosBaseDatos();
    }

    private void cargarDatosBaseDatos() {
        // Reemplaza con tus datos de conexión reales
        String url = "jdbc:mysql://localhost:3306/tu_base_de_datos";
        String usuario = "root";
        String password = "tu_password";
        String query = "SELECT id, nombre, correo FROM usuarios";

        try (Connection con = DriverManager.getConnection(url, usuario, password); PreparedStatement pst = con.prepareStatement(query); ResultSet rs = pst.executeQuery()) {

            // Limpiar el modelo por si ya tenía datos previos
            modelo.setRowCount(0);

            // 4. Recorrer las filas de la base de datos y agregarlas a la JTable
            while (rs.next()) {
                Object[] fila = new Object[3];
                fila[0] = rs.getInt("id");
                fila[1] = rs.getString("nombre");
                fila[2] = rs.getString("correo");

                modelo.addRow(fila);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar datos: " + e.getMessage(),
                    "Error de Conexión", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
