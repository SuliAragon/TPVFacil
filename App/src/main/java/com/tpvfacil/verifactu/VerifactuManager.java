package com.tpvfacil.verifactu;

import com.tpvfacil.config.ConfiguracionManager;
import com.tpvfacil.core.db.RegistroVerifactuRepository;
import com.tpvfacil.core.db.RegistroVerifactuRepository.RegistroVerifactu;
import com.tpvfacil.core.modelo.Factura;
import com.tpvfacil.core.util.TicketPrinter;
import com.tpvfacil.licencia.LicenciaManager;
import com.tpvfacil.verifactu.excepciones.AeatException;
import com.tpvfacil.verifactu.excepciones.VerifactuException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Orquestador principal del sistema Verifactu.
 * Coordina el flujo completo: hash → firma → XML → envío AEAT → ticket.
 *
 * En MODO DEMO: usa sandbox AEAT.
 * En MODO COMPLETO: usa producción AEAT.
 *
 * Si el envío falla: la venta se completa igualmente y el registro queda PENDIENTE.
 * Al arrancar la app, se reenvían automáticamente los registros PENDIENTE.
 */
public class VerifactuManager {

    private static final Logger log = LoggerFactory.getLogger(VerifactuManager.class);
    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final int MAX_REINTENTOS_REENVIO = 3;

    private static VerifactuManager instancia;

    private final RegistroVerifactuRepository registroRepo;
    private final AeatClient aeatClient;
    private final CertificadoManager certManager;

    private VerifactuManager() {
        this.registroRepo = new RegistroVerifactuRepository();
        this.aeatClient = new AeatClient();
        this.certManager = new CertificadoManager();
    }

    /** Devuelve la instancia única del manager. */
    public static synchronized VerifactuManager getInstance() {
        if (instancia == null) {
            instancia = new VerifactuManager();
        }
        return instancia;
    }

    /**
     * Procesa una factura completa a través del sistema Verifactu.
     * Flujo: hash → firma → XML → envío AEAT → guardar resultado → imprimir ticket.
     *
     * La venta NUNCA se bloquea por un fallo de Verifactu: si falla el envío,
     * el registro queda en estado PENDIENTE para reenviar al arrancar.
     *
     * @param factura Factura ya guardada en la base de datos (con ID asignado)
     * @return RegistroFactura con el resultado (estado OK, PENDIENTE o ERROR)
     */
    public RegistroFactura procesarFactura(Factura factura) {
        RegistroFactura registro = new RegistroFactura();

        try {
            ConfiguracionManager cfg = ConfiguracionManager.getInstance();
            String nif = cfg.getNif();

            // Construir registro con datos de la factura
            registro.setFacturaId(factura.getId());
            registro.setIdEmisorFactura(nif);
            registro.setNumSerieFactura(factura.getNumeroCompleto());
            registro.setFechaExpedicion(factura.getFecha().format(FMT_FECHA));
            registro.setTipoFactura(determinarTipoFactura(factura));
            registro.setBaseImponible(BigDecimal.valueOf(factura.getBaseImponible()).setScale(2, RoundingMode.HALF_UP));
            registro.setCuotaIva(BigDecimal.valueOf(factura.getCuotaIva()).setScale(2, RoundingMode.HALF_UP));
            registro.setImporteTotal(BigDecimal.valueOf(factura.getTotal()).setScale(2, RoundingMode.HALF_UP));

            // Paso 3: Calcular hash encadenado
            String hashAnterior = obtenerHashAnterior();
            registro.setHuellaAnterior(hashAnterior);
            String huella = HashChain.calcularHash(registro, hashAnterior);
            registro.setHuella(huella);
            log.debug("Hash calculado para {}: {}", factura.getNumeroCompleto(), huella);

            // Paso 4: Firma digital (requiere certificado configurado)
            String firma = firmarRegistro(registro, cfg);
            registro.setFirma(firma);

            // Paso 5: Generar XML
            String xml = XmlGenerator.generar(registro);
            registro.setXmlEnviado(xml);

            // Pasos 6-7: Enviar a AEAT y guardar resultado
            boolean modoProduccion = !LicenciaManager.isModoDemo();
            enviarAAeat(registro, xml, modoProduccion);

        } catch (VerifactuException e) {
            log.error("Error en proceso Verifactu para factura {}: {}",
                    factura.getNumeroCompleto(), e.getMessage());
            registro.setEstado(EstadoEnvio.ERROR);
            registro.setErrorDescripcion(e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado en Verifactu para factura {}",
                    factura.getNumeroCompleto(), e);
            registro.setEstado(EstadoEnvio.PENDIENTE);
            registro.setErrorDescripcion("Error inesperado: " + e.getMessage());
        }

        // Persistir el registro en la BD
        persistirRegistro(registro);

        // Paso 8-9: Imprimir ticket (independiente del estado Verifactu)
        try {
            TicketPrinter.imprimirTicket(factura, registro);
        } catch (Exception e) {
            log.warn("No se pudo imprimir el ticket: {}", e.getMessage());
        }

        return registro;
    }

