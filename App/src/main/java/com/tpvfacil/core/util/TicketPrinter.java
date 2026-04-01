package com.tpvfacil.core.util;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.verifactu.QrGenerator;
import com.tpvfacil.verifactu.RegistroFactura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la impresión de tickets en impresoras térmicas y comandas de cocina.
 * Usa la API javax.print de Java para compatibilidad con cualquier impresora instalada en Windows.
 */
public class TicketPrinter {

    private static final Logger log = LoggerFactory.getLogger(TicketPrinter.class);

    private TicketPrinter() {}

    /**
     * Imprime el ticket de una factura en la impresora de tickets configurada.
     *
     * @param factura   Factura emitida
     * @param registro  Registro Verifactu con el CSV (puede ser null si está pendiente)
     */
    public static void imprimirTicket(Factura factura, RegistroFactura registro) {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String nombreImpresora = cfg.get(ConfiguracionManager.IMPRESORA_TICKETS);
        int anchoPapel = cfg.getAnchoPapelMm();

        TicketFormatter formatter = new TicketFormatter(anchoPapel);
        List<String> lineas = formatter.formatearFactura(
                factura,
                cfg.getNombreNegocio(),
                cfg.getNif(),
                cfg.getDireccion(),
                registro != null ? registro.getCsvAeat() : null
        );

        // Generar QR
        BufferedImage qr = null;
        try {
            qr = QrGenerator.generarQr(factura, cfg.getNif(), anchoPapel);
        } catch (Exception e) {
            log.warn("No se pudo generar el QR del ticket: {}", e.getMessage());
        }

        imprimir(lineas, qr, nombreImpresora, "Ticket " + factura.getNumeroCompleto());
    }

    /**
     * Imprime una comanda en la impresora de cocina configurada.
     *
     * @param nombreMesa  Nombre de la mesa
     * @param itemsCocina Líneas de la comanda a enviar a cocina
     */
    public static void imprimirComanda(String nombreMesa, List<String> itemsCocina) {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String nombreImpresora = cfg.get(ConfiguracionManager.IMPRESORA_COCINA);
        int anchoPapel = cfg.getAnchoPapelMm();

        TicketFormatter formatter = new TicketFormatter(anchoPapel);
        List<String> lineas = formatter.formatearComanda(0, nombreMesa, itemsCocina);

        imprimir(lineas, null, nombreImpresora, "Comanda " + nombreMesa);
    }

    /**
     * Devuelve la lista de nombres de impresoras instaladas en el sistema.
     */
    public static List<String> listarImpresoras() {
        List<String> nombres = new ArrayList<>();
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService servicio : servicios) {
            nombres.add(servicio.getName());
        }
        log.debug("Impresoras disponibles: {}", nombres);
        return nombres;
    }

    // --- Implementación de impresión ---

    private static void imprimir(List<String> lineas, BufferedImage qr,
                                  String nombreImpresora, String nombreTrabajo) {
        PrinterJob job = PrinterJob.getPrinterJob();

        // Seleccionar impresora si está configurada
        if (nombreImpresora != null && !nombreImpresora.isBlank()) {
            PrintService servicio = buscarImpresora(nombreImpresora);
            if (servicio != null) {
                try {
                    job.setPrintService(servicio);
                } catch (PrinterException e) {
                    log.warn("No se pudo seleccionar la impresora '{}': {}", nombreImpresora, e.getMessage());
                }
            } else {
                log.warn("Impresora '{}' no encontrada. Usando impresora por defecto.", nombreImpresora);
            }
        }

        job.setJobName(nombreTrabajo);
        final BufferedImage qrFinal = qr;
        final List<String> lineasFinal = lineas;

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font fuente = new Font(Font.MONOSPACED, Font.PLAIN, 9);
            g2.setFont(fuente);
            FontMetrics fm = g2.getFontMetrics();
            int alturaLinea = fm.getHeight();

            double x = pageFormat.getImageableX();
            double y = pageFormat.getImageableY();
            int yActual = (int) y;

            for (String linea : lineasFinal) {
                // Placeholder QR
                if ("[QR CODE]".equals(linea) && qrFinal != null) {
                    int tamanoQr = qrFinal.getWidth();
                    g2.drawImage(qrFinal, (int) x, yActual, null);
                    yActual += tamanoQr + 4;
                } else {
                    g2.drawString(linea, (int) x, yActual + fm.getAscent());
                    yActual += alturaLinea;
                }
            }

            return Printable.PAGE_EXISTS;
        });

        try {
            job.print();
            log.info("Trabajo de impresión enviado: {}", nombreTrabajo);
        } catch (PrinterException e) {
            log.error("Error al imprimir '{}': {}", nombreTrabajo, e.getMessage());
        }
    }

    private static PrintService buscarImpresora(String nombre) {
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService servicio : servicios) {
            if (servicio.getName().equalsIgnoreCase(nombre)) return servicio;
        }
        return null;
    }
}
