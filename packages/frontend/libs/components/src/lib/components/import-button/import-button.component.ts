import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
} from '@angular/core';
import {
  AuthService,
  FileService,
  OpmlService,
  RepositoryService,
} from '../../services';
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
import { firstValueFrom } from 'rxjs';
import { GqlRepositoryCreateInput } from '@feedless/graphql-api';
import {
  ImportOpmlModalComponent,
  ImportOpmlModalComponentProps,
  ModalProvider,
  SelectionModalComponent,
} from '../../modals';
import { RemoveIfProdDirective } from '../../directives';

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
  ],
  standalone: true,
})
export class ImportButtonComponent {
  private readonly omplService = inject(OpmlService);
  private readonly authService = inject(AuthService);
  private readonly fileService = inject(FileService);
  private readonly modalProvider = inject(ModalProvider);
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

  async createRepositoriesFromJson(uploadEvent: Event) {
    const data = await this.fileService.uploadAsText(uploadEvent);
    const repositories = JSON.parse(data) as GqlRepositoryCreateInput[];

    const selected = await this.modalProvider.openSelectionModal(
      SelectionModalComponent<GqlRepositoryCreateInput>,
      {
        title: 'Import Repositories',
        description: '',
        selectables: repositories.map((r) => ({
          entity: r,
          label: r.title,
        })),
      },
    );

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
