import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BlockElementComponent } from './block-element.component';
import { AppTestModule } from '../../app-test.module';

describe('BlockElementComponent', () => {
  let component: BlockElementComponent;
  let fixture: ComponentFixture<BlockElementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BlockElementComponent, AppTestModule],
    }).compileComponents();

    fixture = TestBed.createComponent(BlockElementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
