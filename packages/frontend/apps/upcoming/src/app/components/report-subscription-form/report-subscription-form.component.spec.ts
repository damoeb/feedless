import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { ReportSubscriptionFormComponent } from './report-subscription-form.component';
import { ReportSubscriptionValue } from './report-subscription';

describe('ReportSubscriptionFormComponent', () => {
  let fixture: ComponentFixture<ReportSubscriptionFormComponent>;
  let component: ReportSubscriptionFormComponent;
  let emitted: ReportSubscriptionValue[];

  const render = async (initial: ReportSubscriptionValue | null = null) => {
    await TestBed.configureTestingModule({
      imports: [ReportSubscriptionFormComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReportSubscriptionFormComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('initial', initial);
    emitted = [];
    component.submitted.subscribe((value) => emitted.push(value));
    fixture.detectChanges();
  };

  const element = () => fixture.nativeElement as HTMLElement;

  afterEach(() => TestBed.resetTestingModule());

  describe('when creating', () => {
    beforeEach(() => render());

    it('emits nothing while required fields are empty', () => {
      component.submit();

      expect(emitted).toEqual([]);
    });

    it('rejects a name shorter than three characters', () => {
      component.form.patchValue({
        name: 'Hi',
        email: 'hans@example.com',
        acceptedTerms: true,
      });
      component.submit();

      expect(emitted).toEqual([]);
    });

    it('rejects an address that is not an email', () => {
      component.form.patchValue({
        name: 'Hans Muster',
        email: 'kein-mail',
        acceptedTerms: true,
      });
      component.submit();

      expect(emitted).toEqual([]);
    });

    it('requires the terms to be accepted', () => {
      component.form.patchValue({ name: 'Hans Muster', email: 'hans@example.com' });
      component.submit();

      expect(emitted).toEqual([]);
    });

    it('emits name and email once everything is valid', () => {
      component.form.patchValue({
        name: 'Hans Muster',
        email: 'hans@example.com',
        acceptedTerms: true,
      });
      component.submit();

      expect(emitted).toEqual([{ name: 'Hans Muster', email: 'hans@example.com' }]);
    });

    it('offers the terms checkbox and a subscribe button', () => {
      expect(element().querySelector('ion-checkbox')).toBeTruthy();
      expect(element().textContent).toContain('Abonnieren');
    });
  });

  /**
   * Das Formular soll später auch ein bestehendes Abo bearbeiten - etwa unter
   * /profile/subscriptions. Bearbeiten ist heute im Backend noch nicht
   * möglich; diese Fälle halten fest, dass das Formular es bereits trägt.
   */
  describe('when editing', () => {
    const existing = { name: 'Hans Muster', email: 'hans@example.com' };

    beforeEach(() => render(existing));

    it('starts with the existing values', () => {
      expect(component.form.getRawValue()).toMatchObject(existing);
    });

    it('does not ask for the terms again, they were accepted on signup', () => {
      expect(element().querySelector('ion-checkbox')).toBeNull();

      component.submit();
      expect(emitted).toEqual([existing]);
    });

    it('saves instead of subscribing', () => {
      expect(element().textContent).toContain('Speichern');
      expect(element().textContent).not.toContain('Abonnieren');
    });

    it('emits the changed values', () => {
      component.form.patchValue({ email: 'neu@example.com' });
      component.submit();

      expect(emitted).toEqual([{ name: 'Hans Muster', email: 'neu@example.com' }]);
    });
  });
});
