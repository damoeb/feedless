import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyRepositoryComponent } from './empty-repository.component';

import { AppTestModule } from '../../app-test.module';

describe('EmptyRepositoryComponent', () => {
  let component: EmptyRepositoryComponent;
  let fixture: ComponentFixture<EmptyRepositoryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyRepositoryComponent, AppTestModule.withDefaults()],
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyRepositoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
