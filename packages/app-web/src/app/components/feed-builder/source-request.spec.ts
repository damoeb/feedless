import { formatSourceRequest, parseSourceRequest } from './source-request';
import { GqlSourceInput } from '../../../generated/graphql';

describe('source-request', () => {
  const source: GqlSourceInput = {
    title: 'Example',
    tags: ['news'],
    flow: {
      sequence: [{ fetch: { get: { url: { literal: 'https://example.com' } } } }],
    },
  };

  describe('formatSourceRequest', () => {
    it('ignores key order', () => {
      const reordered = {
        flow: source.flow,
        tags: source.tags,
        title: source.title,
      } as GqlSourceInput;

      expect(formatSourceRequest(reordered)).toEqual(formatSourceRequest(source));
    });

    it('drops null, undefined and __typename fields', () => {
      const noisy = {
        ...source,
        latLng: null,
        draft: undefined,
        __typename: 'Source',
      } as GqlSourceInput;

      expect(formatSourceRequest(noisy)).toEqual(formatSourceRequest(source));
    });

    it('indents with two spaces', () => {
      expect(formatSourceRequest({ title: 'a' } as GqlSourceInput)).toEqual('{\n  "title": "a"\n}');
    });
  });

  describe('parseSourceRequest', () => {
    it('round-trips a formatted source', () => {
      expect(parseSourceRequest(formatSourceRequest(source))).toEqual(source);
    });

    it('rejects invalid json', () => {
      expect(() => parseSourceRequest('{ "title": ')).toThrow(/Invalid JSON/);
    });

    it('rejects json without a flow sequence', () => {
      expect(() => parseSourceRequest('{ "title": "a" }')).toThrow(/flow\.sequence/);
    });
  });
});
