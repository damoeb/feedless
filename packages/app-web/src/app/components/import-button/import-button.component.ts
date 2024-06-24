import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  Input,
  ViewChild,
} from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ModalController } from '@ionic/angular';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
} from '../../modals/import-opml-modal/import-opml-modal.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-import-button',
  templateUrl: './import-button.component.html',
  styleUrls: ['./import-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ImportButtonComponent {
  @Input()
  color: string;

  @Input()
  expand: string;

  @Input()
  fill: string = 'clear';

  @ViewChild('opmlPicker')
  opmlPickerElement!: ElementRef<HTMLInputElement>;

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
