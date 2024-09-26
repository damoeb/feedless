import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RepositoryModalComponent } from './repository-modal.component';
import { IonicModule } from '@ionic/angular';
import { ReactiveFormsModule } from '@angular/forms';
import { FilterItemsAccordionModule } from '../../components/filter-items-accordion/filter-items-accordion.module';
import { FetchRateAccordionModule } from '../../components/fetch-rate-accordion/fetch-rate-accordion.module';
import { RouterLink } from '@angular/router';
import { RemoveIfProdModule } from '../../directives/remove-if-prod/remove-if-prod.module';

@NgModule({
  declarations: [RepositoryModalComponent],
  exports: [RepositoryModalComponent],
  imports: [
    CommonModule,
    IonicModule,
    ReactiveFormsModule,
    FilterItemsAccordionModule,
    FetchRateAccordionModule,
    RouterLink,
    RemoveIfProdModule,
  ],
})
export class RepositoryModalModule {}
