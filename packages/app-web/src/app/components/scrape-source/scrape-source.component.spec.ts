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

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  fdescribe('#map-to FG', () => {
    it('', () => {
      component.source = {
        request: null,
        response: {
          failed: false,
          elements: [],
          url: 'https://foo.bar',
          debug: {
            metrics: {
              queue: 0,
              render: 0,
            },
            contentType: 'text/html',
            html: null,
            screenshot: null,
            statusCode: 200,
            cookies: [],
            console: [],
          },
        },
      };
      component.ngOnInit();
      fixture.detectChanges();
      expect(component.mapperFg.invalid).toBeTrue();
      component.mapperFg.patchValue({
        type: 'fragment',
        oneOf: {
          fragment: {
            fragmentType: 'boundingBox',
            oneOf: {
              boundingBox: {
                x: 0,
                y: 0,
                h: 0,
                w: 0,
              },
            },
          },
        },
      });

      // expect(component.mapperFg.valid).toBeFalse();
      // component.mapperFg.controls.oneOf.controls.fragment.controls.oneOf.controls.boundingBox.patchValue({
      //   x: 0,
      //   y: 0,
      //   h: 10,
      //   w: 10
      // });
      //
      // expect(component.mapperFg.valid).toBeTrue();
      // component.mapperFg.patchValue({
      //   type: 'readability'
      // })
      // expect(component.mapperFg.valid).toBeTrue();
      // component.mapperFg.patchValue({
      //   type: 'feed'
      // });
      // expect(component.mapperFg.valid).toBeFalse();
      // component.mapperFg.patchValue({
      //   type: 'fragment',
      // });
      // // console.log(JSON.stringify(getFormControlStatus(component.mapperFg), null, 2));
      // expect(component.mapperFg.valid).toBeTrue();
    });
  });
});
