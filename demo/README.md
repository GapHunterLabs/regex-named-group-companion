# Cómo probar este plugin

Este plugin ayuda a leer "patrones de búsqueda de texto" (se llaman
expresiones regulares, o "regex") — cuando ese patrón tiene partes
con nombre, el plugin muestra esos nombres directo en el código para
no tener que descifrar el patrón de memoria.

## Nota sobre el tool window "Regex Named Groups" (esta sí necesita tu confirmación)

Antes, el cuadro de texto de prueba del tool window "Regex Named
Groups" no dejaba escribir ni pegar texto — quedó documentado como
pendiente. **Ya se aplicó un fix (versión 0.1.1)**, pero todavía
necesita que alguien confirme en vivo que funciona de verdad:

1. Abrí el tool window (abajo del IDE, secundario — si no lo ves,
   `View → Tool Windows → Regex Named Groups`).
2. Cliqueá dentro del cuadro de texto grande de arriba y escribí
   cualquier cosa (o pegá texto con Ctrl+V).
3. **Si acepta el texto sin problema y no ves ningún artefacto visual
   raro** (una franja o caracteres cortados arriba a la izquierda del
   cuadro), el bug está resuelto — avisame para cerrarlo del todo.
4. **Si sigue sin aceptar texto**, avisame igual — significa que la
   causa raíz identificada no era la única, y hay que seguir
   investigando.

## Qué hacer

1. En el panel de la izquierda, abrí el archivo
   **`OrderReferenceParser.java`** (dentro de `demo` → `src` → `main`
   → `java` → `com` → `acmecorp` → `orders`).
2. Mirá el final de las líneas que tienen patrones de búsqueda.

## Qué deberías ver

- Al final de la línea con 3 partes con nombre: texto extra en gris
  con esos 3 nombres (algo como `year, month, day`).
- Al final de una línea con partes SIN nombre: **no debería aparecer
  ningún texto extra**.
- Al final de una línea que no es un patrón de búsqueda (solo texto
  normal): tampoco debería aparecer nada.

3. Si querés, repetí lo mismo con el archivo
   **`ShipmentTrackingCodes.kt`** (en la carpeta `kotlin` en vez de
   `java`) — debería comportarse igual.

## Si algo no se ve así

Sacá la captura igual, y avisame qué línea no coincide con lo de
arriba (fuera del problema ya conocido del cuadro de texto).
