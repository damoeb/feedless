import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { VerboseConfigService } from '../common/verbose-config.service';
import { AgentEvent, ScrapeResponseInput } from '../../generated/graphql';

@Injectable()
export class StatsService implements OnModuleInit {
  private readonly log = new Logger(StatsService.name);
  private id = 1;

  constructor(
    private readonly config: VerboseConfigService,
  ) {}

  onModuleInit() {
  }

  recordAgentEvent(event: AgentEvent): number {
    return this.id++;
  }

  recordAgentEventSuccess(id: number, scrapeResponse: ScrapeResponseInput) {

  }

  recordAgentEventFailure(id: number, message: any) {

  }
}
