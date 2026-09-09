/**
 * Viele Quellen schreiben das Datum in den Titel. Auf einer Seite, die das
 * Datum ohnehin nennt, ist das Rauschen.
 */
export function cleanEventTitle(title: string | null | undefined): string {
  if (!title?.trim()) {
    return title ?? '';
  }
  // Deutsches Datum, z.B. "13. März 2026", "1. Januar 2026"
  const withoutNamedMonth = title.replace(
    /[0-9]{1,2}\.[\s.]*[a-zäöüß]{3,10}[\s.]*[0-9]{2,4}/gi,
    '',
  );
  // Numerisches Datum, z.B. "13.03.2026", "1.1.26"
  const withoutNumericDate = withoutNamedMonth.replace(
    /[0-9]{1,2}\.[\s.]*[0-9]{1,2}[\s.]*[0-9]{2,4}/g,
    '',
  );
  return withoutNumericDate.replace(/\s{2,}/g, ' ').trim();
}
