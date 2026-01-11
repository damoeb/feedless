import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
} from '@angular/core';
import { isPlatformBrowser, JsonPipe, NgClass } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { GqlSourceInput } from '@feedless/graphql-api';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonFooter,
  IonHeader,
  IonItem,
  IonLabel,
  IonList,
  IonRow,
  IonSelect,
  IonSelectOption,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { ScrapeService, ServerConfigService } from '@feedless/services';
import { InteractiveWebsiteController } from './interactive-website-controller';
import { InteractiveWebsiteComponent } from '../../components/interactive-website/interactive-website.component';
import { InputComponent } from '@feedless/form-elements';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { IconComponent } from '../../components/icon/icon.component';

export type InteractiveWebsiteModalComponentProps = {
  source: GqlSourceInput;
};

@Component({
  selector: 'app-interactive-website-modal',
  templateUrl: './interactive-website-modal.component.html',
  styleUrls: ['./interactive-website-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IconComponent,
    IonContent,
    IonRow,
    InteractiveWebsiteComponent,
    IonList,
    NgClass,
    IonItem,
    IonSelect,
    FormsModule,
    ReactiveFormsModule,
    IonSelectOption,
    IonLabel,
    IonFooter,
    JsonPipe,
    InputComponent,
  ],
})
export class InteractiveWebsiteModalComponent
  extends InteractiveWebsiteController
  implements OnInit, OnDestroy, InteractiveWebsiteModalComponentProps
{
  readonly changeRef = inject(ChangeDetectorRef);
  private readonly modalCtrl = inject(ModalController);
  readonly scrapeService = inject(ScrapeService);
  readonly serverConfig = inject(ServerConfigService);

  declare source: GqlSourceInput;

  hideNonUiActions = true;
  private readonly platformId = inject(PLATFORM_ID);

  constructor() {
    super();
    if (isPlatformBrowser(this.platformId)) {
      addIcons({ closeOutline, trashOutline });
    }
  }

  ngOnInit(): void {
    this.initializeController();
  }

  ngOnDestroy(): void {
    this.destroyController();
  }

  dismissModal() {
    return this.modalCtrl.dismiss();
  }

  applyChanges() {
    return this.modalCtrl.dismiss(this.sourceBuilder);
  }

  handleActionReorder(event: any) {
    console.log(event);
  }
}
