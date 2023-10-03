import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VisualDiffPage } from './visual-diff.page';

describe('VisualDiffPage', () => {
  let component: VisualDiffPage;
  let fixture: ComponentFixture<VisualDiffPage>;

  beforeEach(async(() => {
    fixture = TestBed.createComponent(VisualDiffPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
