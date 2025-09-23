import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConsoleButtonComponent } from './console-button.component';
import { AppTestModule } from '../../app-test.module';

describe('ConsoleButtonComponent', () => {
  let component: ConsoleButtonComponent;
  let fixture: ComponentFixture<ConsoleButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConsoleButtonComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ConsoleButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
