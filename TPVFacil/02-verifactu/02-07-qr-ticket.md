# 02-07 — QR en el Ticket

#verifactu #implementacion
Relacionado: [[CLAUDE.md]] | [[02-03-clases-java]] | [[03-app-core/03-07-impresion-tickets]]

---

## URL del QR (formato oficial AEAT)

```
https://www2.agenciatributaria.gob.es/es13/h/verifactu
  ?nif=B12345678
  &numserie=A-000123
  &fecha=15-01-2025
  &importe=7.30
```

## Generación con ZXing

```java
String url = String.format(
    "https://www2.agenciatributaria.gob.es/es13/h/verifactu?nif=%s&numserie=%s&fecha=%s&importe=%.2f",
    URLEncoder.encode(nifEmisor, "UTF-8"),
    URLEncoder.encode(numSerie, "UTF-8"),
    URLEncoder.encode(fecha, "UTF-8"),
    importe
);
QRCodeWriter writer = new QRCodeWriter();
BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 150, 150);
BufferedImage qr = MatrixToImageWriter.toBufferedImage(matrix);
```

## Tamaño según Papel

| Papel | Tamaño QR | Tamaño físico |
|-------|-----------|---------------|
| 58mm | 100x100 px | ~2x2 cm |
| 80mm | 150x150 px | ~3x3 cm |

Ancho de papel configurado en [[03-app-core/03-05-configuracion-negocio]].

## Texto Debajo del QR

```
Verifica en: sede.agenciatributaria.gob.es
CSV: ABCD-1234-EFGH-5678
```

## Prompt para Claude

```
Lee [[CLAUDE.md]] y este documento.

Genera QrGenerator.java en com.tpvfacil.verifactu:
- generarQr(Factura factura, String nifEmisor, int anchoPapelMm): BufferedImage
  Tamaño: 100px si 58mm, 150px si 80mm
- construirUrl(Factura factura, String nifEmisor): String (con URL encoding)
```
