import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { GqlSourceInput } from '../../../generated/graphql';
import { ModalController } from '@ionic/angular';
import { ServerConfigService } from '../../services/server-config.service';
import { ScrapeService } from '../../services/scrape.service';
import { InteractiveWebsiteController } from './interactive-website-controller';

export type InteractiveWebsiteModalComponentProps = {
  source: GqlSourceInput;
};

@Component({
  selector: 'app-interactive-website-modal',
  templateUrl: './interactive-website-modal.component.html',
  styleUrls: ['./interactive-website-modal.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
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
