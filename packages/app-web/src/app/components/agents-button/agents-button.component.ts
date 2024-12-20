import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AgentService } from '../../services/agent.service';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AuthService } from '../../services/auth.service';
import { IonRouterLink } from '@ionic/angular/standalone';

@Component({
  selector: 'app-agents-button',
  templateUrl: './agents-button.component.html',
  styleUrls: ['./agents-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class AgentsButtonComponent implements OnInit, OnDestroy {
  agentCount: number = 0;
  isLoggedIn: boolean;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly agentService: AgentService,
    private readonly authService: AuthService,
    protected readonly serverConfig: ServerConfigService,
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
