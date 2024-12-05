import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import { OpmlService } from '../../services/opml.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import {
  IonButton,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonPopover,
  ModalController,
  ToastController,
} from '@ionic/angular/standalone';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
} from '../../modals/import-opml-modal/import-opml-modal.component';
import { firstValueFrom } from 'rxjs';
import { FileService } from '../../services/file.service';
import { RepositoryService } from '../../services/repository.service';
import { GqlRepositoryCreateInput } from '../../../generated/graphql';
import { ModalService } from '../../services/modal.service';
import { RemoveIfProdDirective } from '../../directives/remove-if-prod/remove-if-prod.directive';
import { SelectionModalModule } from '../../modals/selection-modal/selection-modal.module';
import { ImportOpmlModalModule } from '../../modals/import-opml-modal/import-opml-modal.module';

@Component({
  selector: 'app-import-button',
  templateUrl: './import-button.component.html',
  styleUrls: ['./import-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonButton,
    IonLabel,
    IonPopover,
    IonContent,
    IonList,
    IonItem,
    RemoveIfProdDirective,
    SelectionModalModule,
    ImportOpmlModalModule,
  ],
  standalone: true,
})
export class ImportButtonComponent {
  private readonly omplService = inject(OpmlService);
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);
  private readonly modalService = inject(ModalService);
  private readonly repositoryService = inject(RepositoryService);
  private readonly router = inject(Router);
  private readonly toastCtrl = inject(ToastController);
  private readonly modalCtrl = inject(ModalController);

  readonly color = input<string>();

  readonly expand = input<string>();

  readonly fill = input<string>('clear');

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
