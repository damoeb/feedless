import { Component, inject } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';
import { FeedlessHeaderComponent } from '../../components/feedless-header/feedless-header.component';
import { IonContent, IonRouterOutlet } from '@ionic/angular/standalone';

@Component({
  selector: 'app-documents-page',
  templateUrl: './documents.page.html',
  styleUrls: ['./documents.page.scss'],
  imports: [FeedlessHeaderComponent, IonContent, IonRouterOutlet],
  standalone: true,
})
export class DocumentsPage {
  constructor() {
    const appConfig = inject(AppConfigService);

    appConfig.setPageTitle('Documents');
  }
}
