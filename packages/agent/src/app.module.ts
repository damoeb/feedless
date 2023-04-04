import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';
import { AppController } from './app.controller';
import { AgentModule } from './services/agent/agent.module';

@Module({
  imports: [PuppeteerModule, AgentModule],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
