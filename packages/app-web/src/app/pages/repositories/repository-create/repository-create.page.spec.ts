import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryCreatePage } from './repository-create.page';
import { RepositoryCreatePageModule } from './repository-create.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule } from '../../../app-test.module';

describe('RepositoryCreatePage', () => {
  let component: RepositoryCreatePage;
  let fixture: ComponentFixture<RepositoryCreatePage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryCreatePageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryCreatePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
