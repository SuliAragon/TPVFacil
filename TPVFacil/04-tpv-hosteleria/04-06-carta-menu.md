# 04-06 — Carta y Menú

#hosteleria #implementacion
Relacionado: [[CLAUDE.md]] | [[04-03-comandas]] | [[10-base-de-datos/10-01-esquema-completo]]
Estado: #pendiente

---

## Estructura

```
Categorías (con orden configurable):
├── Bebidas calientes
│   ├── Café solo (1,20€ — IVA 10%)
│   └── Café con leche (1,50€ — IVA 10%)
├── Bebidas frías
│   └── Caña de cerveza (1,80€ — IVA 10%)
└── Comida
    └── Tostada con tomate (2,00€ — IVA 10%)
```

IVA por defecto en hostelería: **10%** (configurable por producto).

## Gestión de la Carta

Pantalla accesible desde Menú → Carta y productos.

- Drag & drop para reordenar categorías
- Añadir/editar/desactivar productos
- Imagen opcional por producto (guardada en AppData/TPVFacil/imagenes/)
- Los productos desactivados no aparecen en la carta pero sí en el historial

## Prompt para Claude

```
Lee [[CLAUDE.md]], [[10-base-de-datos/10-01-esquema-completo]] y este documento.

Genera:
1. pantalla-gestion-carta.fxml + GestionCartaController.java
   Panel izq: ListView de categorías con drag&drop para reordenar
   Panel der: TableView de productos de la categoría seleccionada
   Botones: añadir/editar categoría, añadir/editar/activar-desactivar producto

2. Modales: dialogo-categoria.fxml + dialogo-producto.fxml
   Producto: nombre*, precio*, IVA (4/10/21%)*, categoría*, código barras, imagen, activo
```
