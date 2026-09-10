package org.migor.feedless.template

/**
 * Welche Vorlagenvariante ein Versand verwenden soll.
 *
 * Das Backend bleibt generisch: es kennt keine Produkte und verzweigt nirgends
 * auf eines. Es bekommt lediglich einen Variantennamen mitgeteilt und sucht
 * zuerst nach `<vorlage>-<variante>`, bevor es auf `<vorlage>` zurückfällt.
 * Ein Produkt wie upcoming legt damit eigene Vorlagen daneben, ohne dass eine
 * Zeile im Versandpfad davon weiss.
 */
@JvmInline
value class TemplateVariant(val value: String) {
  init {
    require(value.isNotBlank()) { "template variant must not be blank" }
  }
}
