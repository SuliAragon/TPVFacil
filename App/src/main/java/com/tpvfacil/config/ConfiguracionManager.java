package com.tpvfacil.config;

import com.tpvfacil.core.db.ConfiguracionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton que gestiona toda la configuración del negocio.
 * Mantiene una caché en memoria para evitar consultas repetidas a SQLite.
 * Nunca hardcodear credenciales — todo pasa por este manager.
 */
public class ConfiguracionManager {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionManager.class);

    private static ConfiguracionManager instancia;
    private final Map<String, String> cache = new HashMap<>();
    private ConfiguracionRepository repo;

    // Claves de configuración
    public static final String NOMBRE_NEGOCIO        = "nombre_negocio";
    public static final String NIF                   = "nif";
    public static final String DIRECCION             = "direccion";
    public static final String RUTA_CERTIFICADO      = "ruta_certificado";
    public static final String PASSWORD_CERTIFICADO  = "password_certificado";
    public static final String ANCHO_PAPEL_MM        = "ancho_papel_mm";
    public static final String IMPRESORA_TICKETS     = "impresora_tickets";
    public static final String IMPRESORA_COCINA      = "impresora_cocina";
    public static final String WEB_URL_COMPRA        = "web_url_compra";
    public static final String DB_VERSION            = "db_version";
    public static final String APP_VERSION           = "app_version";
    public static final String HW_FINGERPRINT_FALLBACK = "hw_fingerprint_fallback";
    public static final String LICENCIA_ACTIVADA     = "licencia_activada";
    public static final String VERIFACTU_SANDBOX     = "verifactu_sandbox";

    private ConfiguracionManager() {}

    /** Devuelve la instancia única. */
    public static synchronized ConfiguracionManager getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracionManager();
        }
        return instancia;
    }

    /**
     * Carga toda la configuración desde la base de datos hacia la caché.
     * Debe llamarse después de DatabaseManager.inicializar().
     */
    public void cargar() {
        repo = new ConfiguracionRepository();
        cache.clear();
        cache.putAll(repo.getAll());
        // Valores por defecto si no existen
        cache.putIfAbsent(ANCHO_PAPEL_MM, "80");
        cache.putIfAbsent(WEB_URL_COMPRA, "https://tpvfacil.es/precios");
        cache.putIfAbsent(APP_VERSION, "1.0.0");
        log.info("Configuración cargada: {} claves", cache.size());
    }

    /** Obtiene un valor de la caché (null si no existe). */
    public String get(String clave) {
        return cache.get(clave);
    }

    /** Obtiene un valor con un valor por defecto si no existe. */
    public String get(String clave, String valorPorDefecto) {
        return cache.getOrDefault(clave, valorPorDefecto);
    }

    /**
     * Guarda un valor en caché y lo persiste en la base de datos.
     */
    public void set(String clave, String valor) {
        cache.put(clave, valor);
        if (repo != null) {
            repo.set(clave, valor);
        }
    }

    // --- Accesores tipados frecuentes ---

    /** Nombre fiscal del negocio. */
    public String getNombreNegocio() {
        return get(NOMBRE_NEGOCIO, "");
    }

    /** NIF/CIF del negocio. */
    public String getNif() {
        return get(NIF, "");
    }

    /** Dirección completa del negocio. */
    public String getDireccion() {
        return get(DIRECCION, "");
    }

    /** Ancho del papel de la impresora de tickets en mm (58 ó 80). */
    public int getAnchoPapelMm() {
        try {
            return Integer.parseInt(get(ANCHO_PAPEL_MM, "80"));
        } catch (NumberFormatException e) {
            return 80;
        }
    }

    /** Ruta al certificado Verifactu (.p12/.pfx). */
    public String getRutaCertificado() {
        return get(RUTA_CERTIFICADO, "");
    }

    /** URL de la página de compra del software. */
    public String getWebUrlCompra() {
        return get(WEB_URL_COMPRA, "https://tpvfacil.es/precios");
    }

    /**
     * Indica si el negocio ha completado la configuración mínima obligatoria
     * (nombre, NIF y dirección).
     */
    public boolean isConfigurado() {
        String nombre = getNombreNegocio();
        String nif = getNif();
        String dir = getDireccion();
        return nombre != null && !nombre.isBlank()
                && nif != null && !nif.isBlank()
                && dir != null && !dir.isBlank();
    }

    /** Indica si Verifactu debe usar el entorno sandbox de la AEAT. */
    public boolean isVerifactuSandbox() {
        return "true".equalsIgnoreCase(get(VERIFACTU_SANDBOX, "false"));
    }
}
