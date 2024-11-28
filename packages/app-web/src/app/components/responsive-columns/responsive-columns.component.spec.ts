import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ResponsiveColumnsComponent } from './responsive-columns.component';
import { AppTestModule } from '../../app-test.module';
import { ResponsiveColumnsModule } from './responsive-columns.module';

describe('ResponsiveColumnsComponent', () => {
  let component: ResponsiveColumnsComponent;
  let fixture: ComponentFixture<ResponsiveColumnsComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ResponsiveColumnsComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ResponsiveColumnsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
