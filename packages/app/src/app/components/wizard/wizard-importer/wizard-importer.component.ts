import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { ModalDismissal } from '../../../app.module';
import { ItemsFilterModalComponent, ItemsFilterModalComponentProps } from '../../../modals/items-filter-modal/items-filter-modal.component';
import { WizardHandler } from '../wizard-handler';

@Component({
  selector: 'app-wizard-importer',
  templateUrl: './wizard-importer.component.html',
  styleUrls: ['./wizard-importer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardImporterComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  feedUrl: string;

  constructor(private readonly modalCtrl: ModalController) {}

  ngOnInit() {
    this.feedUrl = this.handler.getContext().feedUrl;
  }

  async showFilterModal() {
    const componentProps: ItemsFilterModalComponentProps = {
      filterExpression: '',
    };
    const modal = await this.modalCtrl.create({
      component: ItemsFilterModalComponent,
      componentProps,
      backdropDismiss: false,
    });
    await modal.present();
    await modal.onDidDismiss<ModalDismissal>();
  }
}
