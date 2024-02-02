import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RepositoryPluginsPage } from './repository-plugins.page';
import { RepositoryPluginsPageModule } from './repository-plugins.module';
import { RouterTestingModule } from '@angular/router/testing';
import { AppTestModule } from '../../../../app-test.module';

describe('RepositoryPluginsPage', () => {
  let component: RepositoryPluginsPage;
  let fixture: ComponentFixture<RepositoryPluginsPage>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [
        RepositoryPluginsPageModule,
        AppTestModule.withDefaults(),
        RouterTestingModule.withRoutes([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RepositoryPluginsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
