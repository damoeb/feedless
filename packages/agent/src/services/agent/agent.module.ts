import { Module } from '@nestjs/common';
import { AgentService } from './agent.service';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';
import { CommonModule } from '../common/common.module';

@Module({
  providers: [AgentService],
  exports: [AgentService],
  imports: [PuppeteerModule, CommonModule],
})
export class AgentModule {}
