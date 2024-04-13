import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { EmptyRepositoryComponent } from './empty-repository.component';
import { EmptyRepositoryModule } from './empty-repository.module';
import { AppTestModule } from '../../app-test.module';

describe('EmptyRepositoryComponent', () => {
  let component: EmptyRepositoryComponent;
  let fixture: ComponentFixture<EmptyRepositoryComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [EmptyRepositoryModule, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyRepositoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
