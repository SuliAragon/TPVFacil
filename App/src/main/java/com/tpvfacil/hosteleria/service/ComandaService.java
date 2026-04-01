package com.tpvfacil.hosteleria.service;

import com.tpvfacil.core.db.ComandaRepository;
import com.tpvfacil.core.db.MesaRepository;
import com.tpvfacil.hosteleria.modelo.Comanda;
import com.tpvfacil.hosteleria.modelo.EstadoMesa;
import com.tpvfacil.hosteleria.modelo.LineaComanda;
import com.tpvfacil.hosteleria.modelo.Mesa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de lógica de negocio para comandas de hostelería.
 * Gestiona la creación, cierre y cálculo de totales de comandas.
 */
public class ComandaService {

    private static final Logger log = LoggerFactory.getLogger(ComandaService.class);

    private final ComandaRepository comandaRepository = new ComandaRepository();
    private final MesaRepository mesaRepository = new MesaRepository();

    /** Crea una nueva comanda para la mesa indicada y la pone OCUPADA. */
    public Comanda crearComanda(Mesa mesa) {
        Comanda comanda = new Comanda();
        comanda.setMesaId(mesa.getId());
        comanda.setFechaApertura(LocalDateTime.now());
        comanda.setEstado(Comanda.Estado.ABIERTA);
        comanda.setNumComensales(1);

        comandaRepository.save(comanda);
        mesaRepository.actualizarEstado(mesa.getId(), EstadoMesa.OCUPADA);

        log.info("Nueva comanda {} creada para {}", comanda.getId(), mesa.getNombre());
        return comanda;
    }

    /** Obtiene la comanda abierta de una mesa, o crea una nueva si no existe. */
    public Comanda obtenerOCrear(Mesa mesa) {
        Optional<Comanda> existente = comandaRepository.findAbiertaByMesaId(mesa.getId());
        return existente.orElseGet(() -> crearComanda(mesa));
    }

    /** Cierra una comanda (estado → CERRADA, mesa → PENDIENTE_PAGO). */
    public void cerrarComanda(Comanda comanda) {
        comanda.setEstado(Comanda.Estado.CERRADA);
        comanda.setFechaCierre(LocalDateTime.now());
        comandaRepository.update(comanda);
        mesaRepository.actualizarEstado(comanda.getMesaId(), EstadoMesa.PENDIENTE_PAGO);
        log.info("Comanda {} cerrada", comanda.getId());
    }

    /** Marca la comanda como pagada y libera la mesa. */
    public void marcarPagada(Comanda comanda, int facturaId) {
        comanda.setEstado(Comanda.Estado.PAGADA);
        comanda.setFacturaId(facturaId);
        comanda.setFechaCierre(LocalDateTime.now());
        comandaRepository.update(comanda);
        mesaRepository.actualizarEstado(comanda.getMesaId(), EstadoMesa.LIBRE);
        log.info("Comanda {} pagada — mesa liberada", comanda.getId());
    }

    /** Calcula el total con IVA de una comanda. */
    public double calcularTotal(Comanda comanda) {
        if (comanda.getLineas() == null) return 0;
        double total = 0;
        for (LineaComanda l : comanda.getLineas()) {
            total += l.getSubtotal();
        }
        return total;
    }

    /** Devuelve las líneas no enviadas a cocina de una comanda. */
    public List<LineaComanda> getLineasNuevas(Comanda comanda) {
        return comanda.getLineas().stream()
                .filter(l -> !l.isEnviadoCocina())
                .toList();
    }
}
