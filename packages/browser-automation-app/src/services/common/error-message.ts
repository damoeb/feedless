// Workers reject with strings as well as errors, and a blank reason reaches the harvest log as ''.
export function toErrorMessage(e: unknown): string {
  const message =
    e instanceof Error ? e.message || e.name : typeof e === 'string' ? e : '';
  return message?.trim() ? message : 'unknown error';
}
