import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { AgentService } from '../../../services/agent.service';
import { RepositoryService } from '../../../services/repository.service';
import { AuthService } from '../../../services/auth.service';
import { GqlVertical } from '../../../../generated/graphql';
import {
  IonList,
  IonMenuToggle,
  IonItem,
  IonChip,
} from '@ionic/angular/standalone';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { NgIf } from '@angular/common';
import { BubbleComponent } from '../../../components/bubble/bubble.component';

@Component({
  selector: 'app-feedless-menu',
  templateUrl: './feedless-menu.component.html',
  styleUrls: ['./feedless-menu.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonList,
    IonMenuToggle,
    IonItem,
    RouterLink,
    RouterLinkActive,
    NgIf,
    IonChip,
    BubbleComponent,
  ],
  standalone: true,
})
export class FeedlessMenuComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  agentCount: number;
  feedCount: number;
  trackerCount: number;
  loggedIn: boolean;
  libraryCount: number = 0;

  constructor(
    private readonly agentService: AgentService,
    private readonly authService: AuthService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly repositoryService: RepositoryService,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.authService.authorizationChange().subscribe((authentication) => {
        this.loggedIn = authentication?.loggedIn === true;
        this.changeRef.detectChanges();
      }),
      this.agentService.getAgents().subscribe((agents) => {
        this.agentCount = agents.length;
        this.changeRef.detectChanges();
      }),
      this.repositoryService
        .countRepositories({ product: GqlVertical.RssProxy })
        .subscribe((repoCount) => {
          this.feedCount = repoCount;
          this.changeRef.detectChanges();
        }),
      this.repositoryService
        .countRepositories({ product: GqlVertical.VisualDiff })
        .subscribe((repoCount) => {
          this.trackerCount = repoCount;
          this.changeRef.detectChanges();
        }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
