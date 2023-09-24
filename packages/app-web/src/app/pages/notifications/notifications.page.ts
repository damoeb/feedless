import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ProfileService } from 'src/app/services/profile.service';
import { ServerSettingsService } from '../../services/server-settings.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationsPage implements OnInit, OnDestroy {
  streamId: string;
  feedUrl: string;
  private subscriptions: Subscription[] = [];

  constructor(
    private readonly profileService: ProfileService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly serverSettingsService: ServerSettingsService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.profileService.getProfile().subscribe((profile) => {
        this.streamId = profile.user.notificationsStreamId;
        this.feedUrl = `${this.serverSettingsService.apiUrl}/stream:${this.streamId}`;
        this.changeRef.detectChanges();
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
