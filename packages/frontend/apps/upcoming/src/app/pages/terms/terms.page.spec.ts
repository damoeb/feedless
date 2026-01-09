import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { TermsPage } from './terms.page';

describe('TermsPage', () => {
  let component: TermsPage;
  let fixture: ComponentFixture<TermsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [TermsPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TermsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
