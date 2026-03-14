import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  IonButton,
  IonContent,
  IonInput,
  IonItem,
} from '@ionic/angular/standalone';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { SessionService } from '@feedless/components';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-account-page',
  templateUrl: './account.page.html',
  styleUrls: ['./account.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, IonInput, IonItem, IonButton, ReactiveFormsModule],
  standalone: true,
})
export class AccountPage implements OnInit, OnDestroy {
  private readonly sessionService = inject(SessionService);
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroy$ = new Subject<void>();

  protected form = this.fb.nonNullable.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    country: [''],
  });

  protected email = '';

  ngOnInit(): void {
    this.sessionService
      .getSession()
      .pipe(takeUntil(this.destroy$))
      .subscribe((session) => {
        const user = session?.user;
        if (user) {
          this.email = user.email ?? '';
          this.form.patchValue({
            firstName: user.firstName ?? '',
            lastName: user.lastName ?? '',
            country: user.country ?? '',
          });
          this.cdr.markForCheck();
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected async save(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    await this.sessionService.updateCurrentUser({
      firstName: { set: v.firstName },
      lastName: { set: v.lastName },
      ...(v.country !== undefined &&
        v.country !== '' && { country: { set: v.country } }),
    });
    this.cdr.markForCheck();
  }
}
