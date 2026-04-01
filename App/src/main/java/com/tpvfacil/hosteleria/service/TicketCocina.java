package com.tpvfacil.hosteleria.service;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.db.ComandaRepository;
import com.tpvfacil.core.db.MesaRepository;
import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.LineaComanda;
import com.tpvfacil.hosteleria.modelo.Mesa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Genera el ticket de cocina y marca las líneas como enviadas.
 * Si no hay impresora de cocina configurada, solo marca las líneas
 * y muestra un aviso (no lanza excepción).
 */
public class TicketCocina {

    private static final Logger log = LoggerFactory.getLogger(TicketCocina.class);
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final ComandaRepository comandaRepository = new ComandaRepository();
    private final MesaRepository mesaRepository = new MesaRepository();

    /**
     * Envía las líneas nuevas a cocina: genera el ticket y marca como enviadas.
     * @return true si se envió (o simuló) correctamente
     */
    public boolean enviarACocina(Comanda comanda, List<LineaComanda> lineasNuevas) {
        if (lineasNuevas.isEmpty()) {
            log.info("No hay líneas nuevas para enviar a cocina");
            return false;
        }

        // Buscar mesa para el ticket
        Mesa mesa = mesaRepository.findById(comanda.getMesaId()).orElse(null);
        String nombreMesa = mesa != null ? mesa.getNombre() + " — " + mesa.getZona() : "Mesa " + comanda.getMesaId();

        // Generar formato de ticket
        StringBuilder ticket = new StringBuilder();
        ticket.append("════════════════\n");
        ticket.append("     COCINA\n");
        ticket.append("════════════════\n");
        ticket.append("Mesa: ").append(nombreMesa).append("\n");
        ticket.append("Hora: ").append(LocalDateTime.now().format(FMT_HORA)).append("\n");
        ticket.append("════════════════\n");

        for (LineaComanda l : lineasNuevas) {
            ticket.append(String.format("x%.0f  %s%n", l.getCantidad(), l.getNombreProducto()));
            if (l.getObservaciones() != null && !l.getObservaciones().isBlank()) {
                ticket.append("     ⚠ ").append(l.getObservaciones()).append("\n");
            }
        }
        ticket.append("════════════════\n");

        // Comprobar si hay impresora de cocina configurada
        String impresora = ConfiguracionManager.getInstance().get("impresora_cocina", "");
        if (impresora.isBlank()) {
            log.warn("No hay impresora de cocina configurada. Ticket generado pero no impreso.");
            log.info("Ticket cocina:\n{}", ticket);
        } else {
            // Aquí iría la impresión real con Java Print API
            log.info("Enviando ticket a impresora de cocina: {}", impresora);
            log.info("Ticket cocina:\n{}", ticket);
        }

        // Marcar líneas como enviadas a cocina
        for (LineaComanda l : lineasNuevas) {
            l.setEnviadoCocina(true);
            l.setEstado(LineaComanda.Estado.EN_COCINA);
            comandaRepository.actualizarEstadoLinea(l.getId(), LineaComanda.Estado.EN_COCINA, true);
        }

        log.info("Enviadas {} líneas a cocina para {}", lineasNuevas.size(), nombreMesa);
        return true;
    }
}
