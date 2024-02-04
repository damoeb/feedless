import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { debounce, interval, Subscription } from 'rxjs';
import { ScrapeResponse } from '../../graphql/types';
import { ProductConfig, ProductService } from '../../services/product.service';
import { FormControl } from '@angular/forms';
import { Note, NotebookService } from './services/notebook.service';
import { IonSearchbar } from '@ionic/angular';

@Component({
  selector: 'app-untold-notes-product-page',
  templateUrl: './untold-notes-product.page.html',
  styleUrls: ['./untold-notes-product.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UntoldNotesProductPage implements OnInit, OnDestroy {
  scrapeResponse: ScrapeResponse;
  productConfig: ProductConfig;
  private subscriptions: Subscription[] = [];
  queryFc = new FormControl<string>('');

  @ViewChild('searchbar')
  searchbarElement: IonSearchbar;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly productService: ProductService,
    private readonly notebookService: NotebookService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.productService
        .getActiveProductConfigChange()
        .subscribe((productConfig) => {
          this.productConfig = productConfig;
          this.changeRef.detectChanges();
        }),
      this.queryFc.valueChanges
        .subscribe(query => {
        this.notebookService.queryChanges.next(query)
      }),
      this.notebookService.focusSearchbar.subscribe(() => {
        this.searchbarElement.setFocus()
      })
    );
  }

  @HostListener('window:keydown.esc', ['$event'])
  handleKeyDown(event: KeyboardEvent) {
    this.notebookService.focusSearchbar.next()
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
