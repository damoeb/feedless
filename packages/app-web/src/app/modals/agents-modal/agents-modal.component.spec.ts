import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { AgentsModalComponent } from './agents-modal.component';
import { AgentsModalModule } from './agents-modal.module';

describe('AgentsModalComponent', () => {
  let component: AgentsModalComponent;
  let fixture: ComponentFixture<AgentsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [AgentsModalModule]
    }).compileComponents();

    fixture = TestBed.createComponent(AgentsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
