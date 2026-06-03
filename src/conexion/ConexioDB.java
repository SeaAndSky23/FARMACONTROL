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
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        //} catch (ClassNotFoundException | SQLException e) {
        } catch (SQLException e) {
            // Esto imprimirá de forma detallada qué está bloqueando el login
            System.err.println("🚨 ERROR CRÍTICO EN LA CONEXIÓN DE BASE DE DATOS:");
            e.printStackTrace();
            return null;
        }
    }
}
