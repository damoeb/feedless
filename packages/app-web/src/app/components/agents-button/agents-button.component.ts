import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AgentService } from '../../services/agent.service';
import { Subscription } from 'rxjs';
import { ServerSettingsService } from '../../services/server-settings.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-agents-button',
  templateUrl: './agents-button.component.html',
  styleUrls: ['./agents-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AgentsButtonComponent implements OnInit, OnDestroy {
  agentCount: number = 0;
  isLoggedIn: boolean;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly agentService: AgentService,
    private readonly authService: AuthService,
    protected readonly serverSettings: ServerSettingsService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.authService.authorizationChange().subscribe((authorization) => {
        this.isLoggedIn = authorization?.loggedIn;
        this.changeRef.detectChanges();
      }),
      this.agentService.getAgents().subscribe((agents) => {
        this.agentCount = agents.length;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
