import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { AgentService } from '../../../services/agent.service';
import { RepositoryService } from '../../../services/repository.service';
import { AuthService } from '../../../services/auth.service';
import { GqlVertical } from '../../../../generated/graphql';
import {
  IonAccordion,
  IonAccordionGroup,
  IonItem,
  IonLabel,
  IonList,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { RemoveIfProdDirective } from '../../../directives/remove-if-prod/remove-if-prod.directive';

@Component({
  selector: 'app-feedless-menu',
  templateUrl: './feedless-menu.component.html',
  styleUrls: ['./feedless-menu.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonList,
    IonItem,
    RouterLink,
    IonAccordionGroup,
    IonAccordion,
    IonLabel,
    RemoveIfProdDirective,
  ],
  standalone: true,
})
export class FeedlessMenuComponent implements OnInit, OnDestroy {
  private readonly agentService = inject(AgentService);
  private readonly authService = inject(AuthService);
  private readonly changeRef = inject(ChangeDetectorRef);
  private readonly repositoryService = inject(RepositoryService);

  private subscriptions: Subscription[] = [];
  agentCount: number;
  feedCount: number;
  trackerCount: number;
  loggedIn: boolean;
  libraryCount: number = 0;

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
        })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
