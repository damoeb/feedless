import { Module } from '@nestjs/common';
import { AgentService } from './agent.service';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';

@Module({
  providers: [AgentService],
  exports: [AgentService],
  imports: [PuppeteerModule],
})
export class AgentModule {}
