import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { VisualDiffMenuComponent } from './visual-diff-menu.component';
import { AppTestModule } from '../../app-test.module';

describe('VisualDiffMenuComponent', () => {
  let component: VisualDiffMenuComponent;
  let fixture: ComponentFixture<VisualDiffMenuComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [VisualDiffMenuComponent],
      imports: [AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(VisualDiffMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
