import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ListViewComponent } from './list-view.component';
import { ListViewModule } from './list-view.module';

describe('ListViewComponent', () => {
  let component: ListViewComponent;
  let fixture: ComponentFixture<ListViewComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [ListViewModule],
      }).compileComponents();

      fixture = TestBed.createComponent(ListViewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
