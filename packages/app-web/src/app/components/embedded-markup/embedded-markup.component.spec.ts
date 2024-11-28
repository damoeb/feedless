import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmbeddedMarkupComponent } from './embedded-markup.component';
import { EmbeddedMarkupModule } from './embedded-markup.module';
import { AppTestModule } from '../../app-test.module';
import { SourceBuilder } from '../interactive-website/source-builder';
import { ScrapeService } from '../../services/scrape.service';

describe('EmbeddedMarkupComponent', () => {
  let component: EmbeddedMarkupComponent;
  let fixture: ComponentFixture<EmbeddedMarkupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EmbeddedMarkupComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmbeddedMarkupComponent);
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
