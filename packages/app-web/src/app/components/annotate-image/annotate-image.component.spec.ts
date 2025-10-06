import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotateImageComponent } from './annotate-image.component';
import { AppTestModule } from '../../app-test.module';
import { SourceBuilder } from '../interactive-website/source-builder';
import { ScrapeService } from '../../services/scrape.service';

describe('EmbeddedImageComponent', () => {
  let component: AnnotateImageComponent;
  let fixture: ComponentFixture<AnnotateImageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnnotateImageComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(AnnotateImageComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput(
      'sourceBuilder',
      SourceBuilder.fromUrl('', TestBed.inject(ScrapeService)),
    );
    componentRef.setInput('embed', {
      data: '',
      url: '',
      mimeType: '',
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
