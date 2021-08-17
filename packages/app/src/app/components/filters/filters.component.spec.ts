import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { FiltersComponent } from './filters.component';
import { FiltersModule } from './filters.module';

describe('FiltersComponent', () => {
  let component: FiltersComponent;
  let fixture: ComponentFixture<FiltersComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [FiltersModule],
      }).compileComponents();

      fixture = TestBed.createComponent(FiltersComponent);
      component = fixture.componentInstance;
      component.bucket = {} as any;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
