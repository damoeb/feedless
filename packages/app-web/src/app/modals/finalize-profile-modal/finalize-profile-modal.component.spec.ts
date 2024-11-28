import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FinalizeProfileModalComponent } from './finalize-profile-modal.component';
import { AppTestModule } from '../../app-test.module';

describe('FinalizeProfileModalComponent', () => {
  let component: FinalizeProfileModalComponent;
  let fixture: ComponentFixture<FinalizeProfileModalComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [FinalizeProfileModalComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(FinalizeProfileModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
