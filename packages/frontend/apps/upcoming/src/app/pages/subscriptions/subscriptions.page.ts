import { ChangeDetectionStrategy, Component } from '@angular/core';
import { IonContent, IonLabel, IonList, IonItem, IonNote } from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-subscriptions-page',
  templateUrl: './subscriptions.page.html',
  styleUrls: ['./subscriptions.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonList, IonItem, IonLabel, IonNote, RouterLink],
  standalone: true,
})
export class SubscriptionsPage {
  protected subscriptions: { id: string; label: string; frequency?: string }[] = [];
}
