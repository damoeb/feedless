import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { RepositoriesDirectoryComponent } from './repositories-directory.component';
import {
  ApolloMockController,
  AppTestModule,
  mockRepositories,
  mockServerSettings,
} from '../../app-test.module';
import { RepositoriesDirectoryModule } from './repositories-directory.module';
import { ServerConfigService } from '../../services/server-config.service';
import { ApolloClient } from '@apollo/client/core';

describe('RepositoriesDirectoryComponent', () => {
  let component: RepositoriesDirectoryComponent;
  let fixture: ComponentFixture<RepositoriesDirectoryComponent>;

  beforeEach(waitForAsync(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RepositoriesDirectoryModule,
        AppTestModule.withDefaults((apolloMockController) => {
          mockRepositories(apolloMockController);
        }),
      ],
    }).compileComponents();

    await mockServerSettings(
      TestBed.inject(ApolloMockController),
      TestBed.inject(ServerConfigService),
      TestBed.inject(ApolloClient),
    );

    fixture = TestBed.createComponent(RepositoriesDirectoryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
