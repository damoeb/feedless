import { Module } from '@nestjs/common';
import { AgentService } from './agent.service';
import { PuppeteerModule } from '../puppeteer/puppeteer.module';
import { CommonModule } from '../common/common.module';
import { StatsModule } from '../stats/stats.module';

@Module({
  providers: [AgentService],
  exports: [AgentService],
  imports: [PuppeteerModule, CommonModule, StatsModule],
})
export class AgentModule {}
