# 02-01 — ¿Qué es Verifactu?

#verifactu #referencia
Relacionado: [[CLAUDE.md]] | [[02-02-flujo-tecnico]] | [[02-05-api-aeat]] | [[11-fases/11-03-fase2-verifactu]]

---

## Definición Simple

Verifactu obliga a que **cada factura emitida se registre en la AEAT en tiempo real**, formando una cadena inalterable de registros. El objetivo es eliminar el fraude fiscal por software de doble contabilidad.

## ¿Qué Exige al Software?

1. Generar un **registro XML** por factura → [[02-04-xml-schema]]
2. Calcular un **hash SHA-256 encadenado** → [[02-03-clases-java]]
3. **Firmar digitalmente** con el certificado del negocio → [[02-06-certificados]]
4. **Enviar a la API de la AEAT** en tiempo real → [[02-05-api-aeat]]
5. Imprimir un **QR** en el ticket → [[02-07-qr-ticket]]

## Dos Modos en Nuestro Software

| | MODO DEMO | MODO COMPLETO |
|---|---|---|
| Endpoint | Sandbox AEAT | Producción AEAT |
| Validez fiscal | ❌ No | ✅ Sí |

Ver [[06-licencias/06-01-modo-demo]] y [[06-licencias/06-02-activacion-clave]].

## El módulo Verifactu es Compartido

Lo usan tanto [[04-tpv-hosteleria/04-05-cobro-hosteleria]] como [[05-tpv-comercio/05-06-cobro-comercio]].
Hay **una sola instancia** de `VerifactuManager` en el sistema.
