import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportOpmlModalComponent } from './import-opml-modal.component';
import { AppTestModule } from '@feedless/testing';

describe('ImportOpmlModalComponent', () => {
  let component: ImportOpmlModalComponent;
  let fixture: ComponentFixture<ImportOpmlModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ImportOpmlModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportOpmlModalComponent);
    component = fixture.componentInstance;
    component.outlines = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
