import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { TagsModalComponent } from './tags-modal.component';
import { TagsModalModule } from './tags-modal.module';

describe('ExportModalComponent', () => {
  let component: TagsModalComponent;
  let fixture: ComponentFixture<TagsModalComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TagsModalModule],
    }).compileComponents();

    fixture = TestBed.createComponent(TagsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
