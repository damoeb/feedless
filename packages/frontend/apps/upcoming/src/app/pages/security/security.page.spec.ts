import { ChangeDetectorRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppTestModule } from '@feedless/testing';
// eslint-disable-next-line @nx/enforce-module-boundaries
import { SessionService } from '@feedless/components';
import { SecurityPage } from './security.page';

describe('SecurityPage', () => {
  const laptop = {
    id: 's1',
    name: 'laptop',
    value: '••••a1b2c3',
    valueMasked: true,
    lastUsed: Date.UTC(2026, 8, 14, 10, 0),
    validUntil: Date.UTC(2027, 8, 5),
    type: 'SecretKey',
  };
  const ci = { ...laptop, id: 's2', name: 'ci', value: '••••z9y8x7', lastUsed: null as number | null };

  let fixture: ComponentFixture<SecurityPage>;
  let component: SecurityPage;
  let createUserSecret: ReturnType<typeof vi.fn>;
  let fetchSession: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    createUserSecret = vi.fn().mockResolvedValue({
      ...laptop,
      id: 's3',
      value: 'eyJhbGciOiJIUzI1NiJ9.full.token',
      valueMasked: false,
    });
    fetchSession = vi.fn().mockResolvedValue(undefined);

    await TestBed.configureTestingModule({
      imports: [SecurityPage, AppTestModule.withDefaults()],
      providers: [
        {
          provide: SessionService,
          useValue: {
            getSession: () => of({ isLoggedIn: true, user: { secrets: [laptop, ci] } }),
            createUserSecret,
            fetchSession,
            deleteUserSecret: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SecurityPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  const el = () => fixture.nativeElement as HTMLElement;
  const texts = (selector: string) =>
    Array.from(el().querySelectorAll(selector)).map((node) => node.textContent?.trim() ?? '');
  const createButton = () =>
    el().querySelector('form.create-secret ion-button') as HTMLElement & { disabled: boolean };
  // The page is OnPush, so a change made from outside must mark it before rendering.
  const render = () => {
    fixture.debugElement.injector.get(ChangeDetectorRef).markForCheck();
    fixture.detectChanges();
  };

  it('lists each token by name with its masked value and last use', () => {
    expect(texts('.secret-name')).toEqual(['laptop', 'ci']);
    expect(texts('.secret-masked')).toEqual(['••••a1b2c3', '••••z9y8x7']);
    const lastUsed = texts('.secret-last-used');
    expect(lastUsed[0]).not.toContain('never');
    expect(lastUsed[1]).toContain('never');
  });

  it('keeps create disabled until a name is entered', () => {
    expect(createButton().disabled).toBe(true);

    component['secretName'] = 'laptop';
    render();

    expect(createButton().disabled).toBe(false);
  });

  it('creates the token under the trimmed name and shows its value once', async () => {
    component['secretName'] = '  laptop  ';

    await component['createSecret']();
    render();

    expect(createUserSecret).toHaveBeenCalledWith('laptop');
    expect(fetchSession).toHaveBeenCalled();
    expect(component['secretName']).toBe('');
    expect(texts('.secret-value')).toEqual(['eyJhbGciOiJIUzI1NiJ9.full.token']);
  });

  it('creates nothing for a blank name', async () => {
    component['secretName'] = '   ';

    await component['createSecret']();

    expect(createUserSecret).not.toHaveBeenCalled();
  });
});
