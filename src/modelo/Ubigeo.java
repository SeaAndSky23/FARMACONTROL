/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USUARIO
 */
public class Ubigeo {
    private String codigoUbigeo;
    private String departamento;
    private String provincia;
    private String distrito;

    // Constructor completo
    public Ubigeo(String codigoUbigeo, String departamento, String provincia, String distrito) {
        this.codigoUbigeo = codigoUbigeo;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
    }
    public String getCodigoUbigeo() { return codigoUbigeo; }
    public String getDepartamento() { return departamento; }
    public String getProvincia() { return provincia; }
    public String getDistrito() { return distrito; }

    // Este método define qué se muestra en el JComboBox de Distritos
    @Override
    public String toString() {
        return distrito;
    }
}
