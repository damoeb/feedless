import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WizardComponent } from './wizard/wizard.component';
import { IonicModule } from '@ionic/angular';
import { WizardFeedsComponent } from './wizard-feeds/wizard-feeds.component';
import { FormControl, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BubbleModule } from '../bubble/bubble.module';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { PreviewRemoteFeedModule } from '../preview-remote-feed/preview-remote-feed.module';
import { WizardGenericFeedsComponent } from './wizard-generic-feeds/wizard-generic-feeds.component';
import { WizardNativeFeedComponent } from './wizard-native-feed/wizard-native-feed.component';
import { WizardPageChangeComponent } from './wizard-page-change/wizard-page-change.component';
import { WizardImporterComponent } from './wizard-importer/wizard-importer.component';
import { WizardSourceComponent } from './wizard-source/wizard-source.component';
import { WizardFetchOptionsComponent } from './wizard-fetch-options/wizard-fetch-options.component';
import { WizardBucketComponent } from './wizard-bucket/wizard-bucket.component';
import { FeatureChipModule } from '../feature-chip/feature-chip.module';

export type TypedFormControls<TControl> = {
  [K in keyof TControl]: FormControl<TControl[K]>;
};

@NgModule({
  declarations: [
    WizardComponent,
    WizardFeedsComponent,
    WizardNativeFeedComponent,
    WizardGenericFeedsComponent,
    WizardPageChangeComponent,
    WizardSourceComponent,
    WizardFetchOptionsComponent,
    WizardImporterComponent,
    WizardBucketComponent,
  ],
  exports: [WizardComponent],
  imports: [
    CommonModule,
    IonicModule,
    FormsModule,
    ReactiveFormsModule,
    BubbleModule,
    EmbeddedWebsiteModule,
    PreviewRemoteFeedModule,
    FeatureChipModule,
  ],
})
export class WizardModule {}
