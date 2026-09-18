import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlowModalComponent } from './flow-modal.component';
import { AppTestModule } from '@feedless/testing';

describe('FlowModalComponent', () => {
  let component: FlowModalComponent;
  let fixture: ComponentFixture<FlowModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlowModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FlowModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
