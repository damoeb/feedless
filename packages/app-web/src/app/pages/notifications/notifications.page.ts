import { Component, OnInit } from '@angular/core';
import { ProfileService } from 'src/app/services/profile.service';
import { ServerSettingsService } from '../../services/server-settings.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
})
export class NotificationsPage implements OnInit {
  streamId: string;
  feedUrl: string;
  constructor(
    private readonly profileService: ProfileService,
    private readonly serverSettingsService: ServerSettingsService
  ) {}

  ngOnInit() {
    this.streamId = this.profileService.getNotificationsStreamId();
    this.feedUrl = `${this.serverSettingsService.apiUrl}/stream:${this.streamId}`;
  }
}
