import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InteractiveWebsiteComponent } from './interactive-website.component';
import { InteractiveWebsiteModule } from './interactive-website.module';
import { AppTestModule } from '../../app-test.module';
import { ScrapeController } from './scrape-controller';

describe('InteractiveWebsiteComponent', () => {
  let component: InteractiveWebsiteComponent;
  let fixture: ComponentFixture<InteractiveWebsiteComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [InteractiveWebsiteModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(InteractiveWebsiteComponent);
    component = fixture.componentInstance;
    const scrapeController = new ScrapeController({
      title: '',
      flow: { sequence: [] },
    });
    component.scrapeController = scrapeController;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
