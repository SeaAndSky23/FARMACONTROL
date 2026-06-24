/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author USUARIO
 */
public class PresentacionDTO {

    private int idPresentacion;
    private int idProducto;
    private int idUnidad;
    private String nombreUnidad;
    private int multiplo;
    private boolean aplicaIgv;
    private double precioVenta;
    private double precioCompra;

    public PresentacionDTO() {
    }

    public int getIdPresentacion() {
        return idPresentacion;
    }

    public void setIdPresentacion(int idPresentacion) {
        this.idPresentacion = idPresentacion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public int getIdUnidad() {
        return idUnidad;
    }

    public void setIdUnidad(int idUnidad) {
        this.idUnidad = idUnidad;
    }

    public int getMultiplo() {
        return multiplo;
    }

    public void setMultiplo(int multiplo) {
        this.multiplo = multiplo;
    }

    public boolean isAplicaIgv() {
        return aplicaIgv;
    }

    public void setAplicaIgv(boolean aplicaIgv) {
        this.aplicaIgv = aplicaIgv;
    }

    public double getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(double precioVenta) {
        this.precioVenta = precioVenta;
    }

    public double getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(double precioCompra) {
        this.precioCompra = precioCompra;
    }
}
