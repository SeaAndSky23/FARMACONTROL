/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USUARIO
 */
public class ObjetoCombo {
    private int id;
    private String nombre;

    public ObjetoCombo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    // Este método es CRUCIAL. JComboBox usa toString() para saber qué mostrar en pantalla
    @Override
    public String toString() {
        return this.nombre;
    }
}
