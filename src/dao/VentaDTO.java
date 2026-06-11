/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author USUARIO
 */
public class VentaDTO {

    private int idVenta;
    private int idCaja;
    private int idUsuario;
    private Integer idCliente;
    private String tipoComprobante;
    private String serieComprobante;
    private String numeroComprobante;
    private double total;
    private String metodoPago;

    public VentaDTO() {
    }

    public VentaDTO(int idVenta, int idCaja, int idUsuario, String tipoComprobante, String serieComprobante, String numeroComprobante, double total, String metodoPago) {
        this.idVenta = idVenta;
        this.idCaja = idCaja;
        this.idUsuario = idUsuario;
        this.tipoComprobante = tipoComprobante;
        this.serieComprobante = serieComprobante;
        this.numeroComprobante = numeroComprobante;
        this.total = total;
        this.metodoPago = metodoPago;
    }

    public int getIdVenta() {
        return idVenta;
    }

    public int getIdCaja() {
        return idCaja;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getTipoComprobante() {
        return tipoComprobante;
    }

    public String getSerieComprobante() {
        return serieComprobante;
    }

    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public double getTotal() {
        return total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setIdVenta(int idVenta) {
        this.idVenta = idVenta;
    }

    public void setIdCaja(int idCaja) {
        this.idCaja = idCaja;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setTipoComprobante(String tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public void setSerieComprobante(String serieComprobante) {
        this.serieComprobante = serieComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

}
