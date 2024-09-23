import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TableComponent } from './table.component';
import { TableModule } from './table.module';
import { AppTestModule } from '../../app-test.module';

xdescribe('TableComponent', () => {
  let component: TableComponent<any>;
  let fixture: ComponentFixture<TableComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TableModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TableComponent);
    component = fixture.componentInstance;
    component.rows = [];
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
