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

    // 2. Constructor actualizado para recibir los campos por separado
    public Trabajador(int idTrabajador, String nombres, String apPaterno, String apMaterno) {
        this.idTrabajador = idTrabajador;
        this.nombres = nombres;
        this.apPaterno = apPaterno;
        this.apMaterno = apMaterno;
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

    // 4. Ahora el toString() funcionará perfectamente sin errores
    @Override
    public String toString() {
        return this.apPaterno + " " + this.apMaterno + ", " + this.nombres;
    }
}

