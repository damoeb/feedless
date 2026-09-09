import { parsePlaceAndArea, stripHtml } from './admin-geo.service';

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

/**
 * Alle Fälle stammen aus echten Antworten von
 * api3.geo.admin.ch/rest/services/ech/SearchServer, abgefragt am 2026-09-09.
 */
describe('parsePlaceAndArea', () => {
  it('reads name and canton out of the bold label', () => {
    expect(parsePlaceAndArea('<b>Riehen (BS)</b>', 'riehen bs')).toEqual({
      place: 'Riehen',
      area: 'BS',
    });
    expect(parsePlaceAndArea('<b>Hedingen (ZH)</b>', 'hedingen zh')).toEqual({
      place: 'Hedingen',
      area: 'ZH',
    });
  });

  it('keeps the casing that detail throws away', () => {
    // detail liefert 'bern be' - daraus wurde "Veranstaltungen in bern".
    expect(parsePlaceAndArea('<b>Bern (BE)</b>', 'bern be')).toEqual({
      place: 'Bern',
      area: 'BE',
    });
  });

  it('keeps hyphens, slashes and accents', () => {
    expect(
      parsePlaceAndArea('<b>La Chaux-de-Fonds (NE)</b>', 'la chaux-de-fonds ne'),
    ).toEqual({ place: 'La Chaux-de-Fonds', area: 'NE' });
    expect(
      parsePlaceAndArea('<b>Biel/Bienne (BE)</b>', 'biel/bienne be'),
    ).toEqual({ place: 'Biel/Bienne', area: 'BE' });
    expect(parsePlaceAndArea('<b>Genève (GE)</b>', 'geneve ge')).toEqual({
      place: 'Genève',
      area: 'GE',
    });
  });

  it('takes the canton from outside the bold part when it sits there', () => {
    expect(
      parsePlaceAndArea('<i>Ort</i> <b>Riehen</b> (BS) - Riehen', 'riehen riehen'),
    ).toEqual({ place: 'Riehen', area: 'BS' });
    expect(
      parsePlaceAndArea(
        '<b>Zug</b> (ZG) - Steinhausen,Baar,Zug',
        'zug steinhausen,baar,zug',
      ),
    ).toEqual({ place: 'Zug', area: 'ZG' });
  });

  it('falls back to detail when the label carries no canton', () => {
    expect(parsePlaceAndArea('<b>Zug</b>', 'zug zg')).toEqual({
      place: 'Zug',
      area: 'ZG',
    });
  });

  it('ignores a detail token that is not a canton code', () => {
    // Sonst stünde hier area: 'HAUSACKER'.
    expect(
      parsePlaceAndArea(
        '<i>Bus</i> <b>Hedingen, Hausacker</b>',
        'hedingen, hausacker 8582721 haltestelle bus',
      ),
    ).toEqual({ place: 'Hedingen, Hausacker', area: '' });
  });

  it('survives the newline the api sometimes puts inside the bold tag', () => {
    expect(
      parsePlaceAndArea(
        '<b>Genève\n</b> (GE) - Genève,Cologny,Carouge (GE)',
        'geneve\n geneve,cologny',
      ),
    ).toEqual({ place: 'Genève', area: 'GE' });
  });

  it('tolerates a missing label or detail', () => {
    expect(parsePlaceAndArea(undefined, undefined)).toEqual({
      place: '',
      area: '',
    });
  });
});
