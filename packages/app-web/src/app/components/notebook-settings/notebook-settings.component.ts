import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { NotebookService, NotebookSettings } from '../../services/notebook.service';
import { map, Subscription } from 'rxjs';

import {
  IonButton,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonItemDivider,
  IonLabel,
  IonList,
  IonPopover,
  IonText,
  IonTitle,
  IonToolbar,
} from '@ionic/angular/standalone';
import { SessionService } from '../../services/session.service';
import { NestedKeys, TypeAtPath } from '../../types';
import { AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-notebook-settings',
  templateUrl: './notebook-settings.component.html',
  styleUrls: ['./notebook-settings.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonList,
    IonLabel,
    IonItem,
    IonButton,
    IonItemDivider,
    IonText,
    IonHeader,
    IonToolbar,
    IonTitle,
    IonContent,
    IonIcon,
    IonPopover,
    AsyncPipe,
  ],
  standalone: true,
})
export class NotebookSettingsComponent implements OnInit, OnDestroy {
  private readonly notebookService = inject(NotebookService);
  private readonly sessionService = inject(SessionService);
  private readonly changeRef = inject(ChangeDetectorRef);

  private subscriptions: Subscription[] = [];
  readerOptions: any = {};
  contentWidthStepSize: number = 20;
  isDarkMode: boolean;

  constructor() {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.sessionService.watchColorScheme().subscribe((isDarkMode) => {
        this.isDarkMode = isDarkMode;
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  ifActiveOption<T extends NestedKeys<NotebookSettings>, V extends TypeAtPath<NotebookSettings, T>>(
    option: T,
    expectedValue: V
  ) {
    return this.notebookService.getSettingsValue(option).pipe(
      map((value) => {
        if (value == expectedValue) {
          return 'primary';
        }
        return 'light';
      })
    );
  }

  changeOption<T extends NestedKeys<NotebookSettings>, V extends TypeAtPath<NotebookSettings, T>>(
    option: T,
    value: V
  ) {
    // this.readerOptions[option] = value;
    // this.changeRef.detectChanges();
  }

  getOption<T extends NestedKeys<NotebookSettings>>(option: T) {
    return this.notebookService.getSettingsValue(option);
  }

  changeNumOption<
    T extends NestedKeys<NotebookSettings>,
    V extends TypeAtPath<NotebookSettings, T> & number,
  >(numOption: T, increment: number, constraints: { min: number; max: number }) {
    // const value: number = this.getOption(numOption);
    // this.changeOption(
    //   numOption,
    //   parseFloat(
    //     Math.max(
    //       Math.min(value + increment, constraints.max),
    //       constraints.min,
    //     ).toFixed(1),
    //   ) as V,
    // );
  }
}
