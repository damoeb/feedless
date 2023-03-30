import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NativeFeedComponent } from './native-feed.component';
import { IonicModule } from '@ionic/angular';
import { ArticleRefModule } from '../article-ref/article-ref.module';
import { RouterLink } from '@angular/router';
import { FilterToolbarModule } from '../filter-toolbar/filter-toolbar.module';
import { SubscribeModalModule } from '../../modals/subscribe-modal/subscribe-modal.module';
import { BubbleModule } from '../bubble/bubble.module';
import { WizardModule } from '../wizard/wizard.module';

@NgModule({
  declarations: [NativeFeedComponent],
  exports: [NativeFeedComponent],
  imports: [
    CommonModule,
    IonicModule,
    ArticleRefModule,
    RouterLink,
    FilterToolbarModule,
    SubscribeModalModule,
    BubbleModule,
    WizardModule,
  ],
})
export class NativeFeedModule {}
