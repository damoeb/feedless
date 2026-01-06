import { inject, Injectable } from '@angular/core';
import {
  GqlRepositoryCreateInput,
  GqlSourceInput,
} from '@feedless/graphql-api';
import {
  ModalProvider,
  SelectableEntity,
  SelectionModalComponent,
} from '@feedless/components';
import { sortBy } from 'lodash-es';
import { FileService } from './file.service';
import { RepositoryService } from './repository.service';
import { ToastController } from '@ionic/angular/standalone';
import { Nullable } from '@feedless/shared-types';

@Injectable({
  providedIn: 'root',
})
export class SourceService {
  private readonly fileService = inject(FileService);
  private readonly repositoryService = inject(RepositoryService);
  private readonly modalProvider = inject(ModalProvider);
  private readonly toastCtrl = inject(ToastController);

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
      (selectable) => selectable.entity.title,
    );

    const selected =
      await this.modalProvider.openSelectionModal<GqlSourceInput>(
        SelectionModalComponent<GqlSourceInput>,
        {
          selectables,
          title: 'Import Sources',
          description: 'Select those sources you want to import',
        },
      );

    if (selected.length > 0) {
      console.log(
        `Adding ${selected.length} sources to repository ${repositoryId}`,
      );
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
