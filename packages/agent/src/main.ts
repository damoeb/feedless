import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import * as winston from 'winston';
import { WinstonModule } from 'nest-winston';

console.log(`NODE_ENV=${process.env.NODE_ENV}`);

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger: WinstonModule.createLogger({
      defaultMeta: {
        '@version': process.env.APP_VERSION || '1',
        commit: process.env.APP_GIT_HAS || '',
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
    }),
  });
  await app.listen(3000);
}

bootstrap().catch(console.error);
