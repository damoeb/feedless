import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmbeddedMarkupComponent } from './embedded-markup.component';
import { EmbeddedMarkupModule } from './embedded-markup.module';
import { AppTestModule } from '../../app-test.module';
import { ScrapeController } from '../interactive-website/scrape-controller';

describe('EmbeddedMarkupComponent', () => {
  let component: EmbeddedMarkupComponent;
  let fixture: ComponentFixture<EmbeddedMarkupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EmbeddedMarkupModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmbeddedMarkupComponent);
    component = fixture.componentInstance;
    component.scrapeController = new ScrapeController({
      title: '',
      flow: { sequence: [] },
    });
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
