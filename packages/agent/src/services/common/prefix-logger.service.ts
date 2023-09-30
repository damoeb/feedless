import { ConsoleLogger, Injectable, LogLevel } from '@nestjs/common';

@Injectable()
export class PrefixLoggerService extends ConsoleLogger {
  constructor(
    levels: LogLevel[],
    private prefix: string,
  ) {
    super();
    super.setLogLevels(levels);
  }

  error(message: any, stack?: string, context?: string) {
    super.error(`[${this.prefix}] ${message}`, stack, context);
  }

  log(message: any, ...optionalParams) {
    super.log(`[${this.prefix}] ${message}`, ...optionalParams);
  }
}
