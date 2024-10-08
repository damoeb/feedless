import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { SelectComponent } from './select.component';
import { SelectModule } from './select.module';
import { AppTestModule } from '../../app-test.module';

describe('SelectComponent', () => {
  let component: SelectComponent<any>;
  let fixture: ComponentFixture<SelectComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [SelectModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
