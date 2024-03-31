import { ChangeDetectionStrategy, Component, ElementRef, ViewChild } from '@angular/core';
import { fixUrl } from '../../../app.module';
import { Router } from '@angular/router';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { dateFormat } from '../../../services/profile.service';
import { OpmlService } from '../../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { ImportOpmlModalComponent, ImportOpmlModalComponentProps } from '../../../modals/import-opml-modal/import-opml-modal.component';
import { AuthService } from '../../../services/auth.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-about-rss-builder',
  templateUrl: './about-rss-builder.page.html',
  styleUrls: ['./about-rss-builder.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AboutRssBuilderPage {
  @ViewChild('opmlPicker')
  opmlPickerElement!: ElementRef<HTMLInputElement>;

  constructor(
    private readonly router: Router,
    private readonly omplService: OpmlService,
    private readonly authService: AuthService,
    private readonly modalCtrl: ModalController,
    readonly serverSettings: ServerSettingsService,
  ) {}

  async handleQuery(url: string) {
    try {
      await this.router.navigate(['/builder'], {
        queryParams: {
          url: fixUrl(url),
        },
      });
    } catch (e) {
      console.warn(e);
    }
  }

  getLicenseExpiry() {
    return new Date(
      this.serverSettings.getBuildFrom() + 1000 * 60 * 60 * 24 * 265 * 2,
    );
  }

  protected readonly dateFormat = dateFormat;

  async importOpml(uploadEvent: Event) {
    const outlines = await this.omplService.convertOpmlToJson(uploadEvent);

    const componentProps: ImportOpmlModalComponentProps = {
      outlines,
    };
    const modal = await this.modalCtrl.create({
      component: ImportOpmlModalComponent,
      cssClass: 'fullscreen-modal',
      componentProps,
    });

    await modal.present();
  }

  async openOpmlPicker() {
    if (await firstValueFrom(this.authService.isAuthenticated())) {
      this.opmlPickerElement.nativeElement.click();
    } else {
      await this.router.navigateByUrl('/login');
    }
  }
}
