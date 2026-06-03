/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USUARIO
 */
public class Trabajador {

   private int idTrabajador;
    private String nombres;
    private String apPaterno;
    private String apMaterno;
    private String DNI;
    private String Telefono;
    private String Direccion;

    // 2. Constructor actualizado para recibir los campos por separado

    public Trabajador(int idTrabajador, String nombres, String apPaterno, String apMaterno, String DNI, String Telefono, String Direccion) {
        this.idTrabajador = idTrabajador;
        this.nombres = nombres;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
        this.DNI = DNI;
        this.Telefono = Telefono;
        this.Direccion = Direccion;
    }

    // Constructor vacío por si lo necesitas en el DAO
    public Trabajador() {
    }

    // 3. Métodos Getters y Setters
    public int getIdTrabajador() {
        return idTrabajador;
    }

    public void setIdTrabajador(int idTrabajador) {
        this.idTrabajador = idTrabajador;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApPaterno() {
        return apPaterno;
    }

    public void setApPaterno(String apPaterno) {
        this.apPaterno = apPaterno;
    }

    public String getApMaterno() {
        return apMaterno;
    }

    public void setApMaterno(String apMaterno) {
        this.apMaterno = apMaterno;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }

    public String getTelefono() {
        return Telefono;
    }

    public void setTelefono(String Telefono) {
        this.Telefono = Telefono;
    }

    public String getDireccion() {
        return Direccion;
    }

    public void setDireccion(String Direccion) {
        this.Direccion = Direccion;
    }

    // 4. Ahora el toString() funcionará perfectamente sin errores
    @Override
    public String toString() {
        return this.apPaterno + " " + this.apMaterno + ", " + this.nombres;
    }
}

