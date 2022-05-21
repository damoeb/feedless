import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';

@Module({
  imports: [PuppeteerModule],
  providers: [AppService],
})
export class AppModule {}
