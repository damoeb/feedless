import { NestFactory } from '@nestjs/core';
import { WebSocket } from 'ws';
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
// bootstrap();

const client = new WebSocket('ws://localhost:8080/ws');

client.on('error', console.error);
client.on('open', console.log);
client.on('ping', console.log);
// client.on('close', function clear() {
//   clearTimeout(this.pingTimeout);
// });
