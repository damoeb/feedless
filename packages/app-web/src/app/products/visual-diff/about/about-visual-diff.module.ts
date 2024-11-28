import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { AboutVisualDiffPage } from './about-visual-diff.page';


import { AboutVisualDiffPageRoutingModule } from './about-visual-diff-routing.module';

import { IonContent } from '@ionic/angular/standalone';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    AboutVisualDiffPageRoutingModule,
    IonContent,
    AboutVisualDiffPage,
],
})
export class AboutVisualDiffModule {}
