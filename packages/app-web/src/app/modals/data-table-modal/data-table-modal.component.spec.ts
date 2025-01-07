import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataTableModalComponent } from './data-table-modal.component';
import { AppTestModule } from '../../app-test.module';
import { DataTableModalModule } from './data-table-modal.module';

describe('DataTableModalComponent', () => {
  let component: DataTableModalComponent;
  let fixture: ComponentFixture<DataTableModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataTableModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(DataTableModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
