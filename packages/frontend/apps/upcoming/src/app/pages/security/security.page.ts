import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  IonButton,
  IonContent,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
} from '@ionic/angular/standalone';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { SessionService } from '@feedless/components';
import type { User, UserSecret } from '@feedless/graphql-api';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-security-page',
  templateUrl: './security.page.html',
  styleUrls: ['./security.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonButton,
    IonItem,
    IonLabel,
    IonList,
    IonNote,
    RouterLink,
    DatePipe,
  ],
  standalone: true,
})
export class SecurityPage implements OnInit, OnDestroy {
  private readonly sessionService = inject(SessionService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroy$ = new Subject<void>();

  protected user: User | null = null;
  protected busy = false;
  // The mutation is the only response carrying the unmasked value.
  protected createdSecret: UserSecret | null = null;

  ngOnInit(): void {
    this.sessionService
      .getSession()
      .pipe(takeUntil(this.destroy$))
      .subscribe((session) => {
        this.user = session?.user ?? null;
        this.cdr.markForCheck();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected async createSecret(): Promise<void> {
    this.busy = true;
    this.cdr.markForCheck();
    try {
      this.createdSecret = await this.sessionService.createUserSecret();
      await this.sessionService.fetchSession('network-only');
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }

  protected async deleteSecret(id: string): Promise<void> {
    if (!confirm('Revoke this token? It will stop working immediately.')) {
      return;
    }
    this.busy = true;
    this.cdr.markForCheck();
    try {
      await this.sessionService.deleteUserSecret({ where: { eq: id } });
      if (this.createdSecret?.id === id) {
        this.createdSecret = null;
      }
      await this.sessionService.fetchSession('network-only');
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
