# 11-05 — Fase 4: TPV Hostelería

#fase #fase-4
Relacionado: [[CLAUDE.md]] | [[11-01-roadmap]] | [[04-tpv-hosteleria/04-01-vision-general]]
Prerequisito: [[11-02-fase1-nucleo|Fase 1 ✅]] | [[11-03-fase2-verifactu|Fase 2 ✅]]
Estado: #pendiente

---

## Criterio de Éxito

- ✅ Pantalla de mesas muestra estados con colores correctos
- ✅ Se puede abrir comanda en mesa libre
- ✅ Productos se añaden desde la carta
- ✅ "Enviar a cocina" imprime en impresora cocina
- ✅ Cobro con Verifactu correcto
- ✅ Mesa vuelve a LIBRE tras el cobro
- ✅ Refresco automático cada 30 segundos

---

## Tareas

### 4.1 — TarjetaMesa + Pantalla de mesas
```
Lee [[CLAUDE.md]], [[04-tpv-hosteleria/04-02-gestion-mesas]] y [[04-tpv-hosteleria/04-01-vision-general]].
Genera TarjetaMesa.java (componente JavaFX) + pantalla-mesas.fxml + MesasController.java.
CRUD de mesas con modal.
```

### 4.2 — Carta de hostelería
```
Lee [[CLAUDE.md]] y [[04-tpv-hosteleria/04-06-carta-menu]].
Genera gestión de carta con categorías y drag & drop.
```

### 4.3 — Pantalla de comanda
```
Lee [[CLAUDE.md]], [[04-tpv-hosteleria/04-03-comandas]] y [[04-tpv-hosteleria/04-04-impresora-cocina]].
Genera pantalla-comanda.fxml + ComandaController.java + ComandaService.java.
Añade imprimirComanda() a TicketPrinter.
```

### 4.4 — Cobro hostelería
```
Lee [[CLAUDE.md]], [[04-tpv-hosteleria/04-05-cobro-hosteleria]] y [[02-verifactu/02-02-flujo-tecnico]].
Genera pantalla-cobro-hosteleria.fxml + CobroHosteleriaController.java.
Incluir modal de división de cuenta.
```

---

## Datos de Prueba

```sql
INSERT INTO mesas (nombre, capacidad, zona) VALUES
  ('Mesa 1', 4, 'Sala'), ('Mesa 2', 4, 'Sala'),
  ('Barra 1', 2, 'Barra'), ('Terraza 1', 6, 'Terraza');

INSERT INTO productos (nombre, precio, iva_porcentaje, categoria) VALUES
  ('Café solo', 1.20, 10.0, 'Bebidas calientes'),
  ('Café con leche', 1.50, 10.0, 'Bebidas calientes'),
  ('Caña de cerveza', 1.80, 10.0, 'Bebidas frías'),
  ('Tostada con tomate', 2.00, 10.0, 'Comida');
```

---

## Verificación

```
1. Abrir hostelería → pantalla de mesas (todas verdes)
2. Clic Mesa 1 → nueva comanda
3. Añadir 2x Café con leche + 1x Tostada
4. "Enviar a cocina" → imprime o avisa si no hay impresora
5. Añadir 1x Caña (sin enviar a cocina)
6. "Cobrar" en tarjeta → ticket + Mesa 1 vuelve a verde
7. Probar división: Mesa 2, productos, dividir entre 3
```
