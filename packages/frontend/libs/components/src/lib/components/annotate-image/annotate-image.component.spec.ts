import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnnotateImageComponent } from './annotate-image.component';
import { AppTestModule } from '@feedless/testing';
import { SourceBuilder } from '@feedless/source';
import { ScrapeService } from '@feedless/services';

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
