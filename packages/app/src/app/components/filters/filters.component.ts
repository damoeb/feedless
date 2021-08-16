import { Component, Input, OnInit } from '@angular/core';
import { ModalController } from '@ionic/angular';
import { BucketService } from '../../services/bucket.service';
import { GqlBucket } from '../../../generated/graphql';

@Component({
  selector: 'app-filters',
  templateUrl: './filters.component.html',
  styleUrls: ['./filters.component.scss'],
})
export class FiltersComponent implements OnInit {
  @Input()
  bucket: GqlBucket;

  changed: boolean = false;
  expression = 'linkCount > 0';
  constructor(
    private readonly modalController: ModalController,
    private readonly bucketService: BucketService
  ) {}

  ngOnInit() {}

  dismissModal() {
    return this.modalController.dismiss(this.changed);
  }

  save() {
    this.bucketService.updateFilterExpression(this.bucket.id, this.expression);
  }
}
