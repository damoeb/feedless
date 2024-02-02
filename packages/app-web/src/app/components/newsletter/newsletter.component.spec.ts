import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { NewsletterComponent } from './newsletter.component';
import { NewsletterModule } from './newsletter.module';
import { AppTestModule } from '../../app-test.module';

describe('NewsletterComponent', () => {
  let component: NewsletterComponent;
  let fixture: ComponentFixture<NewsletterComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [NewsletterModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(NewsletterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
