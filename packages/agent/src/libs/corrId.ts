import { v4 } from 'uuid';

export function newCorrId(): string {
  return v4().substring(0, 5);
}
