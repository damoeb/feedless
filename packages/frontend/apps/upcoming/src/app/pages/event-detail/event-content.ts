import { normalizeTitle } from '../../event-dedup';

type ContentEvent = {
  title?: string | null;
  text?: string | null;
};

/** Ab so vielen eigenen Zeichen trägt eine Event-Seite genug, um zu ranken. */
export const MIN_OWN_TEXT_LENGTH = 80;

/**
 * Der Text vieler Events wiederholt nur den Titel. Gemessen am 2026-09-09 über
 * 1265 Events: 45,5 % tragen ausser dem Titel gar nichts, 70,4 % höchstens 80
 * Zeichen. Das hier ist, was nach Abzug des Titels übrig bleibt.
 */
export function ownText(event: ContentEvent): string {
  const text = collapse(event.text);
  const title = collapse(event.title);
  if (!title) {
    return text;
  }
  return collapse(text.split(title).join(' '));
}

/**
 * Ob die Detailseite indexiert werden darf.
 *
 * Bewusst streng: rund 1500 Seiten zu veröffentlichen, die nur ihren Titel
 * wiederholen, wäre dünner Inhalt in grossem Massstab - genau das Problem, das
 * die URL-Konsolidierung eine Ebene höher gerade beseitigt hat. Die Schwelle
 * wirkt automatisch: sobald ein Event echten Text trägt, wird seine Seite von
 * selbst indexierbar, ohne dass jemand etwas umstellt.
 */
export function isIndexable(event: ContentEvent): boolean {
  if (!normalizeTitle(event.title)) {
    return false;
  }
  return ownText(event).length >= MIN_OWN_TEXT_LENGTH;
}

function collapse(value: string | null | undefined): string {
  return (value ?? '').replace(/\s+/g, ' ').trim();
}
