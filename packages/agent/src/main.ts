import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { PrefixLoggerService } from './services/common/prefix-logger.service';

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    logger: new PrefixLoggerService(
      process.env.NODE_ENV === 'prod'
        ? ['error', 'warn']
        : ['log', 'error', 'verbose', 'warn'],
      'agent',
    ),
  });
  await app.listen(3000);
}
bootstrap();
