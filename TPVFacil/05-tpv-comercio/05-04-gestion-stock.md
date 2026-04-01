# 05-04 — Gestión de Stock

#comercio #implementacion
Relacionado: [[CLAUDE.md]] | [[05-06-cobro-comercio]] | [[10-base-de-datos/10-01-esquema-completo]]
Estado: #pendiente

---

## Modelo

- `stock = -1` → sin control
- `stock = 0` → agotado
- `stock > 0` → unidades disponibles

## Flujo Automático

Al confirmar cobro: `StockService.descontarStock(factura)` descuenta una unidad por cada línea vendida. Ver [[05-06-cobro-comercio]].

## Alertas Stock Bajo

Umbral por defecto: 5 unidades. Se comprueba al arrancar y al cerrar caja → [[03-app-core/03-06-cierre-caja]].
Si hay productos bajo umbral: banner naranja en pantalla de inicio.

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera StockService.java en com.tpvfacil.comercio.service:
- descontarStock(Factura factura)
- reponerStock(int productoId, int cantidad)
- ajustarStock(Map<Integer,Integer> stockPorId): transacción única
- getProductosBajoStock(int umbral)
- getProductosAgotados()

Genera pantalla-gestion-stock.fxml + GestionStockController.java:
- TableView: nombre, categoría, stock (editable), alerta visual (rojo/naranja)
- Botón "Guardar cambios" → ajustarStock()
```
