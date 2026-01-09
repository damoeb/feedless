import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InteractiveWebsiteComponent } from './interactive-website.component';
import { AppTestModule, mockScrape } from '@feedless/testing';
import { SourceBuilder } from '@feedless/source';
import { ScrapeService } from '@feedless/services';

describe('InteractiveWebsiteComponent', () => {
  let component: InteractiveWebsiteComponent;
  let fixture: ComponentFixture<InteractiveWebsiteComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        InteractiveWebsiteComponent,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) =>
            mockScrape(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InteractiveWebsiteComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput(
      'sourceBuilder',
      SourceBuilder.fromUrl('', TestBed.inject(ScrapeService)),
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
});
