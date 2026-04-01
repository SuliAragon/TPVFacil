# 03-03 — Modelos de Datos

#app-core #fase-1
Relacionado: [[CLAUDE.md]] | [[01-arquitectura/01-03-estructura-paquetes-java]] | [[10-base-de-datos/10-01-esquema-completo]]
Estado: #pendiente

---

## Paquete `com.tpvfacil.core.modelo`

### Producto
```
id, nombre*, precio*, ivaPorcentaje*, categoria, codigoBarras, stock(-1=sin control), activo
```

### Cliente
```
id, nombre*, nif, direccion, codigoPostal, ciudad, telefono, email
```

### Factura
```
id, serie, numero, fecha*, cliente(nullable), lineas,
baseImponible*, cuotaIva*, total*, formaPago*, tipoNegocio*, anulada
```

### LineaFactura
```
id, facturaId, productoId, descripcion*(snapshot), cantidad*, precioUnitario*, ivaPorcentaje*, subtotal*
```

### Enums
```java
FormaPago:   EFECTIVO, TARJETA, MIXTO
TipoNegocio: HOSTELERIA, COMERCIO
```

## Paquete `com.tpvfacil.hosteleria.modelo`

### Mesa
```
id, nombre*, capacidad, zona, estado(EstadoMesa), activa
```

### EstadoMesa (enum)
```java
LIBRE, OCUPADA, PENDIENTE_PAGO
// getDescripcion() → "Libre", "Ocupada", "Pendiente de pago"
// getColor() → "#27AE60", "#E74C3C", "#F39C12"
```

### Comanda
```
id, mesaId*, fechaApertura*, fechaCierre, numComensales, estado, facturaId, lineas
```

### LineaComanda
```
id, comandaId*, productoId*, nombreProducto*(snapshot), cantidad*, precioUnitario*,
enviadoCocina, estado(PENDIENTE/EN_COCINA/SERVIDO), observaciones
```

## Paquete `com.tpvfacil.comercio.modelo`

### ItemCesta
```
producto, cantidad, subtotal (calculado)
```

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera TODOS los modelos listados. Para cada clase:
- Constructor vacío + constructor con todos los campos
- Getters y setters estándar Java
- toString() con los campos principales
- equals() y hashCode() por id
- Javadoc en español

Los enums deben implementar: getDescripcion() en español y getColor() en hex.
```
