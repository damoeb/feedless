import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TagsModalComponent } from './tags-modal.component';
import { AppTestModule } from '../../app-test.module';
import { TagsModalModule } from './tags-modal.module';

describe('TagsModalComponent', () => {
  let component: TagsModalComponent;
  let fixture: ComponentFixture<TagsModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TagsModalModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(TagsModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
