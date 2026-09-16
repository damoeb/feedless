import { isPlainObject } from 'lodash-es';
import { GqlSourceInput } from '../../../generated/graphql';

// Stable output so only real changes show up in a diff
export function formatSourceRequest(source: GqlSourceInput): string {
  return JSON.stringify(normalize(source), null, 2);
}

export function parseSourceRequest(text: string): GqlSourceInput {
  let parsed: any;
  try {
    parsed = JSON.parse(text);
  } catch (e) {
    throw new Error(`Invalid JSON: ${(e as Error).message}`);
  }
  if (!isPlainObject(parsed) || !Array.isArray(parsed.flow?.sequence)) {
    throw new Error('Expected an object with flow.sequence');
  }
  return parsed as GqlSourceInput;
}

function normalize(value: unknown): unknown {
  if (Array.isArray(value)) {
    return value.map(normalize);
  }
  if (isPlainObject(value)) {
    const object = value as { [key: string]: unknown };
    return Object.keys(object)
      .filter((key) => key !== '__typename' && object[key] !== null && object[key] !== undefined)
      .sort()
      .reduce(
        (normalized, key) => ({ ...normalized, [key]: normalize(object[key]) }),
        {} as { [key: string]: unknown }
      );
  }
  return value;
}
