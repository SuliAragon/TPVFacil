# 00-01 — Glosario del Proyecto

#meta #referencia
Relacionado: [[CLAUDE.md]] | [[00-02-decisiones-pendientes]] | [[00-03-arbol-dependencias]]

---

## Términos de Negocio

| Término | Definición |
|---------|-----------|
| **TPV** | Terminal Punto de Venta. El software que usa el negocio para cobrar. |
| **Ticket** | Justificante de compra simplificado. Sin datos del comprador. |
| **Factura simplificada** | Equivalente legal al ticket. Lo que genera el TPV en cada venta. |
| **Factura completa** | Con datos del comprador (NIF, nombre, dirección). |
| **Verifactu** | Sistema de la AEAT que registra cada factura en tiempo real. Ver [[02-verifactu/02-01-que-es-verifactu]]. |
| **AEAT** | Agencia Estatal de Administración Tributaria (Hacienda). |
| **CSV AEAT** | Código Seguro de Verificación. Código que devuelve la AEAT al confirmar cada factura. |
| **Hash encadenado** | Huella SHA-256 de cada factura que incluye la huella de la anterior. Ver [[02-verifactu/02-03-clases-java]]. |
| **Certificado digital** | Archivo .p12/.pfx que firma digitalmente las facturas. Ver [[02-verifactu/02-06-certificados]]. |
| **Modo Demo** | Estado sin activar. Limitado a 20 tickets/día. Ver [[06-licencias/06-01-modo-demo]]. |
| **Modo Completo** | Estado tras introducir la clave de licencia. Sin límites. Ver [[06-licencias/06-02-activacion-clave]]. |
| **Hardware fingerprint** | ID único del equipo. Vincula la licencia al PC. Ver [[06-licencias/06-04-hardware-fingerprint]]. |
| **Sandbox AEAT** | Entorno de pruebas. Sin validez fiscal real. Ver [[02-verifactu/02-05-api-aeat]]. |
| **Comanda** | Pedido de una mesa en hostelería. Ver [[04-tpv-hosteleria/04-03-comandas]]. |
| **Cierre de caja** | Proceso de fin de jornada. Ver [[03-app-core/03-06-cierre-caja]]. |

---

## Términos Técnicos

| Término | Definición |
|---------|-----------|
| **JavaFX** | Framework de interfaces gráficas para Java. Usa FXML para las pantallas. |
| **FXML** | Formato XML para definir interfaces JavaFX. |
| **SQLite** | Base de datos en un archivo. Sin servidor. Ver [[10-base-de-datos/10-01-esquema-completo]]. |
| **Maven** | Gestor de dependencias Java. Configurado en [[01-arquitectura/01-04-configuracion-maven]]. |
| **jpackage** | Herramienta que genera el instalador .exe con JRE incluido. Ver [[07-instalador/07-01-jpackage]]. |
| **JAXB** | Java XML Binding. Genera el XML de Verifactu. Ver [[02-verifactu/02-04-xml-schema]]. |
| **BouncyCastle** | Librería de criptografía Java. Firma los registros Verifactu. Ver [[02-verifactu/02-06-certificados]]. |
| **SHA-256** | Algoritmo de hash. Genera la huella de cada factura. Ver [[02-verifactu/02-03-clases-java]]. |
| **Schema.org** | Estándar de marcado semántico para SEO. Ver [[09-seo/09-04-seo-tecnico]]. |
| **Core Web Vitals** | Métricas de rendimiento de Google. Ver [[09-seo/09-04-seo-tecnico]]. |
