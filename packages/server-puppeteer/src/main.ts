import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger:
      process.env.NODE_ENV === 'prod'
        ? ['error', 'warn', 'log']
        : ['log', 'debug', 'error', 'verbose', 'warn'],
  });
  await app.listen(3000);
}
bootstrap();
