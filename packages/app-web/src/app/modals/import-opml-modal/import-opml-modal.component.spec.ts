import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ImportOpmlModalComponent } from './import-opml-modal.component';
import { ImportOpmlModalModule } from './import-opml-modal.module';
import { AppTestModule } from '../../app-test.module';

describe('ImportOpmlModalComponent', () => {
  let component: ImportOpmlModalComponent;
  let fixture: ComponentFixture<ImportOpmlModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ImportOpmlModalModule, AppTestModule.withDefaults()],
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
