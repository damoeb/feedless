import { inject, Injectable } from '@angular/core';
import { GqlRepositoryCreateInput, GqlSourceInput } from '../../generated/graphql';
import {
  SelectableEntity,
  SelectionModalComponent,
} from '../modals/selection-modal/selection-modal.component';
import { sortBy } from 'lodash-es';
import { HttpClient } from '@angular/common/http';
import { FileService } from './file.service';
import { RepositoryService } from './repository.service';
import { ModalService } from './modal.service';
import { ToastController } from '@ionic/angular/standalone';
import { Nullable } from '../types';

@Injectable({
  providedIn: 'root',
})
export class SourceService {
  private readonly fileService = inject(FileService);
  private readonly repositoryService = inject(RepositoryService);
  private readonly modalService = inject(ModalService);
  private readonly toastCtrl = inject(ToastController);

  constructor() {}

  async uploadFeedlessJson(uploadEvent: Event, repositoryId: Nullable<string>) {
    const data = await this.fileService.uploadAsText(uploadEvent);
    const repositories = JSON.parse(data) as GqlRepositoryCreateInput[];
    const selectables: SelectableEntity<GqlSourceInput>[] = sortBy(
      repositories
        .filter((r) => r.sources)
        .flatMap((r) => r.sources)
        .map<SelectableEntity<GqlSourceInput>>((source) => {
          return {
            entity: source,
            label: source.title,
          };
        }),
      (selectable) => selectable.entity.title
    );

    const selected = await this.modalService.openSelectionModal<GqlSourceInput>(
      SelectionModalComponent<GqlSourceInput>,
      {
        selectables,
        title: 'Import Sources',
        description: 'Select those sources you want to import',
      }
    );

    if (selected.length > 0) {
      console.log(`Adding ${selected.length} sources to repository ${repositoryId}`);
      await this.repositoryService.updateRepository({
        where: {
          id: repositoryId,
        },
        data: {
          sources: {
            add: selected,
          },
        },
      });

      const toast = await this.toastCtrl.create({
        message: `Added sources`,
        duration: 3000,
        color: 'success',
      });

      await toast.present();
    } else {
      console.log('No sources to add');
    }
  }
}
