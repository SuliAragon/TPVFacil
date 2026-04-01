# 10-01 — Esquema Completo SQLite

#base-de-datos #referencia
Relacionado: [[CLAUDE.md]] | [[03-app-core/03-02-base-de-datos-sqlite]] | [[10-02-migraciones]]

---

## Tablas Core

### `configuracion`
```sql
CREATE TABLE IF NOT EXISTS configuracion (
  clave TEXT PRIMARY KEY,
  valor TEXT
);
```
Claves → ver [[03-app-core/03-05-configuracion-negocio]].

### `productos`
```sql
CREATE TABLE IF NOT EXISTS productos (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre      TEXT NOT NULL,
  precio      REAL NOT NULL,
  iva_porcentaje REAL NOT NULL DEFAULT 21.0,
  categoria   TEXT,
  codigo_barras TEXT UNIQUE,
  stock       INTEGER DEFAULT -1,  -- -1 = sin control
  activo      INTEGER DEFAULT 1,
  fecha_creacion TEXT DEFAULT (datetime('now'))
);
```

### `clientes`
```sql
CREATE TABLE IF NOT EXISTS clientes (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL, nif TEXT UNIQUE,
  direccion TEXT, codigo_postal TEXT, ciudad TEXT,
  telefono TEXT, email TEXT,
  fecha_creacion TEXT DEFAULT (datetime('now'))
);
```

### `facturas`
```sql
CREATE TABLE IF NOT EXISTS facturas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  serie TEXT NOT NULL DEFAULT 'A',
  numero INTEGER NOT NULL,
  fecha TEXT NOT NULL,
  cliente_id INTEGER REFERENCES clientes(id),
  base_imponible REAL NOT NULL,
  cuota_iva REAL NOT NULL,
  total REAL NOT NULL,
  forma_pago TEXT NOT NULL,        -- EFECTIVO, TARJETA, MIXTO
  efectivo_entregado REAL DEFAULT 0,
  cambio REAL DEFAULT 0,
  tipo_negocio TEXT NOT NULL,      -- HOSTELERIA, COMERCIO
  anulada INTEGER DEFAULT 0,
  UNIQUE(serie, numero)
);
```

### `lineas_factura`
```sql
CREATE TABLE IF NOT EXISTS lineas_factura (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_id INTEGER NOT NULL REFERENCES facturas(id),
  producto_id INTEGER REFERENCES productos(id),
  descripcion TEXT NOT NULL,
  cantidad REAL NOT NULL,
  precio_unitario REAL NOT NULL,
  iva_porcentaje REAL NOT NULL,
  subtotal REAL NOT NULL
);
```

### `registros_verifactu`
```sql
CREATE TABLE IF NOT EXISTS registros_verifactu (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  factura_id INTEGER NOT NULL REFERENCES facturas(id),
  huella TEXT NOT NULL,
  huella_anterior TEXT NOT NULL,
  firma TEXT NOT NULL,
  xml_enviado TEXT,
  csv_aeat TEXT,
  estado TEXT NOT NULL DEFAULT 'PENDIENTE',  -- OK, PENDIENTE, ERROR
  error_descripcion TEXT,
  fecha_envio TEXT,
  intentos INTEGER DEFAULT 0
);
```

### `caja`
```sql
CREATE TABLE IF NOT EXISTS caja (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  fecha_apertura TEXT NOT NULL,
  fecha_cierre TEXT,
  efectivo_inicial REAL DEFAULT 0,
  total_efectivo REAL DEFAULT 0,
  total_tarjeta REAL DEFAULT 0,
  total_ventas INTEGER DEFAULT 0,
  estado TEXT DEFAULT 'ABIERTA'
);
```

## Tablas Hostelería

### `mesas`
```sql
CREATE TABLE IF NOT EXISTS mesas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  capacidad INTEGER DEFAULT 4,
  zona TEXT DEFAULT 'Sala',
  estado TEXT DEFAULT 'LIBRE',   -- LIBRE, OCUPADA, PENDIENTE_PAGO
  activa INTEGER DEFAULT 1
);
```

### `comandas` + `lineas_comanda`
```sql
CREATE TABLE IF NOT EXISTS comandas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  mesa_id INTEGER NOT NULL REFERENCES mesas(id),
  fecha_apertura TEXT NOT NULL,
  fecha_cierre TEXT,
  num_comensales INTEGER DEFAULT 1,
  estado TEXT DEFAULT 'ABIERTA',    -- ABIERTA, CERRADA, PAGADA
  factura_id INTEGER REFERENCES facturas(id),
  observaciones TEXT
);

CREATE TABLE IF NOT EXISTS lineas_comanda (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  comanda_id INTEGER NOT NULL REFERENCES comandas(id),
  producto_id INTEGER NOT NULL REFERENCES productos(id),
  cantidad REAL NOT NULL DEFAULT 1,
  precio_unitario REAL NOT NULL,
  enviado_cocina INTEGER DEFAULT 0,
  estado TEXT DEFAULT 'PENDIENTE',  -- PENDIENTE, EN_COCINA, SERVIDO
  observaciones TEXT,
  fecha_pedido TEXT DEFAULT (datetime('now'))
);
```

## Índices

```sql
CREATE INDEX IF NOT EXISTS idx_facturas_fecha ON facturas(fecha);
CREATE INDEX IF NOT EXISTS idx_productos_barras ON productos(codigo_barras);
CREATE INDEX IF NOT EXISTS idx_registros_estado ON registros_verifactu(estado);
CREATE INDEX IF NOT EXISTS idx_comandas_mesa ON comandas(mesa_id, estado);
```
