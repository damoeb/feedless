import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TableComponent } from './table.component';
import { TableModule } from './table.module';
import { AppTestModule } from '../../app-test.module';

describe('TableComponent', () => {
  let component: TableComponent;
  let fixture: ComponentFixture<TableComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TableModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
