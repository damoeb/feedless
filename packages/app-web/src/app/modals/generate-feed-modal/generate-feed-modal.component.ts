import { Component } from '@angular/core';
import { ModalController, ToastController } from '@ionic/angular';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { TypedFormGroup } from '../../components/scrape-source/scrape-source.component';

export interface GenerateFeedModalComponentProps {}

type FilterOperator = 'contains' | 'startsWith' | 'matches' | 'endsWith'
type FilterField = 'link' | 'title' | 'content'
type FilterType = 'include' | 'exclude'

interface FilterData {
  type: FilterType,
  field: FilterField,
  operator: FilterOperator,
  value: string
}

@Component({
  selector: 'app-generate-feed-modal',
  templateUrl: './generate-feed-modal.component.html',
  styleUrls: ['./generate-feed-modal.component.scss'],
})
export class GenerateFeedModalComponent
  implements GenerateFeedModalComponentProps
{
  atomFeedUrl: string = 'https://feedless.org/f/234234234';
  jsonFeedUrl: string = 'https://feedless.org/f/2342342346';

  fetchFrequencyFc = new FormControl<string>('0 0 0 * * *', {
    nonNullable: true,
    validators: Validators.pattern('([^ ]+ ){5}[^ ]+'),
  })
  filters: FormGroup<TypedFormGroup<FilterData>>[] = [];

  constructor(
    private readonly modalCtrl: ModalController,
    private readonly toastCtrl: ToastController,
  ) {}
  closeModal() {
    return this.modalCtrl.dismiss();
  }

  async copy(jsonFeedUrl: string) {
    const toast = await this.toastCtrl.create({
      message: 'Link copied',
      duration: 3000,
    });

    await toast.present();
  }

  addFilter() {
    if (this.filters.some(filter => filter.invalid)) {
      return
    }

    this.filters.push(new FormGroup({
      type: new FormControl<FilterType>('exclude', [Validators.required]),
      field: new FormControl<FilterField>('title', [Validators.required]),
      operator: new FormControl<FilterOperator>('startsWith', [Validators.required]),
      value: new FormControl<string>('', [Validators.required, Validators.minLength(3)]),
    }))
  }

  removeFilter(index: number) {
    this.filters.slice(index, index+1);
  }

  createFeed() {

  }
}
