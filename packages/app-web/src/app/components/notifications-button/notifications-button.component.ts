import { Component, OnDestroy, OnInit } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { notificationsOutline } from 'ionicons/icons';

@Component({
  selector: 'app-notifications-button',
  templateUrl: './notifications-button.component.html',
  styleUrls: ['./notifications-button.component.scss'],
  standalone: false,
})
export class NotificationsButtonComponent implements OnInit, OnDestroy {
  notificationRepositoryId: string;
  private subscriptions: Subscription[] = [];

  constructor(readonly sessionService: SessionService) {
    addIcons({ notificationsOutline });
  }

  async ngOnInit(): Promise<void> {
    this.subscriptions.push(
      this.sessionService.getSession().subscribe((session) => {
        this.notificationRepositoryId = session.user?.notificationRepositoryId;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
