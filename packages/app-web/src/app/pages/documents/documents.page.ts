import { Component } from '@angular/core';
import { AppConfigService } from '../../services/app-config.service';

@Component({
    selector: 'app-documents-page',
    templateUrl: './documents.page.html',
    styleUrls: ['./documents.page.scss'],
    standalone: false
})
export class DocumentsPage {
  constructor(appConfig: AppConfigService) {
    appConfig.setPageTitle('Documents');
  }
}
