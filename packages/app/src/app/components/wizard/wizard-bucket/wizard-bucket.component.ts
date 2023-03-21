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
  private pagination: Pagination;

  constructor(
    private readonly bucketService: BucketService,
    private readonly changeRef: ChangeDetectorRef,
    private readonly profileService: ProfileService
  ) {}

  async ngOnInit() {
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
    await this.handler.updateContext({
      bucket: {
        connect: {
          id: bucket.id,
        },
      },
      isCurrentStepValid: true,
    });
  }

  async createBucket(data: BucketFormData) {
    await this.handler.updateContext({
      bucket: {
        create: data.data,
      },
      isCurrentStepValid: data.valid,
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
    const { document } = discovery;
    if (this.handler.getContext()?.bucket?.create) {
      return this.handler.getContext()?.bucket?.create as BucketData;
    } else {
      return {
        title: document.title,
        description: document.description,
        websiteUrl: discovery.websiteUrl,
        visibility: GqlVisibility.IsProtected,
        tags: '',
        imageUrl: '',
      };
    }
  }
}
