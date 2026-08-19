# Demo — Regex Named Group Companion

Dos partes de la feature, dos pasos de verificación distintos.

## Parte 1 — Inlay hint (Java + Kotlin)

1. Abrí `demo/src/main/java/com/acmecorp/orders/OrderReferenceParser.java`
   en el sandbox IDE.
2. Confirmá, línea por línea:

| Línea | Qué debería verse |
|---|---|
| `ORDER_DATE_PATTERN` (3 named groups) | Inlay al final de línea: `year, month, day` |
| `looksLikeAnInvoiceNumber` (`Pattern.matches` directo, sin constante) | Inlay: `region, sequence` -- el hint también debe aparecer en literales usados directamente en un call site, no solo en constantes |
| `LEGACY_ORDER_PATTERN` (grupos sin nombre) | **Sin inlay** -- grupos de captura simples, ninguno nombrado |
| `SUPPORT_EMAIL` (string común) | **Sin inlay** -- no es un regex |

3. Abrí `demo/src/main/kotlin/com/acmecorp/orders/ShipmentTrackingCodes.kt`
   y confirmá lo mismo:

| Línea | Qué debería verse |
|---|---|
| `TRACKING_CODE_PATTERN` (2 named groups) | Inlay: `carrierRegion, trackingId` |
| `Regex(...)` dentro de `isValidTrackingCode` (call site directo) | Inlay: `carrier, code` |
| `LEGACY_PATTERN` (grupos sin nombre) | **Sin inlay** |
| `buildDynamicPattern` (interpolación real `$regionCode`) | **Sin inlay** -- el patrón real no se puede conocer desde el código fuente, nunca debe adivinarse |

## Parte 2 — Tool window "Regex Named Groups"

1. Abrí el tool window (abajo del IDE, secundario -- si no lo ves,
   `View → Tool Windows → Regex Named Groups`).
2. Pattern: `ORD-(?<year>\d{4})-(?<month>\d{2})-(?<day>\d{2})`
3. Sample text:

```
Recent orders: ORD-2026-08-19, ORD-2026-07-02, and a malformed one ORD-26-8-9
```

4. Confirmá:
   - 2 matches resaltados (el malformado no matchea).
   - La sección "Named groups" lista, para cada match, los 3 valores
     reales extraídos (ej. `Match 1: year: "2026", month: "08", day: "19"`).
5. Cambiá el patrón a uno sin named groups (`\d{4}`) y confirmá que la
   sección dice `(pattern has no named groups)`, no queda en blanco ni
   confuso.
6. Cambiá el patrón a uno inválido (`(?<year>\d{4}` sin cerrar) y
   confirmá que aparece el mensaje real de `PatternSyntaxException`.

## Qué reportar

- ¿Los 4 casos de la Parte 1 (Java) se comportan como la tabla dice?
- ¿Los 4 casos de la Parte 1 (Kotlin) se comportan como la tabla dice?
- ¿El tool window extrae los valores reales, en el orden correcto?
- ¿El caso sin grupos y el caso inválido muestran el mensaje correcto,
  sin crashear el panel?
