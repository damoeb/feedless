import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';
import { AppController } from './app.controller';

@Module({
  imports: [
    PuppeteerModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
