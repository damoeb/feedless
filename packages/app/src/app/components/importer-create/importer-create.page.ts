import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Bucket } from '../../services/bucket.service';
import { FeedDiscovery, FeedService, NativeFeed, SearchResponse } from '../../services/feed.service';
import { ModalController, ToastController } from '@ionic/angular';
import { GqlGenericFeedRule, GqlNativeFeedReference } from '../../../generated/graphql';
import { ImporterService } from '../../services/importer.service';
import { SettingsService } from '../../services/settings.service';

@Component({
  selector: 'app-importer-create',
  templateUrl: './importer-create.page.html',
  styleUrls: ['./importer-create.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImporterCreatePage implements OnInit {

  formGroup: FormGroup<{ website: FormControl<string | null>; description: FormControl<string | null>; name: FormControl<string | null> }>;
  bucket: Bucket;
  loading = false;
  query: string;
  canInspectPage: boolean;
  inspectionResult: FeedDiscovery;
  existingFeeds: SearchResponse;

  constructor(private readonly feedService: FeedService,
              private readonly importerService: ImporterService,
              private readonly settingsService: SettingsService,
              private readonly toastCtrl: ToastController,
              private readonly modalController: ModalController,
              private readonly changeRef: ChangeDetectorRef) {
    this.formGroup = new FormGroup({
      name: new FormControl('', Validators.required),
      description: new FormControl('', Validators.required),
      website: new FormControl('', Validators.required),
    });
  }

  ngOnInit() {
    console.log('this.bucket', this.bucket);
  }

  async search() {
    if (this.query.trim().length > 3) {
      this.loading = true;
      this.canInspectPage = this.isUrl(this.query);
      this.existingFeeds = await this.feedService.searchFeeds(this.query);
      if (this.canInspectPage) {
        this.inspectionResult = await this.inspectPage(this.query);
      }
      this.loading = false;
      this.changeRef.detectChanges();
    }
  }

  async inspectPage(url: string) {
    return this.feedService.discoverFeeds(url);
  }

  private isUrl(value: string): boolean {
    try {
      const potentialUrl = value.toLowerCase();
      if (potentialUrl.startsWith('http://') || potentialUrl.startsWith('https://')) {
        new URL(value)
      } else {
        new URL(`https://${value}`)
      }
      return true;
    } catch (e) {
      return false;
    }
  }

  closeModal() {
    return this.modalController.dismiss()
  }

  async importNativeFeed(data: Pick<GqlNativeFeedReference, 'url' | 'type' | 'description' | 'title'>) {
    const nativeFeed = await this.feedService.createNativeFeed({
      corrId: this.settingsService.getCorrId(),
      feedUrl: data.url,
      description: data.description,
      title: data.title,
      harvestSite: true,
      harvestSiteWithPrerender: false,
      websiteUrl: ''
    })
    await this.importerService.createImporter({
      feedId: nativeFeed.id,
      bucketId: this.bucket.id,
      corrId: this.settingsService.getCorrId(),
      autoRelease: true
    });

    const toast = await this.toastCtrl.create({
      message: 'Created',
      duration: 3000,
      color: 'success',
    });

    await toast.present();
    await this.modalController.dismiss();
  }

  importGenericFeed(genericFeedRule: Pick<GqlGenericFeedRule, 'feedUrl' | 'score' | 'linkXPath' | 'extendContext' | 'dateXPath' | 'count' | 'contextXPath'>) {
    console.log('showGenericFeed')
  }
}
