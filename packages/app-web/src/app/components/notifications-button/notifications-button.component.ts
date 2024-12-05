import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { SessionService } from '../../services/session.service';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { notificationsOutline } from 'ionicons/icons';
import { IonButton, IonIcon } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-notifications-button',
  templateUrl: './notifications-button.component.html',
  styleUrls: ['./notifications-button.component.scss'],
  imports: [IonButton, RouterLink, IonIcon],
  standalone: true,
})
export class NotificationsButtonComponent implements OnInit, OnDestroy {
  readonly sessionService = inject(SessionService);

  notificationRepositoryId: string;
  private subscriptions: Subscription[] = [];

  constructor() {
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
