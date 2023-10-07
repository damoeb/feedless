import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ScrapeSourceModalComponent } from './scrape-source-modal.component';
import { ScrapeSourceModalModule } from './scrape-source-modal.module';

describe('ScrapeSourceModal', () => {
  let component: ScrapeSourceModalComponent;
  let fixture: ComponentFixture<ScrapeSourceModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ScrapeSourceModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ScrapeSourceModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
