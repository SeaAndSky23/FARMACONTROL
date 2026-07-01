/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reportes;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Desktop;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import modelo.VentaDTO;

/**
 *
 * @author USUARIO
 */
public class GeneradorReciboPDF {

    /**
     * Genera el PDF del comprobante y lo abre automáticamente (vista previa de
     * impresión / lector PDF predeterminado del sistema).
     *
     * @param venta Cabecera de la venta ya registrada
     * @param modeloTabla El DefaultTableModel de tblVenta (para sacar el
     * detalle)
     * @param nombreCliente Nombre del cliente (o "CLIENTE VARIOS")
     * @param cajero Nombre del cajero logueado
     * @param subtotalNeto Total sin IGV
     * @param igv Monto de IGV
     */
    public static void generarYAbrir(VentaDTO venta, DefaultTableModel modeloTabla,
            String nombreCliente, String cajero,
            double subtotalNeto, double igv) {

        // 1. Carpeta destino: /recibos dentro del proyecto (se crea si no existe)
        File carpeta = new File("recibos");
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        String nombreArchivo = "recibos/" + venta.getTipoComprobante() + "_"
                + venta.getNumeroComprobante() + ".pdf";

        Document documento = new Document(PageSize.A5); // tamaño típico de boleta/ticket

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(nombreArchivo));
            documento.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            // ── ENCABEZADO ──
            Paragraph titulo = new Paragraph("FARMACIA BLASFARMA", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);

            Paragraph tipoDoc = new Paragraph(
                    venta.getTipoComprobante().toUpperCase()
                    + "  N° " + venta.getNumeroComprobante(), fSubtitulo);
            tipoDoc.setAlignment(Element.ALIGN_CENTER);
            tipoDoc.setSpacingAfter(10f);
            documento.add(tipoDoc);

            // ── DATOS GENERALES ──
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            documento.add(new Paragraph("Fecha: " + sdf.format(new Date()), fNormal));
            documento.add(new Paragraph("Cliente: " + nombreCliente, fNormal));
            documento.add(new Paragraph("Cajero: " + cajero, fNormal));
            documento.add(new Paragraph("Método de pago: " + venta.getMetodoPago(), fNormal));
            documento.add(Chunk.NEWLINE);

            // ── TABLA DE PRODUCTOS ──
            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4f, 1.5f, 1.8f, 1.8f});

            agregarCeldaHeader(tabla, "Descripción", fNegrita);
            agregarCeldaHeader(tabla, "Cant.", fNegrita);
            agregarCeldaHeader(tabla, "P.Unit", fNegrita);
            agregarCeldaHeader(tabla, "Subtotal", fNegrita);

            // Columnas reales de tu tblVenta:
            // 0 CodBarras | 1 Descripcion | 2 Unidad | 3 Cantidad
            // 4 PrecioUnit | 5 IGV | 6 Subtotal | 7 Multiplo | 8 Id_Prod
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                Object idProd = modeloTabla.getValueAt(i, 8);
                if (idProd == null || idProd.toString().trim().isEmpty()) {
                    continue; // salta la fila vacía de escaneo
                }
                String descripcion = String.valueOf(modeloTabla.getValueAt(i, 1));
                String cantidad = String.valueOf(modeloTabla.getValueAt(i, 3));
                double precio = (double) modeloTabla.getValueAt(i, 4);
                double subtotal = (double) modeloTabla.getValueAt(i, 6);

                tabla.addCell(new Phrase(descripcion, fNormal));
                tabla.addCell(new Phrase(cantidad, fNormal));
                tabla.addCell(new Phrase(String.format("%.2f", precio), fNormal));
                tabla.addCell(new Phrase(String.format("%.2f", subtotal), fNormal));
            }

            documento.add(tabla);
            documento.add(Chunk.NEWLINE);

            // ── TOTALES ──
            Paragraph pNeto = new Paragraph("Subtotal: S/ " + String.format("%.2f", subtotalNeto), fNormal);
            pNeto.setAlignment(Element.ALIGN_RIGHT);
            documento.add(pNeto);

            Paragraph pIgv = new Paragraph("IGV (18%): S/ " + String.format("%.2f", igv), fNormal);
            pIgv.setAlignment(Element.ALIGN_RIGHT);
            documento.add(pIgv);

            Paragraph pTotal = new Paragraph("TOTAL: S/ " + String.format("%.2f", venta.getTotal()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            pTotal.setAlignment(Element.ALIGN_RIGHT);
            documento.add(pTotal);

            documento.add(Chunk.NEWLINE);
            Paragraph gracias = new Paragraph("¡Gracias por su compra!", fNormal);
            gracias.setAlignment(Element.ALIGN_CENTER);
            documento.add(gracias);

            documento.close();

            // 2. Abrir el PDF automáticamente (esto ya sirve como "vista previa de impresión":
            //    el usuario ve el PDF en su lector predeterminado y desde ahí presiona Ctrl+P)
            abrirArchivo(new File(nombreArchivo));

        } catch (DocumentException | java.io.IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error al generar el PDF: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void agregarCeldaHeader(PdfPTable tabla, String texto, Font f) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, f));
        celda.setBackgroundColor(new java.awt.Color(230, 230, 230));
        celda.setPadding(4f);
        tabla.addCell(celda);
    }

    private static void abrirArchivo(File archivo) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            }
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(null,
                    "El PDF se generó en: " + archivo.getAbsolutePath()
                    + "\npero no se pudo abrir automáticamente.",
                    "Aviso", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
