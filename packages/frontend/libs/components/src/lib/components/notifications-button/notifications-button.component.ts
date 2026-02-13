import {
  Component,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { SessionService } from '../../services';
import { Subscription } from 'rxjs';
import { addIcons } from 'ionicons';
import { notificationsOutline } from 'ionicons/icons';
import { IonButton } from '@ionic/angular/standalone';
import { isPlatformBrowser } from '@angular/common';
import { IconComponent } from '../icon/icon.component';

@Component({
  selector: 'app-notifications-button',
  templateUrl: './notifications-button.component.html',
  styleUrls: ['./notifications-button.component.scss'],
  imports: [IonButton, IconComponent],
  standalone: true,
})
export class NotificationsButtonComponent implements OnInit, OnDestroy {
  readonly sessionService = inject(SessionService);

  notificationRepositoryId: string;
  private subscriptions: Subscription[] = [];
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ notificationsOutline });
    }
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
