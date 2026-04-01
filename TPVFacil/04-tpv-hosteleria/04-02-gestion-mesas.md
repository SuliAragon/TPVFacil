# 04-02 — Gestión de Mesas

#hosteleria #implementacion
Relacionado: [[CLAUDE.md]] | [[04-01-vision-general]] | [[04-03-comandas]] | [[03-app-core/03-03-modelos-datos]]
Estado: #pendiente

---

## Pantalla Principal

Grid de tarjetas (`TarjetaMesa`) en `FlowPane`. Se adapta al tamaño de la ventana.

Cada tarjeta muestra:
- Nombre de la mesa
- Zona (Sala, Terraza, Barra...)
- Capacidad
- Estado → color de fondo
- Tiempo abierta si OCUPADA o PENDIENTE (formato `1h 23m`, se actualiza cada minuto)

## Refresco Automático

```java
// Cada 30 segundos recargar estado de mesas desde BD
Timeline timeline = new Timeline(
    new KeyFrame(Duration.seconds(30), e -> cargarMesas())
);
timeline.setCycleCount(Timeline.INDEFINITE);
timeline.play();
```

## CRUD de Mesas

Menú contextual (clic derecho) o botón ⚙️:
- Añadir mesa (modal: nombre, capacidad, zona)
- Editar mesa
- Eliminar mesa (solo si LIBRE)
- "Importar mesas": crear N mesas numeradas de golpe

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[04-01-vision-general]] y este documento.

Genera:
1. TarjetaMesa.java en com.tpvfacil.core.ui.componentes (extiende StackPane)
   Campos: nombre, zona, capacidad, estado (color), tiempo abierta
   El tiempo se actualiza con Timeline interno cada 60 segundos.

2. pantalla-mesas.fxml + MesasController.java
   FlowPane de TarjetaMesa. Refresco cada 30s. Menú contextual.
   Botones superiores: [+ Añadir mesa] [Cierre de caja] [⚙ Configuración] [← Inicio]

3. Modal CRUD de mesas: dialogo-mesa.fxml + DialogoMesaController.java
   Validar que nombre no esté vacío. No borrar mesa con comanda activa.
```
