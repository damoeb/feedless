import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EnclosuresComponent } from './enclosures.component';
import { EnclosuresModule } from './enclosures.module';
import { AppTestModule } from '../../app-test.module';

describe('EnclosureComponent', () => {
  let component: EnclosuresComponent;
  let fixture: ComponentFixture<EnclosuresComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EnclosuresModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EnclosuresComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
