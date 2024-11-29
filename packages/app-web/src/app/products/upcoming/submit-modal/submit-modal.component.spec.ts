import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubmitModalComponent } from './submit-modal.component';
import { AppTestModule } from '../../../app-test.module';

describe('SubmitModalComponent', () => {
  let component: SubmitModalComponent;
  let fixture: ComponentFixture<SubmitModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SubmitModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(SubmitModalComponent);
    component = fixture.componentInstance;
    component.location = {} as any;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});