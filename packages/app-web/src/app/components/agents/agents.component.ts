import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Agent, AgentService } from '../../services/agent.service';
import { dateTimeFormat } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { ServerSettingsService } from '../../services/server-settings.service';

@Component({
  selector: 'app-agents',
  templateUrl: './agents.component.html',
  styleUrls: ['./agents.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AgentsComponent implements OnInit, OnDestroy {
  agents: Agent[] = [];
  private subscriptions: Subscription[] = [];
  protected readonly dateTimeFormat = dateTimeFormat;

  constructor(
    private readonly agentService: AgentService,
    protected readonly serverSettings: ServerSettingsService,
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
}
