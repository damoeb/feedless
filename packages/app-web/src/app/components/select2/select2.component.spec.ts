import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { IonicModule } from '@ionic/angular';

import { Select2Component } from './select2.component';

describe('SelectComponent', () => {
  let component: Select2Component<any>;
  let fixture: ComponentFixture<Select2Component<any>>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [Select2Component],
      imports: [IonicModule.forRoot()],
    }).compileComponents();

    fixture = TestBed.createComponent(Select2Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
