import { Component, OnInit } from '@angular/core';
import { ProfileService } from 'src/app/services/profile.service';

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.page.html',
  styleUrls: ['./notifications.page.scss'],
})
export class NotificationsPage implements OnInit {
  streamId: string;
  constructor(private readonly profileService: ProfileService) {}

  ngOnInit() {
    this.streamId = this.profileService.getNotificationsStreamId();
  }
}
