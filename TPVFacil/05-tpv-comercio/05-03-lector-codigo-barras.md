# 05-03 — Lector de Código de Barras

#comercio #implementacion
Relacionado: [[CLAUDE.md]] | [[05-02-pantalla-venta]]
Estado: #pendiente

---

## Cómo Funciona

Los lectores USB simulan un teclado: introducen el código y pulsan Enter. No requieren drivers.

La estrategia: detectar si la entrada al buscador es "humana" (lenta) o del lector (instantánea).

## Algoritmo de Detección

```java
private static final long THRESHOLD_MS = 50; // configurable
private long ultimaTeclaMs = 0;
private StringBuilder bufferBarcode = new StringBuilder();

buscadorField.setOnKeyPressed(event -> {
    long ahora = System.currentTimeMillis();
    long diff = ahora - ultimaTeclaMs;
    ultimaTeclaMs = ahora;

    if (event.getCode() == KeyCode.ENTER) {
        String codigo = bufferBarcode.toString().trim();
        if (!codigo.isEmpty()) buscarPorCodigoBarras(codigo);
        bufferBarcode.setLength(0);
        buscadorField.clear();
    } else if (diff < THRESHOLD_MS) {
        bufferBarcode.append(event.getText()); // entrada rápida = lector
    } else {
        bufferBarcode.setLength(0); // entrada lenta = humano → búsqueda normal
    }
});
```

## Feedback Visual

- Producto encontrado: animación de pulso verde en el total de la cesta
- Producto no encontrado: Snackbar rojo 2 segundos

## Threshold Configurable

Clave: `lector_barras_threshold_ms` en [[03-app-core/03-05-configuracion-negocio|ConfiguracionManager]].
Valor por defecto: `50`.

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.
Añade la detección de lector de código de barras a VentaController.java.
El threshold se lee de ConfiguracionManager.
El buscador debe tener el foco siempre cuando la pantalla de venta está activa.
```
