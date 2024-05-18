import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { ReaderComponent } from './reader.component';
import { ReaderModule } from './reader.module';
import { AppTestModule } from '../../app-test.module';

describe('ReaderComponent', () => {
  let component: ReaderComponent;
  let fixture: ComponentFixture<ReaderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [ReaderModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(ReaderComponent);
    component = fixture.componentInstance;
    component.html = '';
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
