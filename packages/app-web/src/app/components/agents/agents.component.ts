import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Agent, AgentService } from '../../services/agent.service';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import { compact } from 'lodash-es';
import { IonChip, IonItem, IonLabel, IonList } from '@ionic/angular/standalone';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonList, IonItem, IonLabel, IonChip],
  standalone: true,
})
export class AgentsComponent implements OnInit, OnDestroy {
  private readonly agentService = inject(AgentService);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  agents: Agent[] = [];
  private subscriptions: Subscription[] = [];

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
    return ts.format('DD.MM YYYY');
  } else {
    return compact([ts.toNow(true), suffix]).join(' ');
  }
}
