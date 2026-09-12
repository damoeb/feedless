import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AlertController, ModalController } from '@ionic/angular/standalone';
import { AppTestModule } from '@feedless/testing';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { ReportService } from '@feedless/components';
import { GqlIntervalUnit } from '@feedless/graphql-api';
import { EmailAboModalComponent } from './email-abo-modal.component';

describe('EmailAboModalComponent', () => {
  const zug = {
    lat: 47.1679898,
    lng: 8.5173652,
    place: 'Zug',
    displayName: '6300, Zug',
    area: 'ZG',
    countryCode: 'CH',
  };
  const subscription = { name: 'Hans Muster', email: 'hans@example.com' };

  let fixture: ComponentFixture<EmailAboModalComponent>;
  let component: EmailAboModalComponent;
  let createReport: ReturnType<typeof vi.fn>;
  let dismiss: ReturnType<typeof vi.fn>;
  let createAlert: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    createReport = vi.fn().mockResolvedValue(undefined);
    dismiss = vi.fn().mockResolvedValue(true);
    createAlert = vi.fn().mockResolvedValue({ present: vi.fn() });

    await TestBed.configureTestingModule({
      imports: [EmailAboModalComponent, AppTestModule.withDefaults()],
      providers: [
        { provide: ReportService, useValue: { createReport } },
        { provide: ModalController, useValue: { dismiss } },
        { provide: AlertController, useValue: { create: createAlert } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EmailAboModalComponent);
    component = fixture.componentInstance;
    component.repositoryId = 'repo-1';
    component.location = zug;
    fixture.detectChanges();
  });

  it('renders the subscription form', () => {
    expect(
      (fixture.nativeElement as HTMLElement).querySelector(
        'app-report-subscription-form',
      ),
    ).toBeTruthy();
  });

  it('subscribes the repository to a weekly report around the place', async () => {
    await component.subscribe(subscription);

    expect(createReport).toHaveBeenCalledTimes(1);
    const [repositoryId, segment] = createReport.mock.calls[0];
    expect(repositoryId).toBe('repo-1');
    expect(segment.recipient.email).toEqual(subscription);
    expect(segment.when.scheduled.interval).toBe(GqlIntervalUnit.Week);
    expect(segment.what.latLng.near.point).toEqual({ lat: zug.lat, lng: zug.lng });
  });

  /**
   * Das Backend verschickt erst nach der Bestätigung. Die Meldung muss das
   * sagen, sonst wartet der Besucher auf eine Mail, die nie kommt.
   */
  it('closes and asks the visitor to confirm the link in the mail', async () => {
    await component.subscribe(subscription);

    expect(dismiss).toHaveBeenCalled();
    expect(createAlert.mock.calls[0][0].message).toContain('Link');
  });

  it('does not claim success when the request fails', async () => {
    createReport.mockRejectedValue(new Error('api down'));

    await component.subscribe(subscription);

    expect(dismiss).not.toHaveBeenCalled();
    expect(createAlert.mock.calls[0][0].header).not.toContain('erstellt');
  });
});
