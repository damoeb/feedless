import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FilterToolbarComponent } from './filter-toolbar.component';
import { FilterToolbarModule } from './filter-toolbar.module';
import { AppTestModule } from '../../app-test.module';

describe('FilterToolbarComponent', () => {
  let component: FilterToolbarComponent<any>;
  let fixture: ComponentFixture<FilterToolbarComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FilterToolbarModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FilterToolbarComponent);
    component = fixture.componentInstance;
    component.filters = {};
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
