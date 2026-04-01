package com.tpvfacil.core.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor singleton de la base de datos SQLite.
 * Único punto de acceso a la conexión. Thread-safe.
 *
 * Rutas de la base de datos:
 *   Windows: %LOCALAPPDATA%\TPVFacil\tpvfacil.db
 *   Mac:     ~/Library/Application Support/TPVFacil/tpvfacil.db
 *   Linux:   ~/.tpvfacil/tpvfacil.db
 */
public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static final int VERSION_ACTUAL = 1;

    private static DatabaseManager instancia;
    private Connection conexion;

    private DatabaseManager() {}

    /** Devuelve la instancia única del gestor. */
    public static synchronized DatabaseManager getInstance() {
        if (instancia == null) {
            instancia = new DatabaseManager();
        }
        return instancia;
    }

    /**
     * Inicializa la base de datos: crea el directorio AppData,
     * abre la conexión, activa PRAGMAs y ejecuta migraciones.
     */
    public void inicializar() {
        try {
            String rutaBd = obtenerRutaBd();
            File archivoBd = new File(rutaBd);
            archivoBd.getParentFile().mkdirs();

            conexion = DriverManager.getConnection("jdbc:sqlite:" + rutaBd);

            try (Statement stmt = conexion.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA foreign_keys=ON");
            }

            ejecutarMigraciones();
            log.info("Base de datos inicializada en: {}", rutaBd);

        } catch (SQLException e) {
            throw new RuntimeException("Error crítico al inicializar la base de datos", e);
        }
    }

    /** Devuelve la conexión activa a la BD. */
    public Connection getConexion() {
        return conexion;
    }

    /** Cierra la conexión con la base de datos. */
    public void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
                log.info("Conexión a base de datos cerrada.");
            } catch (SQLException e) {
                log.error("Error al cerrar la conexión", e);
            }
        }
    }

    // --- Rutas por sistema operativo ---

    private String obtenerRutaBd() {
        String os = System.getProperty("os.name").toLowerCase();
        String ruta;

        if (os.contains("win")) {
            ruta = System.getenv("LOCALAPPDATA") + File.separator + "TPVFacil" + File.separator + "tpvfacil.db";
        } else if (os.contains("mac")) {
            ruta = System.getProperty("user.home") + "/Library/Application Support/TPVFacil/tpvfacil.db";
        } else {
            ruta = System.getProperty("user.home") + "/.tpvfacil/tpvfacil.db";
        }

        return ruta;
    }

    // --- Migraciones ---

    private void ejecutarMigraciones() throws SQLException {
        int instalada = obtenerVersionBd();
        if (instalada < 1) {
            migrar1();
        }
        // Futuras versiones: if (instalada < 2) migrar2();
    }

    private int obtenerVersionBd() {
        try (var stmt = conexion.prepareStatement(
                "SELECT valor FROM configuracion WHERE clave = 'db_version'")) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return Integer.parseInt(rs.getString("valor"));
            }
        } catch (SQLException e) {
            // La tabla aún no existe → versión 0
        }
        return 0;
    }

    /** Migración v1: crea el esquema completo inicial. */
    private void migrar1() throws SQLException {
        log.info("Ejecutando migración v1: creando esquema inicial...");

        String[] sentencias = {
            // Tablas core
            """
            CREATE TABLE IF NOT EXISTS configuracion (
              clave TEXT PRIMARY KEY,
              valor TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS productos (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre          TEXT NOT NULL,
              precio          REAL NOT NULL,
              iva_porcentaje  REAL NOT NULL DEFAULT 21.0,
              categoria       TEXT,
              codigo_barras   TEXT UNIQUE,
              stock           INTEGER DEFAULT -1,
              activo          INTEGER DEFAULT 1,
              fecha_creacion  TEXT DEFAULT (datetime('now'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS clientes (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre          TEXT NOT NULL,
              nif             TEXT UNIQUE,
              direccion       TEXT,
              codigo_postal   TEXT,
              ciudad          TEXT,
              telefono        TEXT,
              email           TEXT,
              fecha_creacion  TEXT DEFAULT (datetime('now'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS facturas (
              id                  INTEGER PRIMARY KEY AUTOINCREMENT,
              serie               TEXT NOT NULL DEFAULT 'A',
              numero              INTEGER NOT NULL,
              fecha               TEXT NOT NULL,
              cliente_id          INTEGER REFERENCES clientes(id),
              base_imponible      REAL NOT NULL,
              cuota_iva           REAL NOT NULL,
              total               REAL NOT NULL,
              forma_pago          TEXT NOT NULL,
              efectivo_entregado  REAL DEFAULT 0,
              cambio              REAL DEFAULT 0,
              tipo_negocio        TEXT NOT NULL,
              anulada             INTEGER DEFAULT 0,
              UNIQUE(serie, numero)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS lineas_factura (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              factura_id      INTEGER NOT NULL REFERENCES facturas(id),
              producto_id     INTEGER REFERENCES productos(id),
              descripcion     TEXT NOT NULL,
              cantidad        REAL NOT NULL,
              precio_unitario REAL NOT NULL,
              iva_porcentaje  REAL NOT NULL,
              subtotal        REAL NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS registros_verifactu (
              id                  INTEGER PRIMARY KEY AUTOINCREMENT,
              factura_id          INTEGER NOT NULL REFERENCES facturas(id),
              huella              TEXT NOT NULL,
              huella_anterior     TEXT NOT NULL,
              firma               TEXT NOT NULL,
              xml_enviado         TEXT,
              csv_aeat            TEXT,
              estado              TEXT NOT NULL DEFAULT 'PENDIENTE',
              error_descripcion   TEXT,
              fecha_envio         TEXT,
              intentos            INTEGER DEFAULT 0
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS caja (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              fecha_apertura  TEXT NOT NULL,
              fecha_cierre    TEXT,
              efectivo_inicial REAL DEFAULT 0,
              total_efectivo  REAL DEFAULT 0,
              total_tarjeta   REAL DEFAULT 0,
              total_ventas    INTEGER DEFAULT 0,
              estado          TEXT DEFAULT 'ABIERTA'
            )
            """,
            // Tablas hostelería
            """
            CREATE TABLE IF NOT EXISTS mesas (
              id          INTEGER PRIMARY KEY AUTOINCREMENT,
              nombre      TEXT NOT NULL,
              capacidad   INTEGER DEFAULT 4,
              zona        TEXT DEFAULT 'Sala',
              estado      TEXT DEFAULT 'LIBRE',
              activa      INTEGER DEFAULT 1
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS comandas (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              mesa_id         INTEGER NOT NULL REFERENCES mesas(id),
              fecha_apertura  TEXT NOT NULL,
              fecha_cierre    TEXT,
              num_comensales  INTEGER DEFAULT 1,
              estado          TEXT DEFAULT 'ABIERTA',
              factura_id      INTEGER REFERENCES facturas(id),
              observaciones   TEXT
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS lineas_comanda (
              id              INTEGER PRIMARY KEY AUTOINCREMENT,
              comanda_id      INTEGER NOT NULL REFERENCES comandas(id),
              producto_id     INTEGER NOT NULL REFERENCES productos(id),
              cantidad        REAL NOT NULL DEFAULT 1,
              precio_unitario REAL NOT NULL,
              enviado_cocina  INTEGER DEFAULT 0,
              estado          TEXT DEFAULT 'PENDIENTE',
              observaciones   TEXT,
              fecha_pedido    TEXT DEFAULT (datetime('now'))
            )
            """,
            // Índices
            "CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha)",
            "CREATE INDEX IF NOT EXISTS idx_productos_barras ON productos(codigo_barras)",
            "CREATE INDEX IF NOT EXISTS idx_registros_estado ON registros_verifactu(estado)",
            "CREATE INDEX IF NOT EXISTS idx_comandas_mesa ON comandas(mesa_id, estado)",
            // Valores iniciales
            """
            INSERT OR IGNORE INTO configuracion (clave, valor) VALUES
              ('db_version', '1'),
              ('ancho_papel_mm', '80'),
              ('verifactu_sandbox', 'false'),
              ('web_url_compra', 'https://tpvfacil.es/precios')
            """
        };

        try (Statement stmt = conexion.createStatement()) {
            for (String sql : sentencias) {
                stmt.execute(sql.trim());
            }
        }

        // Datos de prueba para Fase 3 (Comercio)
        String datosTest = """
            INSERT OR IGNORE INTO productos (nombre, precio, iva_porcentaje, categoria, codigo_barras, stock) VALUES
              ('Agua mineral 1L', 0.79, 10.0, 'Bebidas', '8410207000016', 50),
              ('Coca-Cola 330ml', 1.29, 21.0, 'Bebidas', '5449000000996', 30),
              ('Pan de molde', 1.49, 4.0, 'Alimentación', '8410011009980', 20),
              ('Producto sin barras', 5.00, 21.0, 'Otros', NULL, -1)
            """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(datosTest.trim());
        }

        // Datos de prueba para Fase 4 (Hostelería)
        String datosMesas = """
            INSERT OR IGNORE INTO mesas (nombre, capacidad, zona) VALUES
              ('Mesa 1', 4, 'Sala'), ('Mesa 2', 4, 'Sala'),
              ('Barra 1', 2, 'Barra'), ('Terraza 1', 6, 'Terraza')
            """;
        String datosHosteleria = """
            INSERT OR IGNORE INTO productos (nombre, precio, iva_porcentaje, categoria) VALUES
              ('Café solo', 1.20, 10.0, 'Bebidas calientes'),
              ('Café con leche', 1.50, 10.0, 'Bebidas calientes'),
              ('Caña de cerveza', 1.80, 10.0, 'Bebidas frías'),
              ('Tostada con tomate', 2.00, 10.0, 'Comida')
            """;
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(datosMesas.trim());
            stmt.execute(datosHosteleria.trim());
        }

        log.info("Migración v1 completada.");
    }

    /**
     * Ejecuta una sentencia SQL arbitraria (útil para pruebas y herramientas internas).
     */
    public void ejecutarSQL(String sql) throws SQLException {
        try (Statement stmt = conexion.createStatement()) {
            stmt.execute(sql);
        }
    }
}
