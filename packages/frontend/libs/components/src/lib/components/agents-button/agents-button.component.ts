import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { AgentService, AuthService, ServerConfigService } from '../../services';
import { Subscription } from 'rxjs';
import { IonButton, IonChip, IonLabel } from '@ionic/angular/standalone';
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

  agentCount = 0;
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
