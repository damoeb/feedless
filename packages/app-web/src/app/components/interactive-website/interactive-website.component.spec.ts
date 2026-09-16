import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InteractiveWebsiteComponent } from './interactive-website.component';
import { AppTestModule, mockScrape } from '../../app-test.module';
import { SourceBuilder } from './source-builder';
import { ScrapeService } from '../../services/scrape.service';

describe('InteractiveWebsiteComponent', () => {
  let component: InteractiveWebsiteComponent;
  let fixture: ComponentFixture<InteractiveWebsiteComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InteractiveWebsiteComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockScrape(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InteractiveWebsiteComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput(
      'sourceBuilder',
      SourceBuilder.fromUrl('', TestBed.inject(ScrapeService))
    );

    fixture.detectChanges();
  }));

  afterEach(() => {
    if (component && typeof component.ngOnDestroy === 'function') {
      component.ngOnDestroy();
    }
    fixture.destroy();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('extractElements', () => {
    function extract(xpath: string): HTMLElement[] {
      let actual: HTMLElement[];
      component.sourceBuilder().events.extractElements.next({
        xpath,
        callback: (elements) => (actual = elements),
      });
      return actual;
    }

    beforeEach(async () => {
      await fixture.whenStable();
      component.embedMarkup = {
        mimeType: 'text/html',
        data: '<html><body><ul><li><a href="/a">A</a></li><li><a href="/b">B</a></li></ul></body></html>',
        url: 'https://example.org',
      };
    });

    it('answers while the markup tab is not shown', () => {
      component.viewModeFc.setValue(component.viewModeImage);
      fixture.detectChanges();

      const actual = extract('//li');

      expect(actual.map((el) => el.textContent)).toEqual(['A', 'B']);
    });

    it('answers with no elements for an invalid xpath', () => {
      const actual = extract('//li[');

      expect(actual).toEqual([]);
    });
  });
});
