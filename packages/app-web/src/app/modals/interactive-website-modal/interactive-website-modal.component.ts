import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { GqlSourceInput } from '../../../generated/graphql';
import {
  ModalController,
  IonHeader,
  IonToolbar,
  IonTitle,
  IonButtons,
  IonButton,
  IonIcon,
  IonContent,
  IonRow,
  IonList,
  IonItem,
  IonSelect,
  IonSelectOption,
  IonLabel,
  IonFooter,
} from '@ionic/angular/standalone';
import { ServerConfigService } from '../../services/server-config.service';
import { ScrapeService } from '../../services/scrape.service';
import { InteractiveWebsiteController } from './interactive-website-controller';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';
import { InteractiveWebsiteComponent } from '../../components/interactive-website/interactive-website.component';
import { NgClass, JsonPipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

export type InteractiveWebsiteModalComponentProps = {
  source: GqlSourceInput;
};

@Component({
  selector: 'app-interactive-website-modal',
  templateUrl: './interactive-website-modal.component.html',
  styleUrls: ['./interactive-website-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    IonToolbar,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
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
    JsonPipe
],
  standalone: true,
})
export class InteractiveWebsiteModalComponent
  extends InteractiveWebsiteController
  implements OnInit, OnDestroy, InteractiveWebsiteModalComponentProps
{
  @Input({ required: true })
  source: GqlSourceInput;

  hideNonUiActions: boolean = true;

  constructor(
    public readonly changeRef: ChangeDetectorRef,
    private readonly modalCtrl: ModalController,
    public readonly scrapeService: ScrapeService,
    public readonly serverConfig: ServerConfigService,
  ) {
    super();
    addIcons({ closeOutline, trashOutline });
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
}
