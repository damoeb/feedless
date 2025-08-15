import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as winston from 'winston';
import { WinstonModule } from 'nest-winston';

console.log(`NODE_ENV=${process.env.NODE_ENV}`);

function createJsonLogger() {
  return WinstonModule.createLogger({
    defaultMeta: {
      '@version': process.env.APP_VERSION || '1',
      commit: process.env.APP_GIT_HASH || '',
      app: 'agent',
    },
    transports: [
      new winston.transports.Console({
        format: winston.format.combine(
          winston.format.timestamp(),
          winston.format.json({}),
        ),
      }),
    ],
  });
}

function createSimpleLogger() {
  return WinstonModule.createLogger({
    transports: [
      new winston.transports.Console({
        format: winston.format.simple(),
      }),
    ],
  });
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger:
      process.env.NODE_ENV == 'prod'
        ? createJsonLogger()
        : createSimpleLogger(),
  });
  await app.listen(3000);
}

bootstrap().catch(console.error);
