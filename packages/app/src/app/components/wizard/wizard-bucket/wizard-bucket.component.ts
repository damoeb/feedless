import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
} from '@angular/core';
import { WizardStepId } from '../wizard/wizard.component';
import { BasicBucket, BucketService } from '../../../services/bucket.service';
import { ProfileService } from '../../../services/profile.service';
import { Pagination } from '../../../services/pagination.service';
import { WizardHandler } from '../wizard-handler';
import {
  BucketData,
  BucketFormData,
} from '../../bucket-edit/bucket-edit.component';
import { GqlVisibility } from '../../../../generated/graphql';
import { visibilityToLabel } from '../../../pages/buckets/bucket/bucket.page';
import { isUndefined } from 'lodash-es';

@Component({
  selector: 'app-wizard-bucket',
  templateUrl: './wizard-bucket.component.html',
  styleUrls: ['./wizard-bucket.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class WizardBucketComponent implements OnInit {
  @Input()
  handler: WizardHandler;

  @Output()
  navigateTo: EventEmitter<WizardStepId> = new EventEmitter<WizardStepId>();
  existingBuckets: Array<BasicBucket> = [];
  readonly accordionValueCreate = 'create';
  private pagination: Pagination;
  private existingBucketId: string;
  private createBucketData: BucketFormData;

  constructor(
    private readonly bucketService: BucketService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly profileService: ProfileService
  ) {}

  async ngOnInit() {
    await this.handler.updateContext({
      isCurrentStepValid: false,
    });
    await this.searchBuckets();
  }

  async searchBuckets() {
    const { buckets, pagination } = await this.bucketService.search({
      where: {
        ownerId: this.profileService.getUserId(),
      },
      page: 0,
    });
    this.existingBuckets = buckets;
    this.pagination = pagination;
    this.changeRef.detectChanges();
  }

  async useExistingBucket(bucket: BasicBucket) {
    this.existingBucketId = bucket.id;
    await this.bubbleExistingBucket();
  }

  async handleCreateBucket(data: BucketFormData) {
    this.createBucketData = data;
    console.log('updateContext', data);
    await this.handler.updateContext({
      bucket: {
        create: this.createBucketData.data,
      },
      isCurrentStepValid: this.createBucketData.valid,
    });
  }

  hasBuckets(): boolean {
    return this.existingBuckets?.length > 0;
  }

  isSelected(bucket: BasicBucket): boolean {
    return bucket.id === this.handler.getContext().bucket?.connect?.id;
  }

  getBucketFormData(): BucketData {
    const discovery = this.handler.getDiscovery();

    if (this.handler.getContext()?.bucket?.create) {
      return this.handler.getContext()?.bucket?.create as BucketData;
    } else if (discovery) {
      const { document } = discovery;
      return {
        title: document.title,
        description: document.description,
        websiteUrl: document.url,
        visibility: GqlVisibility.IsPublic,
        tags: '',
        imageUrl: '',
      };
    } else {
      return {
        title: '',
        description: '',
        websiteUrl: '',
        visibility: GqlVisibility.IsPublic,
        tags: '',
        imageUrl: '',
      };
    }
  }

  label(visibility: GqlVisibility): string {
    return visibilityToLabel(visibility);
  }

  async handleChange(event: any) {
    console.log('handleChange', event.detail.value);
    if (event.detail.value === this.accordionValueCreate) {
      console.log('updateContext', this.createBucketData);
      await this.handler.updateContext({
        bucket: {
          create: this.createBucketData.data,
        },
        isCurrentStepValid: this.createBucketData.valid,
      });
    } else {
      await this.bubbleExistingBucket();
    }
  }

  private async bubbleExistingBucket() {
    if (this.existingBucketId) {
      console.log('updateContext', this.existingBucketId);
      await this.handler.updateContext({
        bucket: {
          connect: {
            id: this.existingBucketId,
          },
        },
        isCurrentStepValid: !isUndefined(this.existingBucketId),
      });
    }
  }
}
