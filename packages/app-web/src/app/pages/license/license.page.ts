import { Component, OnInit } from '@angular/core';
import { ServerSettingsService } from '../../services/server-settings.service';

@Component({
  selector: 'app-license-page',
  templateUrl: './license.page.html',
  styleUrls: ['./license.page.scss'],
})
export class LicensePage implements OnInit {
  constructor(
    readonly serverSettings: ServerSettingsService,
  ) {
  }

  ngOnInit() {

  }

}
