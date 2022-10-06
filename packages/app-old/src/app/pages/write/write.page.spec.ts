import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { WritePage } from './write.page';
import { WritePageModule } from './write.module';

describe('WritePage', () => {
  let component: WritePage;
  let fixture: ComponentFixture<WritePage>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [WritePageModule],
      }).compileComponents();

      fixture = TestBed.createComponent(WritePage);
      component = fixture.componentInstance;
      fixture.detectChanges();
    })
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
