import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-language-button',
  templateUrl: './language-button.component.html',
  styleUrls: ['./language-button.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LanguageButtonComponent implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  protected langFc = new FormControl<string>(null);
  protected languages: string[];

  constructor(
    private readonly translateService: TranslateService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit(): Promise<void> {
    this.languages = this.translateService.getLangs();
    this.subscriptions.push(
      this.translateService.onLangChange.subscribe((lang) => {
        this.langFc.setValue(lang.lang, { emitEvent: false });
        this.changeRef.detectChanges();
      }),
      this.langFc.valueChanges.subscribe((lang) => {
        this.translateService.use(lang);
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
