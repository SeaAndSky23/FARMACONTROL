/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import conexion.ConexioDB;
import modelo.Ubigeo;
import java.sql.*;
import java.util.ArrayList;
import modelo.Trabajador;

/**
 *
 * @author USUARIO
 */
public class TrabajadorDAO {

    // 1. Obtener Departamentos únicos
    public ArrayList<String> obtenerDepartamentos() {
        ArrayList<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT Departamento FROM UBIGEO ORDER BY Departamento";
        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(rs.getString("Departamento"));
            }
        } catch (SQLException e) {
            System.out.println("Error Departamentos: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<String> obtenerProvincias(String departamento) {
        ArrayList<String> lista = new ArrayList<>();
        String sql = "SELECT DISTINCT Provincia FROM UBIGEO WHERE Departamento = ? ORDER BY Provincia";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, departamento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("Provincia"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error Provincias: " + e.getMessage());
        }
        return lista;
    }

    public ArrayList<Ubigeo> obtenerDistritos(String departamento, String provincia) {
        ArrayList<Ubigeo> lista = new ArrayList<>();
        String sql = "SELECT Codigo_ubigeo, Distrito FROM UBIGEO WHERE Departamento = ? AND Provincia = ? ORDER BY Distrito";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, departamento);
            ps.setString(2, provincia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Ubigeo(rs.getString("Codigo_ubigeo"), departamento, provincia, rs.getString("Distrito")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error Distritos: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarTrabajador(String dni, String nombres, String apPaterno, String apMaterno, String telefono, String direccion, String codigoUbigeo) {
        String sql = "INSERT INTO TRABAJADORES (DNI, NOMBRES, AP_PATERNO, AP_MATERNO, TELEFONO, DIRECCION, Codigo_ubigeo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConexioDB.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, dni);
            ps.setString(2, nombres);
            ps.setString(3, apPaterno);
            ps.setString(4, apMaterno);
            ps.setString(5, telefono);
            ps.setString(6, direccion);
            ps.setString(7, codigoUbigeo);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error al registrar trabajador: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<Trabajador> listarTrabajadores() {
        ArrayList<Trabajador> lista = new ArrayList<>();
        // Añadimos DNI, TELEFONO y DIRECCION a la consulta SQL
        String sql = "SELECT id_trabajador, DNI, NOMBRES, AP_PATERNO, AP_MATERNO, TELEFONO, DIRECCION FROM TRABAJADORES ORDER BY AP_PATERNO ASC";

        try (Connection con = ConexioDB.getConexion(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Trabajador t = new Trabajador();
                t.setIdTrabajador(rs.getInt("id_trabajador"));
                t.setNombres(rs.getString("NOMBRES"));
                t.setApPaterno(rs.getString("AP_PATERNO"));
                t.setApMaterno(rs.getString("AP_MATERNO"));

                // Pasamos los nuevos datos de la BD al objeto (Asegúrate de tener estos setters en tu clase Trabajador)
                t.setDNI(rs.getString("DNI"));
                t.setTelefono(rs.getString("TELEFONO"));
                t.setDireccion(rs.getString("DIRECCION"));

                lista.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar trabajadores: " + e.getMessage());
        }
        return lista;
    }
}