    /**
     * Reenvía todos los registros en estado PENDIENTE o ERROR al arrancar la app.
     * Se llama desde Main.java en el flujo de arranque.
     */
    public void reenviarPendientes() {
        List<RegistroVerifactu> pendientes = registroRepo.findPendientes();
        if (pendientes.isEmpty()) {
            log.info("No hay registros Verifactu pendientes de reenvío.");
            return;
        }

        log.info("Reintentando {} registros Verifactu pendientes...", pendientes.size());
        boolean modoProduccion = !LicenciaManager.isModoDemo();

        for (RegistroVerifactu rv : pendientes) {
            if (rv.intentos >= MAX_REINTENTOS_REENVIO) {
                log.warn("Registro {} supera el máximo de reintentos ({}) — marcando ERROR",
                        rv.facturaId, MAX_REINTENTOS_REENVIO);
                rv.estado = EstadoEnvio.ERROR.name();
                rv.errorDescripcion = "Superado el número máximo de reintentos";
                registroRepo.update(rv);
                continue;
            }

            try {
                RespuestaAeat respuesta = aeatClient.enviar(rv.xmlEnviado, modoProduccion);
                if (respuesta.isCorrecto() || respuesta.esDuplicada()) {
                    rv.estado = EstadoEnvio.OK.name();
                    rv.csvAeat = respuesta.getCsv() != null ? respuesta.getCsv() : rv.csvAeat;
                    rv.fechaEnvio = LocalDateTime.now().toString();
                    log.info("Reenvío OK para factura {}", rv.facturaId);
                } else {
                    rv.intentos++;
                    rv.errorDescripcion = respuesta.getDescripcionError();
                    log.warn("Reenvío fallido para factura {}: {}", rv.facturaId, respuesta.getDescripcionError());
                }
            } catch (Exception e) {
                rv.intentos++;
                rv.errorDescripcion = e.getMessage();
                log.warn("Error al reenviar factura {}: {}", rv.facturaId, e.getMessage());
            }

            registroRepo.update(rv);
        }
    }

    /**
     * Carga el certificado digital y lo cachea para uso posterior.
     * Llamar desde ConfiguracionController al guardar el certificado.
     *
     * @throws VerifactuException si el certificado no se puede cargar
     */
    public void cargarCertificado() throws VerifactuException {
        ConfiguracionManager cfg = ConfiguracionManager.getInstance();
        String ruta = cfg.getRutaCertificado();
        String pwdCifrada = cfg.get(ConfiguracionManager.PASSWORD_CERTIFICADO, "");

        if (ruta == null || ruta.isBlank()) {
            log.info("Sin certificado configurado — modo sin firma activo");
            return;
        }

        String password = pwdCifrada.isBlank() ? "" : certManager.descifrarPassword(pwdCifrada);
        certManager.cargarCertificado(Path.of(ruta), password);
        log.info("Certificado Verifactu cargado correctamente.");
    }

    /** Devuelve el CertificadoManager para acceso desde la UI de configuración. */
    public CertificadoManager getCertificadoManager() {
        return certManager;
    }

    // --- Métodos privados ---

    private String obtenerHashAnterior() {
        Optional<RegistroVerifactu> ultimo = registroRepo.findUltimo();
        if (ultimo.isPresent() && ultimo.get().huella != null) {
            return ultimo.get().huella;
        }
        return HashChain.HASH_INICIAL;
    }

    private String firmarRegistro(RegistroFactura registro, ConfiguracionManager cfg) throws VerifactuException {
        if (certManager.getKeyStoreCacheado() == null) {
            // Intentar cargar el certificado si está configurado
            cargarCertificado();
        }

        if (certManager.getKeyStoreCacheado() == null) {
            // Sin certificado: firmar con placeholder (solo válido en sandbox/demo)
            log.warn("Sin certificado configurado — usando firma placeholder (solo sandbox)");
            return HashChain.sha256Hex(registro.getHuella() + "SIN_CERTIFICADO");
        }

        String cadenaAFirmar = HashChain.construirCadena(registro, registro.getHuellaAnterior());
        String pwdCifrada = cfg.get(ConfiguracionManager.PASSWORD_CERTIFICADO, "");
        String password = pwdCifrada.isBlank() ? "" : certManager.descifrarPassword(pwdCifrada);

        return FirmaDigital.firmar(cadenaAFirmar, certManager.getKeyStoreCacheado(),
                certManager.getAlias(), password);
    }

    private void enviarAAeat(RegistroFactura registro, String xml, boolean modoProduccion) {
        try {
            RespuestaAeat respuesta = aeatClient.enviar(xml, modoProduccion);

            if (respuesta.isCorrecto() || respuesta.esDuplicada()) {
                registro.setEstado(EstadoEnvio.OK);
                registro.setCsvAeat(respuesta.getCsv());
                registro.setErrorDescripcion(null);
            } else {
                registro.setEstado(EstadoEnvio.ERROR);
                registro.setErrorDescripcion(respuesta.getDescripcionError());
            }
        } catch (AeatException e) {
            log.warn("Envío AEAT fallido — registro queda PENDIENTE: {}", e.getMessage());
            registro.setEstado(EstadoEnvio.PENDIENTE);
            registro.setErrorDescripcion(e.getMessage());
        }
    }

    private void persistirRegistro(RegistroFactura registro) {
        RegistroVerifactu rv = new RegistroVerifactu();
        rv.facturaId = registro.getFacturaId();
        rv.huella = registro.getHuella() != null ? registro.getHuella() : "";
        rv.huellaAnterior = registro.getHuellaAnterior() != null ? registro.getHuellaAnterior() : HashChain.HASH_INICIAL;
        rv.firma = registro.getFirma() != null ? registro.getFirma() : "";
        rv.xmlEnviado = registro.getXmlEnviado();
        rv.csvAeat = registro.getCsvAeat();
        rv.estado = registro.getEstado().name();
        rv.errorDescripcion = registro.getErrorDescripcion();
        rv.fechaEnvio = registro.getEstado() == EstadoEnvio.OK
                ? LocalDateTime.now().toString() : null;
        rv.intentos = registro.getIntentos();

        registroRepo.save(rv);
    }

    /**
     * Determina el tipo de factura AEAT:
     * F1 = factura completa (con cliente), F2 = ticket simplificado.
     */
    private String determinarTipoFactura(Factura factura) {
        return factura.getClienteId() > 0 ? "F1" : "F2";
    }
}
