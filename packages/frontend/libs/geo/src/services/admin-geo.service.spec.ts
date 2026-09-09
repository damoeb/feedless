import { stripHtml } from './admin-geo.service';

/**
 * Die admin.ch SearchServer-Antwort liefert `label` mit einer eigenen
 * Treffer-Hervorhebung, etwa `<b>Zug (ZG)</b>`. Ungefiltert übernommen landet
 * dieses Markup in `displayName` und damit im Seitentitel, in der
 * Meta-Description und im Fließtext, wo Angular es escaped und der Nutzer
 * `<b>Zug</b>` als Text sieht. Die App hebt Treffer ohnehin selbst hervor.
 */
describe('stripHtml', () => {
  it('removes the highlight the search server adds', () => {
    expect(stripHtml('<b>Zug (ZG)</b>')).toBe('Zug (ZG)');
    expect(stripHtml('<b>Zug</b>')).toBe('Zug');
  });

  it('keeps the text around the highlight', () => {
    expect(
      stripHtml('<b>Zug</b> (ZG) - Steinhausen,Baar,Zug'),
    ).toBe('Zug (ZG) - Steinhausen,Baar,Zug');
  });

  it('leaves a plain label untouched', () => {
    expect(stripHtml('6300 Zug')).toBe('6300 Zug');
  });

  it('collapses the whitespace a removed tag leaves behind', () => {
    expect(stripHtml('<b>Zug</b>  <b>ZG</b>')).toBe('Zug ZG');
  });

  it('removes every tag, not just the bold one', () => {
    // Die API klassifiziert Treffer zusätzlich: `<i>Ort</i>`, `<i>Quartier</i>`.
    expect(stripHtml('<i>Ort</i> <b>Riehen</b> (BS) - Riehen')).toBe(
      'Ort Riehen (BS) - Riehen',
    );
  });

  it('keeps a slash in a bilingual name intact', () => {
    expect(stripHtml('<b>Biel/Bienne (BE)</b>')).toBe(
      'Biel/Bienne (BE)',
    );
  });

  it('tolerates an empty or missing label', () => {
    expect(stripHtml('')).toBe('');
    expect(stripHtml(undefined)).toBe('');
  });
});
