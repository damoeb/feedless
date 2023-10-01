import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { PrefixLoggerService } from './services/common/prefix-logger.service';
import { LogLevel } from '@nestjs/common';

console.log(`NODE_ENV=${process.env.NODE_ENV}`);
const loggerPrefix = 'agent';
const fallbackLogLevel =
  process.env.NODE_ENV === 'prod'
    ? ['error', 'warn']
    : ['log', 'error', 'verbose', 'warn'];

const logLevels: LogLevel[] =
  process.env.LOG_LEVELS?.split(',')
    .filter((level) => !!level)
    .map((level) => level.toLowerCase())
    .filter((level) =>
      ['log', 'error', 'warn', 'debug', 'verbose', 'fatal'].includes(level),
    )
    .map((level) => level as any) || fallbackLogLevel;

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger: new PrefixLoggerService(logLevels, loggerPrefix),
  });
  await app.listen(3000);
}
bootstrap().catch(console.error);
