import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InteractiveWebsiteComponent } from './interactive-website.component';
import { InteractiveWebsiteModule } from './interactive-website.module';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { SourceBuilder } from './source-builder';
import { ScrapeService } from '../../services/scrape.service';

describe('InteractiveWebsiteComponent', () => {
  let component: InteractiveWebsiteComponent;
  let fixture: ComponentFixture<InteractiveWebsiteComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        InteractiveWebsiteModule,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InteractiveWebsiteComponent);
    component = fixture.componentInstance;

    component.sourceBuilder = SourceBuilder.fromUrl(
      '',
      TestBed.inject(ScrapeService),
    );
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
