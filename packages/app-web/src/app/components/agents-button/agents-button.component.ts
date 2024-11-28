import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
  inject,
} from '@angular/core';
import { AgentService } from '../../services/agent.service';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AuthService } from '../../services/auth.service';
import {
  IonRouterLink,
  IonButton,
  IonLabel,
  IonChip,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

import { BubbleComponent } from '../bubble/bubble.component';

@Component({
  selector: 'app-agents-button',
  templateUrl: './agents-button.component.html',
  styleUrls: ['./agents-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonButton, RouterLink, IonLabel, BubbleComponent, IonChip],
  standalone: true,
})
export class AgentsButtonComponent implements OnInit, OnDestroy {
  private readonly agentService = inject(AgentService);
  private readonly authService = inject(AuthService);
  protected readonly serverConfig = inject(ServerConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  agentCount: number = 0;
  isLoggedIn: boolean;
  private subscriptions: Subscription[] = [];

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
