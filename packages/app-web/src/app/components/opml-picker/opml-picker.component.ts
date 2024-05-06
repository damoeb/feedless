import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
} from '../../modals/import-opml-modal/import-opml-modal.component';
import { firstValueFrom } from 'rxjs';
import { OpmlService } from '../../services/opml.service';
import { ModalController } from '@ionic/angular';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-opml-picker',
  templateUrl: './opml-picker.component.html',
  styleUrls: ['./opml-picker.component.scss'],
})
export class OpmlPickerComponent {
  @ViewChild('opmlPicker')
  opmlPickerElement!: ElementRef<HTMLInputElement>;

  @Input()
  color: string;

  @Input()
  fill: string;

  @Input()
  expand: string;

  constructor(
    private readonly omplService: OpmlService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly modalCtrl: ModalController,
  ) {}

  async importOpml(uploadEvent: Event) {
    const outlines = await this.omplService.convertOpmlToJson(uploadEvent);

    const componentProps: ImportOpmlModalComponentProps = {
      outlines,
    };
    const modal = await this.modalCtrl.create({
      component: ImportOpmlModalComponent,
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
