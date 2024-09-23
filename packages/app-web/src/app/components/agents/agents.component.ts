import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Agent, AgentService } from '../../services/agent.service';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { compact } from 'lodash-es';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AgentsComponent implements OnInit, OnDestroy {
  agents: Agent[] = [];
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly agentService: AgentService,
    protected readonly serverConfig: ServerConfigService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.agentService.getAgents().subscribe((agents) => {
        this.agents = agents;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  fromNow = relativeTimeOrElse;
}

export function relativeTimeOrElse(
  futureTimestamp: number,
  suffix: string = null,
): string {
  dayjs.extend(relativeTime);
  const now = dayjs();
  const ts = dayjs(futureTimestamp);
  if (now.subtract(2, 'weeks').isAfter(ts)) {
    return ts.format('DD.MMMM YYYY');
  } else {
    return compact([ts.toNow(true), suffix]).join(' ');
  }
}
