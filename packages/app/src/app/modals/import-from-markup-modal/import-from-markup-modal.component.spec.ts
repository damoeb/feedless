import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { ImportFromMarkupModalComponent } from './import-from-markup-modal.component';

describe('ImportFromMarkupModalComponent', () => {
  let component: ImportFromMarkupModalComponent;
  let fixture: ComponentFixture<ImportFromMarkupModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ImportFromMarkupModalComponent],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportFromMarkupModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
