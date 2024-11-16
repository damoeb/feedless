import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { ModalController, ToastController } from '@ionic/angular/standalone';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
} from '../../modals/import-opml-modal/import-opml-modal.component';
import { firstValueFrom } from 'rxjs';
import { FileService } from '../../services/file.service';
import { RepositoryService } from '../../services/repository.service';
import { isArray } from 'lodash-es';
import { GqlRepositoryCreateInput } from '../../../generated/graphql';
import { ModalService } from '../../services/modal.service';

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

  constructor(
    private readonly omplService: OpmlService,
    private readonly authService: AuthService,
    private readonly fileService: FileService,
    private readonly modalService: ModalService,
    private readonly repositoryService: RepositoryService,
    private readonly router: Router,
    private readonly toastCtrl: ToastController,
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

  async openFilePicker(filePicker: HTMLInputElement) {
    if (await firstValueFrom(this.authService.isAuthenticated())) {
      filePicker.click();
    } else {
      await this.router.navigateByUrl('/login');
    }
  }

  async importFeedlessJson(uploadEvent: Event) {
    const data = await this.fileService.uploadAsText(uploadEvent);
    const repositories = JSON.parse(data) as GqlRepositoryCreateInput[];

    const selected = await this.modalService.openSelectionModal({
      title: 'Import Repositories',
      description: '',
      selectables: repositories.map((r) => ({
        entity: r,
        label: r.title,
      })),
    });

    if (selected.length > 0) {
      const repo = await this.repositoryService.createRepositories(selected);
      const toast = await this.toastCtrl.create({
        message: `Imported ${selected.length} repositories`,
        duration: 3000,
        color: 'success',
      });

      await toast.present();
      await this.router.navigateByUrl(`/feeds/${repo[0].id}`);
    }
  }
}
