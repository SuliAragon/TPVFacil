package com.tpvfacil.licencia;

import com.tpvfacil.core.db.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

/**
 * Gestiona las restricciones del modo demo.
 * Límite: 20 tickets por día. Verifactu solo en sandbox.
 */
public class ModoDemo {

    private static final Logger log = LoggerFactory.getLogger(ModoDemo.class);

    /** Máximo de tickets que se pueden emitir por día en modo demo. */
    public static final int MAX_TICKETS_DIA = 20;

    private static final FacturaRepository facturaRepo = new FacturaRepository();

    private ModoDemo() {}

    /**
     * Indica si se puede emitir un ticket ahora mismo.
     * En modo completo siempre devuelve true.
     * En modo demo, comprueba el contador diario.
     */
    public static boolean puedeEmitirTicket() {
        if (!LicenciaManager.isModoDemo()) return true;
        int hoy = facturaRepo.contarPorFecha(LocalDate.now());
        boolean puede = hoy < MAX_TICKETS_DIA;
        if (!puede) {
            log.warn("Límite demo alcanzado: {} tickets emitidos hoy", hoy);
        }
        return puede;
    }

    /**
     * Devuelve los tickets restantes hoy en modo demo.
     * En modo completo devuelve Integer.MAX_VALUE.
     */
    public static int ticketsRestantesHoy() {
        if (!LicenciaManager.isModoDemo()) return Integer.MAX_VALUE;
        int hoy = facturaRepo.contarPorFecha(LocalDate.now());
        return Math.max(0, MAX_TICKETS_DIA - hoy);
    }

    /**
     * Devuelve los tickets emitidos hoy.
     * En modo completo devuelve 0 (no aplica límite).
     */
    public static int ticketsEmitidosHoy() {
        if (!LicenciaManager.isModoDemo()) return 0;
        return facturaRepo.contarPorFecha(LocalDate.now());
    }
}
