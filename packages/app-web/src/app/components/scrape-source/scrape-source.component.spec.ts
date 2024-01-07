import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ScrapeSourceComponent } from './scrape-source.component';
import { AppTestModule } from '../../app-test.module';
import { ScrapeSourceModule } from './scrape-source.module';
import { getFormControlStatus } from '../../modals/feed-builder-modal/feed-builder-modal.component';
import { ResponseMapper } from '../../modals/feed-builder-modal/scrape-builder';

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
  describe('#map-to FG', () => {
    describe('assign', () => {
      const noParamResponseMapperList: ResponseMapper[] = ['readability', 'pageScreenshot', 'pageMarkup'];
      noParamResponseMapperList.forEach(type => {
        it(type, () => {
          component.mapperFg.controls.type.setValue(type)
          fixture.detectChanges();
          expect(component.mapperFg.enabled).toBeFalse();
        });
      })
      it('fragment', () => {
        component.mapperFg.controls.type.setValue('fragment')
        // fixture.detectChanges();
        expect(component.mapperFg.enabled).toBeTrue();
        expect(component.mapperFg.valid).toBeFalse();
        component.mapperFg.controls.oneOf.controls.fragment.patchValue({
          fragmentType: 'selector',
          oneOf: {
            selector: {
              xpath: '/',
              includeImage: false
            }
          }
        });
        expect(component.mapperFg.valid).toBeTrue();

        component.mapperFg.controls.oneOf.controls.fragment.patchValue({
          fragmentType: 'boundingBox',
          oneOf: {
            boundingBox: {
              x: 0,
              y: 0,
              w: 0,
              h: 0,
            }
          }
        });

        expect(component.mapperFg.valid).toBeFalse();
        component.mapperFg.controls.oneOf.controls.fragment.controls.oneOf.controls.boundingBox.setValue({
            x: 0,
            y: 0,
            w: 10,
            h: 10,
          }
        );
        expect(component.mapperFg.valid).toBeTrue();

        component.mapperFg.controls.type.setValue('feed');
        expect(component.mapperFg.valid).toBeFalse();

        component.mapperFg.controls.type.setValue('fragment');
        fixture.detectChanges();
        // console.log(JSON.stringify(getFormControlStatus(component.mapperFg), null, 2))
        expect(component.mapperFg.valid).toBeTrue();

      });
    });
  });
});
