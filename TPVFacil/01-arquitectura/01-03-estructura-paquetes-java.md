# 01-03 — Estructura de Paquetes Java

#arquitectura #referencia
Relacionado: [[CLAUDE.md]] | [[01-01-estructura-proyecto]] | [[03-app-core/03-03-modelos-datos]]

---

## Árbol de Paquetes

```
com.tpvfacil
│
├── Main.java                         ← [[03-app-core/03-01-main-java]]
│
├── core/
│   ├── modelo/                       ← [[03-app-core/03-03-modelos-datos]]
│   │   ├── Producto.java
│   │   ├── Cliente.java
│   │   ├── Factura.java
│   │   ├── LineaFactura.java
│   │   ├── FormaPago.java            (enum)
│   │   └── TipoNegocio.java          (enum)
│   ├── db/                           ← [[10-base-de-datos/10-01-esquema-completo]]
│   │   ├── DatabaseManager.java
│   │   ├── ProductoRepository.java
│   │   ├── ClienteRepository.java
│   │   ├── FacturaRepository.java
│   │   ├── RegistroVerifactuRepository.java
│   │   ├── MesaRepository.java
│   │   ├── ComandaRepository.java
│   │   ├── ConfiguracionRepository.java
│   │   └── CajaRepository.java
│   ├── ui/
│   │   ├── InicioController.java     ← [[03-app-core/03-01-main-java]]
│   │   ├── ConfiguracionController.java
│   │   ├── CierreCajaController.java ← [[03-app-core/03-06-cierre-caja]]
│   │   └── componentes/
│   │       └── TarjetaMesa.java      ← [[04-tpv-hosteleria/04-02-gestion-mesas]]
│   └── util/
│       ├── BackupManager.java        ← [[10-base-de-datos/10-03-backup]]
│       └── MonedaUtil.java
│
├── verifactu/                        ← [[02-verifactu/02-03-clases-java]]
│   ├── VerifactuManager.java
│   ├── RegistroFactura.java
│   ├── HashChain.java
│   ├── FirmaDigital.java
│   ├── XmlGenerator.java
│   ├── AeatClient.java
│   ├── CertificadoManager.java
│   ├── QrGenerator.java              ← [[02-verifactu/02-07-qr-ticket]]
│   └── excepciones/
│       ├── VerifactuException.java
│       └── AeatException.java
│
├── hosteleria/                       ← [[04-tpv-hosteleria/04-01-vision-general]]
│   ├── modelo/
│   │   ├── Mesa.java
│   │   ├── EstadoMesa.java
│   │   ├── Comanda.java
│   │   └── LineaComanda.java
│   ├── controller/
│   │   ├── MesasController.java      ← [[04-tpv-hosteleria/04-02-gestion-mesas]]
│   │   ├── ComandaController.java    ← [[04-tpv-hosteleria/04-03-comandas]]
│   │   └── CobroHosteleriaController.java ← [[04-tpv-hosteleria/04-05-cobro-hosteleria]]
│   └── service/
│       ├── ComandaService.java
│       └── MesaService.java
│
├── comercio/                         ← [[05-tpv-comercio/05-01-vision-general]]
│   ├── modelo/
│   │   └── ItemCesta.java
│   ├── controller/
│   │   ├── VentaController.java      ← [[05-tpv-comercio/05-02-pantalla-venta]]
│   │   ├── CobroComercioController.java ← [[05-tpv-comercio/05-06-cobro-comercio]]
│   │   └── DevolucionController.java ← [[05-tpv-comercio/05-05-devoluciones]]
│   └── service/
│       ├── VentaService.java
│       └── StockService.java         ← [[05-tpv-comercio/05-04-gestion-stock]]
│
├── licencia/                         ← [[06-licencias/06-02-activacion-clave]]
│   ├── LicenciaManager.java
│   ├── ModoDemo.java                 ← [[06-licencias/06-01-modo-demo]]
│   ├── HardwareFingerprint.java      ← [[06-licencias/06-04-hardware-fingerprint]]
│   ├── ActivacionController.java
│   └── GeneradorClaves.java          ← Herramienta interna (main propio)
│
└── config/
    └── ConfiguracionManager.java     ← [[03-app-core/03-05-configuracion-negocio]]
```

---

## Convenciones de Código

| Elemento | Estilo | Ejemplo |
|----------|--------|---------|
| Clases | PascalCase | `VerifactuManager` |
| Métodos | camelCase | `procesarFactura()` |
| Constantes | UPPER_SNAKE | `MAX_TICKETS_DEMO` |
| FXML | kebab-case | `pantalla-mesas.fxml` |
| Javadoc | Español | `/** Procesa una factura... */` |
