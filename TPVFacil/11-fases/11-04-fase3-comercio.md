# 11-04 — Fase 3: TPV Comercio

#fase #fase-3
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[05-tpv-comercio/05-01-vision-general]]
Prerequisito: [[11-02-fase1-nucleo|Fase 1 ✅]] | [[11-03-fase2-verifactu|Fase 2 ✅]]
Estado: #pendiente

---

## Criterio de Éxito

- ✅ Pantalla de venta carga catálogo de productos
- ✅ Búsqueda filtra en tiempo real
- ✅ Lector USB añade producto automáticamente
- ✅ Flujo completo: venta → cobro → ticket → AEAT
- ✅ Devoluciones generan factura rectificativa R1
- ✅ Stock se descuenta al cobrar
- ✅ Cierre de caja con backup automático

---

## Tareas

### 3.1 — Pantalla de venta + lector barras
```
Lee [[CLAUDE.md]], [[05-tpv-comercio/05-02-pantalla-venta]] y [[05-tpv-comercio/05-03-lector-codigo-barras]].
Genera pantalla-venta.fxml + VentaController.java con detección de lector USB.
```

### 3.2 — Gestión de productos
```
Lee [[CLAUDE.md]] y [[04-tpv-hosteleria/04-06-carta-menu]].
Misma pantalla que la carta de hostelería pero con IVA configurable (4/10/21%).
```

### 3.3 — StockService
```
Lee [[CLAUDE.md]] y [[05-tpv-comercio/05-04-gestion-stock]].
Genera StockService.java + pantalla-gestion-stock.fxml.
```

### 3.4 — Pantalla de cobro + VentaService
```
Lee [[CLAUDE.md]], [[05-tpv-comercio/05-06-cobro-comercio]] y [[02-verifactu/02-02-flujo-tecnico]].
Genera pantalla-cobro-comercio.fxml + CobroComercioController.java + VentaService.java.
```

### 3.5 — Devoluciones
```
Lee [[CLAUDE.md]] y [[05-tpv-comercio/05-05-devoluciones]].
Genera pantalla-devolucion.fxml + DevolucionController.java.
```

### 3.6 — Cierre de caja
```
Lee [[CLAUDE.md]], [[03-app-core/03-06-cierre-caja]] y [[10-base-de-datos/10-03-backup]].
Genera CierreCajaController.java + pantalla-cierre-caja.fxml.
```

---

## Datos de Prueba

```sql
INSERT INTO productos (nombre, precio, iva_porcentaje, categoria, codigo_barras, stock) VALUES
  ('Agua mineral 1L', 0.79, 10.0, 'Bebidas', '8410207000016', 50),
  ('Coca-Cola 330ml', 1.29, 21.0, 'Bebidas', '5449000000996', 30),
  ('Pan de molde', 1.49, 4.0, 'Alimentación', '8410011009980', 20),
  ('Producto sin barras', 5.00, 21.0, 'Otros', NULL, -1);
```

---

## Verificación

```
1. Buscar "agua" → aparece producto
2. Escanear código de Coca-Cola → se añade a cesta
3. Cobrar 10€ en efectivo → cambio correcto
4. Ticket con QR y CSV de la AEAT
5. Stock Coca-Cola bajó
6. Devolución del ticket anterior → factura R1 en Verifactu
7. Cierre de caja → totales + backup generado
```
