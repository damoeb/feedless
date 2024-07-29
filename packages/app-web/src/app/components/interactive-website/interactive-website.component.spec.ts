import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { InteractiveWebsiteComponent } from './interactive-website.component';
import { InteractiveWebsiteModule } from './interactive-website.module';
import { AppTestModule } from '../../app-test.module';

describe('InteractiveWebsiteComponent', () => {
  let component: InteractiveWebsiteComponent;
  let fixture: ComponentFixture<InteractiveWebsiteComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [InteractiveWebsiteModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(InteractiveWebsiteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
