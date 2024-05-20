import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ServerSettingsService } from '../../services/server-settings.service';
import { AuthService } from '../../services/auth.service';
import { RepositoryService } from '../../services/repository.service';

@Component({
  selector: 'app-repositories-button',
  templateUrl: './repositories-button.component.html',
  styleUrls: ['./repositories-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoriesButtonComponent implements OnInit, OnDestroy {
  @Input({required: true})
  name: string

  @Input({required: true})
  link: string

  feedCount: number = 0;
  isLoggedIn: boolean;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly repositoryService: RepositoryService,
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
      this.repositoryService.countRepositories().subscribe((repoCount) => {
        this.feedCount = repoCount;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
