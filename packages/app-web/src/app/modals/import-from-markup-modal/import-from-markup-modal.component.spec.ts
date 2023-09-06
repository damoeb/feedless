import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportFromMarkupModalComponent } from './import-from-markup-modal.component';
import { ImportFromMarkupModalModule } from './import-from-markup-modal.module';

describe('ImportFromMarkupModalComponent', () => {
  let component: ImportFromMarkupModalComponent;
  let fixture: ComponentFixture<ImportFromMarkupModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImportFromMarkupModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportFromMarkupModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
