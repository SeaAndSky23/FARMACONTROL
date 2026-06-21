/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DTO;

import java.util.Date;

/**
 *
 * @author USUARIO
 */
public class ProductoDTO {

    private int idProducto;
    private String codigoBarras;
    private String descripcion;
    private int idMarca;
    private int idPrincipio;
    private int idConcentracion;
    private int idForma;
    private Date fechaVencimiento;
    private int idCondicion;

    public ProductoDTO() {
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(int idMarca) {
        this.idMarca = idMarca;
    }

    public int getIdPrincipio() {
        return idPrincipio;
    }

    public void setIdPrincipio(int idPrincipio) {
        this.idPrincipio = idPrincipio;
    }

    public int getIdConcentracion() {
        return idConcentracion;
    }

    public void setIdConcentracion(int idConcentracion) {
        this.idConcentracion = idConcentracion;
    }

    public int getIdForma() {
        return idForma;
    }

    public void setIdForma(int idForma) {
        this.idForma = idForma;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public int getIdCondicion() {
        return idCondicion;
    }

    public void setIdCondicion(int idCondicion) {
        this.idCondicion = idCondicion;
    }
}
