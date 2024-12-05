import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionModalComponent } from './selection-modal.component';
import { AppTestModule } from '../../app-test.module';
import { SelectionModalModule } from './selection-modal.module';

describe('SelectionModalComponent', () => {
  let component: SelectionModalComponent<string>;
  let fixture: ComponentFixture<SelectionModalComponent<string>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SelectionModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SelectionModalComponent<string>);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
