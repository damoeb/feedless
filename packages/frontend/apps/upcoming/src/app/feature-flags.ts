/**
 * Die Event-Detailseiten sind gebaut, aber abgeschaltet: die Liste verlinkt
 * bis auf Weiteres direkt zur Quelle.
 *
 * Der Grund ist der Bestand, nicht die Seite. Gemessen am 2026-09-09 über 1265
 * Events der nächsten 30 Tage:
 *
 * - 45,5 % tragen ausser dem Titel keinen eigenen Text, 70,4 % höchstens 80 Zeichen
 * - 95,9 % starten um 10:00, weil die Pipeline diesen Wert stempelt, wenn sie
 *   keine Zeit erkennt
 * - 0 % haben ein Bild
 * - bei einem Teil steckt der Text bereits im Titelfeld, weil Titel und
 *   abgeschnittener Fliesstext beim Scrapen zusammengezogen wurden
 *
 * Eine eigene Seite kann daraus heute nicht mehr zeigen als der Titel und ein
 * Link nach draussen. Sie einzuschalten hiesse, Nutzer über einen Zwischenhalt
 * zu schicken, der ihnen nichts gibt.
 *
 * Umlegen, sobald zwei Dinge stimmen: die Pipeline liefert Beginn, Ende, Ort
 * und Beschreibung als eigene Felder, und Nutzer können Events korrigieren
 * und ergänzen. Siehe „Custom Attributes" und „Events bearbeiten" in
 * docs/tasks.md.
 *
 * Die Route, der Resolver und die Seite selbst bleiben bestehen, ebenso die
 * Weiterleitungen der alten Deeplinks - nur die Verlinkung aus der Liste
 * hängt an diesem Schalter.
 */
export const EVENT_DETAIL_PAGES_ENABLED = false;
