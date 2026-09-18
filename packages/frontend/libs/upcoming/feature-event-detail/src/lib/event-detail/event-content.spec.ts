import { isIndexable, MIN_OWN_TEXT_LENGTH, ownText } from './event-content';

describe('ownText', () => {
  it('is empty when the text only repeats the title', () => {
    expect(
      ownText({ title: 'Chilbi Baar', text: 'Chilbi Baar' }),
    ).toBe('');
  });

  it('collapses the whitespace noise the scrapers leave behind', () => {
    expect(
      ownText({
        title: 'Chilbi Baar',
        text: 'Chilbi Baar\n\n   \n Drei Tage Musik und Marktstände.',
      }),
    ).toBe('Drei Tage Musik und Marktstände.');
  });

  it('returns the whole text when there is no title', () => {
    expect(ownText({ title: '', text: 'Ein Abend mit Musik' })).toBe(
      'Ein Abend mit Musik',
    );
  });

  it('tolerates missing fields', () => {
    expect(ownText({})).toBe('');
  });
});

/**
 * Gemessen am 2026-09-09 über 1265 Events: 45,5 % tragen ausser dem Titel
 * nichts, 70,4 % höchstens 80 Zeichen. Ohne diese Schwelle würden rund 1500
 * Seiten veröffentlicht, die im Wesentlichen ihren Titel wiederholen.
 */
describe('isIndexable', () => {
  const longEnough = 'x'.repeat(MIN_OWN_TEXT_LENGTH);
  const tooShort = 'x'.repeat(MIN_OWN_TEXT_LENGTH - 1);

  it('accepts an event that carries its own text', () => {
    expect(
      isIndexable({ title: 'Chilbi Baar', text: `Chilbi Baar ${longEnough}` }),
    ).toBe(true);
  });

  it('rejects an event whose text only repeats the title', () => {
    expect(isIndexable({ title: 'Chilbi Baar', text: 'Chilbi Baar' })).toBe(
      false,
    );
  });

  it('rejects a text that is too short to rank for anything', () => {
    expect(isIndexable({ title: 'Chilbi Baar', text: 'Chilbi Baar Musik' })).toBe(
      false,
    );
  });

  it('draws the line exactly at the threshold', () => {
    expect(
      isIndexable({ title: 'Chilbi Baar', text: `Chilbi Baar ${tooShort}` }),
    ).toBe(false);
    expect(
      isIndexable({ title: 'Chilbi Baar', text: `Chilbi Baar ${longEnough}` }),
    ).toBe(true);
  });

  it('rejects an event without a title', () => {
    expect(isIndexable({ title: '   ', text: longEnough })).toBe(false);
  });
});
