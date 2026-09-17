import { hasEventTitle } from './event-title';

describe('hasEventTitle', () => {
  it('rejects a title that is only dates', () => {
    expect(hasEventTitle({ title: '19.09.202619.09.2026' })).toBe(false);
  });

  it('rejects a blank or missing title', () => {
    expect(hasEventTitle({ title: '   ' })).toBe(false);
    expect(hasEventTitle({ title: null })).toBe(false);
  });

  it('accepts a title with text besides the date', () => {
    expect(hasEventTitle({ title: '02.10.202502.10.2025 Mittagessen' })).toBe(
      true,
    );
  });
});
