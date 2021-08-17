import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ToolbarComponent } from './toolbar.component';
import { ToolbarModule } from './toolbar.module';

describe('ToolbarComponent', () => {
  let component: ToolbarComponent;
  let fixture: ComponentFixture<ToolbarComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ToolbarModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ToolbarComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
