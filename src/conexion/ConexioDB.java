/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author USUARIO
 */
public class ConexioDB {

    private static final String URL = "jdbc:sqlserver://127.0.0.1:1433;"
            + "databaseName=BDFARMACONTROL;"
            + "encrypt=true;"
            + "trustServerCertificate=true;"
            + "loginTimeout=5;";
    private static final String USER = "sa";
    private static final String PASSWORD = "baekseung_mari23";

    public static Connection getConexion() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("🚨 ERROR CRÍTICO EN LA CONEXIÓN DE BASE DE DATOS:");
            e.printStackTrace();
            throw new RuntimeException("No se pudo conectar a la base de datos: " + e.getMessage(), e);
        }
    }
}
