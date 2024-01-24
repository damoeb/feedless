import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InputComponent } from './input.component';
import { InputModule } from './input.module';

describe('InputComponent', () => {
  let component: InputComponent<any>;
  let fixture: ComponentFixture<InputComponent<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [InputModule]
    }).compileComponents();

    fixture = TestBed.createComponent(InputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
