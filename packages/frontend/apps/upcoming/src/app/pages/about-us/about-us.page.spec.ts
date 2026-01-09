import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { AppTestModule } from '@feedless/testing';
import { AboutUsPage } from './about-us.page';

describe('AboutUsPage', () => {
  let component: AboutUsPage;
  let fixture: ComponentFixture<AboutUsPage>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutUsPage, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AboutUsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
