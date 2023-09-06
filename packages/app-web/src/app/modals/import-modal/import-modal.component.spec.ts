import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportModalComponent } from './import-modal.component';
import { ImportModalModule } from './import-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('ImportModalComponent', () => {
  let component: ImportModalComponent;
  let fixture: ComponentFixture<ImportModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImportModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ImportModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
