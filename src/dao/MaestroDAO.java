/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import modelo.ObjetoCombo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author USUARIO
 */
public class MaestroDAO {

    public List<ObjetoCombo> listarMaestro(String tabla, String campoId, String campoNombre) {
        List<ObjetoCombo> lista = new ArrayList<>();
        String sql = "SELECT " + campoId + ", " + campoNombre
                + " FROM " + tabla
                + " ORDER BY " + campoNombre + " ASC";

        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new ObjetoCombo(rs.getInt(campoId), rs.getString(campoNombre)));
            }

        } catch (SQLException e) {
            System.out.println("Error al listar maestro [" + tabla + "]: " + e.getMessage());
        }
        return lista;
    }

    public boolean insertarMaestro(String tabla, String columna, String valor) {
        String sql = "INSERT INTO " + tabla + " (" + columna + ") VALUES (?)";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, valor.trim());
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al insertar en [" + tabla + "]: " + e.getMessage());
            return false;
        }
    }
}
