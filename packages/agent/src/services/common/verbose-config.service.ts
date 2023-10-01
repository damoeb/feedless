import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { isUndefined } from 'lodash';

export type EnvValueOptions<T> = { fallback?: T; mask?: number };

@Injectable()
export class VerboseConfigService extends ConfigService {
  private readonly log = new Logger(VerboseConfigService.name);

  getBoolean(propertyPath: string): boolean {
    const result = super.get(propertyPath)?.toLowerCase()?.trim() === 'true';
    this.logEnv(propertyPath, result);
    return result;
  }

  getInt(propertyPath: string, { mask, fallback }: EnvValueOptions<number>) {
    const value = super.get(propertyPath);
    const useFallback = isUndefined(value);
    const result = useFallback ? fallback : parseInt(value);
    this.logEnv(propertyPath, result, useFallback, mask);
    return result;
  }

  getString(
    propertyPath: string,
    { mask, fallback }: EnvValueOptions<string>,
  ): string {
    const value = super.get(propertyPath);
    const useFallback = isUndefined(value);
    const result = useFallback ? fallback : value;
    this.logEnv(propertyPath, result, useFallback, mask);
    return result;
  }

  private logEnv(
    propertyPath: string,
    value: any,
    isFallback: boolean = false,
    masked?: number,
  ) {
    const isMasked = !isUndefined(masked);
    const applyNotes = (value: string) => {
      const notes: string[] = [];
      if (isFallback) {
        notes.push('default');
      }
      if (isMasked) {
        notes.push('masked');
      }

      return `${value} ${notes.length === 0 ? '' : `[${notes.join(', ')}]`}`;
    };

    const applyMask = (value: string) => {
      if (isMasked) {
        return `${value?.substring(0, masked)}*****`;
      } else {
        return `${value}`;
      }
    };

    this.log.log(`env.${propertyPath}=${applyNotes(applyMask(value))}`);
  }
}
