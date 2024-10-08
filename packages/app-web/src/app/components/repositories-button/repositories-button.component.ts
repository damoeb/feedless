import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { ServerConfigService } from '../../services/server-config.service';
import { AuthService } from '../../services/auth.service';
import { RepositoryService } from '../../services/repository.service';
import { GqlProductCategory } from '../../../generated/graphql';

@Component({
  selector: 'app-repositories-button',
  templateUrl: './repositories-button.component.html',
  styleUrls: ['./repositories-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoriesButtonComponent implements OnInit, OnDestroy {
  @Input({ required: true })
  name: string;

  @Input({ required: true })
  link: string;

  @Input({ required: true })
  product: GqlProductCategory;

  // feedCount: number = 0;
  isLoggedIn: boolean;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
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
      // this.repositoryService
      //   .countRepositories({ product: this.product })
      //   .subscribe((repoCount) => {
      //     this.feedCount = repoCount;
      //     this.changeRef.detectChanges();
      //   }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
