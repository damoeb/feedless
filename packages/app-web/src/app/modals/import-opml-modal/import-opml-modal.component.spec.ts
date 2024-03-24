import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportOpmlModalComponent } from './import-opml-modal.component';
import { ImportOpmlModalModule } from './import-opml-modal.module';

describe('ImportOpmlComponent', () => {
  let component: ImportOpmlModalComponent;
  let fixture: ComponentFixture<ImportOpmlModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImportOpmlModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportOpmlModalComponent);
    component = fixture.componentInstance;
    component.outlines = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
