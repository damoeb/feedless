import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { VisualDiffMenuComponent } from './visual-diff-menu.component';
import { AppTestModule } from '../../../app-test.module';
import { VisualDiffMenuModule } from './visual-diff-menu.module';

describe('VisualDiffMenuComponent', () => {
  let component: VisualDiffMenuComponent;
  let fixture: ComponentFixture<VisualDiffMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [VisualDiffMenuModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(VisualDiffMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
