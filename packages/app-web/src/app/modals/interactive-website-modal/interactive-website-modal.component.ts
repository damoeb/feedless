import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { GqlSourceInput } from '../../../generated/graphql';
import { ModalController } from '@ionic/angular/standalone';
import { ServerConfigService } from '../../services/server-config.service';
import { ScrapeService } from '../../services/scrape.service';
import { InteractiveWebsiteController } from './interactive-website-controller';
import { addIcons } from 'ionicons';
import { closeOutline, trashOutline } from 'ionicons/icons';

export type InteractiveWebsiteModalComponentProps = {
  source: GqlSourceInput;
};

@Component({
  selector: 'app-interactive-website-modal',
  templateUrl: './interactive-website-modal.component.html',
  styleUrls: ['./interactive-website-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false,
})
export class InteractiveWebsiteModalComponent
  extends InteractiveWebsiteController
  implements OnInit, OnDestroy, InteractiveWebsiteModalComponentProps
{
  readonly changeRef = inject(ChangeDetectorRef);
  private readonly modalCtrl = inject(ModalController);
  readonly scrapeService = inject(ScrapeService);
  readonly serverConfig = inject(ServerConfigService);

  @Input({ required: true })
  source: GqlSourceInput;

  hideNonUiActions: boolean = true;

  constructor() {
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
