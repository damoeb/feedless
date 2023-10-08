import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TransformWebsiteToFeedComponent } from './transform-website-to-feed.component';
import { IonicModule } from '@ionic/angular';
import { WizardModule } from '../wizard/wizard.module';
import { EmbeddedWebsiteModule } from '../embedded-website/embedded-website.module';
import { RemoteFeedModule } from '../remote-feed/remote-feed.module';



@NgModule({
  declarations: [TransformWebsiteToFeedComponent],
  exports: [TransformWebsiteToFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    WizardModule,
    EmbeddedWebsiteModule,
    RemoteFeedModule
  ]
})
export class TransformWebsiteToFeedModule { }
