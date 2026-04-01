package com.tpvfacil.core.util;

import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.modelo.LineaFactura;
import com.tpvfacil.licencia.LicenciaManager;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Formatea el contenido de un ticket térmico según el ancho de papel configurado.
 *
 * Anchos soportados:
 *   58mm → 32 caracteres por línea
 *   80mm → 48 caracteres por línea
 *
 * En modo demo añade la marca de agua "DEMO — NO VÁLIDO FISCALMENTE".
 */
public class TicketFormatter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int anchoChars;

    public TicketFormatter(int anchoMm) {
        this.anchoChars = anchoMm <= 58 ? 32 : 48;
    }

    /**
     * Genera las líneas de texto del ticket de una factura.
     *
     * @param factura          Factura emitida
     * @param nombreNegocio    Nombre del negocio
     * @param nif              NIF del negocio
     * @param direccion        Dirección del negocio
     * @param csvAeat          Código CSV de la AEAT (puede ser null si está pendiente)
     * @return Lista de líneas de texto listas para imprimir
     */
    public List<String> formatearFactura(Factura factura, String nombreNegocio,
                                          String nif, String direccion, String csvAeat) {
        List<String> lineas = new ArrayList<>();

        // Cabecera del negocio
        lineas.add(separador('─'));
        lineas.add(centrar(nombreNegocio));
        lineas.add(centrar("NIF: " + nif));
        if (direccion != null && !direccion.isBlank()) {
            lineas.addAll(partirLinea(direccion));
        }
        lineas.add(separador('─'));

        // Datos del ticket
        lineas.add("Ticket Nº: " + factura.getNumeroCompleto());
        lineas.add("Fecha: " + (factura.getFecha() != null ? factura.getFecha().format(FMT) : ""));
        lineas.add("");

        // Líneas de productos
        for (LineaFactura linea : factura.getLineas()) {
            String descripcion = truncar(linea.getDescripcion(), anchoChars - 10);
            String cantidadPrecio = "x" + formatNum(linea.getCantidad());
            String subtotal = formatEuro(linea.getSubtotal());
            lineas.add(izquierdaDerecha(descripcion + " " + cantidadPrecio, subtotal));
        }

        // Totales
        lineas.add(separador('─'));
        lineas.add(izquierdaDerecha("Base imponible:", formatEuro(factura.getBaseImponible())));
        lineas.add(izquierdaDerecha("IVA:", formatEuro(factura.getCuotaIva())));
        lineas.add(separador('─'));
        lineas.add(izquierdaDerecha("TOTAL:", formatEuro(factura.getTotal())));

        if (factura.getFormaPago() != null) {
            lineas.add(izquierdaDerecha("Forma de pago:", factura.getFormaPago().getDescripcion()));
            if (factura.getEfectivoEntregado() > 0) {
                lineas.add(izquierdaDerecha("Efectivo:", formatEuro(factura.getEfectivoEntregado())));
                lineas.add(izquierdaDerecha("Cambio:", formatEuro(factura.getCambio())));
            }
        }

        // Verifactu
        lineas.add(separador('─'));
        lineas.add("[QR CODE]");
        if (csvAeat != null && !csvAeat.isBlank()) {
            lineas.add("CSV: " + csvAeat);
        } else {
            lineas.add("CSV: Pendiente de envío AEAT");
        }
        lineas.add("Verifica: sede.agenciatributaria.gob.es");

        // Marca de agua DEMO
        if (LicenciaManager.isModoDemo()) {
            lineas.add(separador('═'));
            lineas.add(centrar("DEMO — NO VÁLIDO FISCALMENTE"));
            lineas.add(separador('═'));
        }

        lineas.add(separador('─'));
        lineas.add("");

        return lineas;
    }

    /**
     * Formatea una comanda para la impresora de cocina.
     */
    public List<String> formatearComanda(int mesaId, String nombreMesa, List<String> items) {
        List<String> lineas = new ArrayList<>();
        lineas.add(separador('═'));
        lineas.add(centrar("** COCINA **"));
        lineas.add(centrar("Mesa: " + nombreMesa));
        lineas.add(centrar(java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        lineas.add(separador('─'));
        for (String item : items) {
            lineas.add(item);
        }
        lineas.add(separador('═'));
        lineas.add("");
        return lineas;
    }

    // --- Métodos de formato ---

    private String separador(char c) {
        return String.valueOf(c).repeat(anchoChars);
    }

    private String centrar(String texto) {
        if (texto.length() >= anchoChars) return truncar(texto, anchoChars);
        int espacios = (anchoChars - texto.length()) / 2;
        return " ".repeat(espacios) + texto;
    }

    private String izquierdaDerecha(String izquierda, String derecha) {
        int espacios = anchoChars - izquierda.length() - derecha.length();
        if (espacios < 1) espacios = 1;
        return izquierda + " ".repeat(espacios) + derecha;
    }

    private String truncar(String texto, int max) {
        if (texto == null) return "";
        return texto.length() <= max ? texto : texto.substring(0, max - 1) + "…";
    }

    private List<String> partirLinea(String texto) {
        List<String> resultado = new ArrayList<>();
        while (texto.length() > anchoChars) {
            resultado.add(texto.substring(0, anchoChars));
            texto = texto.substring(anchoChars);
        }
        if (!texto.isBlank()) resultado.add(centrar(texto));
        return resultado;
    }

    private String formatEuro(double importe) {
        return String.format("%.2f€", importe).replace(".", ",");
    }

    private String formatNum(double num) {
        if (num == Math.floor(num)) return String.valueOf((int) num);
        return String.format("%.2f", num).replace(".", ",");
    }
}
