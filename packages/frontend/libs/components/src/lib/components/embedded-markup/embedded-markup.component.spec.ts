import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmbeddedMarkupComponent } from './embedded-markup.component';
import { AppTestModule } from '@feedless/testing';
import { ScrapeService } from '../../services';
import { SourceBuilder } from '../../source/source-builder';

describe('EmbeddedMarkupComponent', () => {
  let component: EmbeddedMarkupComponent;
  let fixture: ComponentFixture<EmbeddedMarkupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmbeddedMarkupComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmbeddedMarkupComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput(
      'sourceBuilder',
      SourceBuilder.fromUrl('', TestBed.inject(ScrapeService)),
    );

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
