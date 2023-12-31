import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ScrapeSourceComponent } from './scrape-source.component';
import { AppTestModule } from '../../app-test.module';
import { ScrapeSourceModule } from './scrape-source.module';

describe('ScrapeSourceComponent', () => {
  let component: ScrapeSourceComponent;
  let fixture: ComponentFixture<ScrapeSourceComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ScrapeSourceComponent],
      imports: [ScrapeSourceModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ScrapeSourceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  fit('should create', () => {
    expect(component).toBeTruthy();
  });
});
