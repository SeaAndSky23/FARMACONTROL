/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USUARIO
 */
public class Sesion {

    public static int idUsuario;
    public static String nombreUsuario;
    private static String nombreRol;

    // Getters y Setters Estáticos
    public static int getIdUsuario() {
        return idUsuario;
    }

    public static void setIdUsuario(int idUsuario) {
        Sesion.idUsuario = idUsuario;
    }

    public static String getNombreUsuario() {
        return nombreUsuario;
    }

    public static void setNombreUsuario(String nombreUsuario) {
        Sesion.nombreUsuario = nombreUsuario;
    }

    public static String getNombreRol() {
        return nombreRol;
    }

    public static void setNombreRol(String nombreRol) {
        Sesion.nombreRol = nombreRol;
    }
}
