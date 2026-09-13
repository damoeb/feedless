import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedDetailsPage } from './feed-details.page';
import { AppTestModule, mockPlugins, mockRepository, mocks } from '../../app-test.module';
import { GqlVisibility } from '../../../generated/graphql';
import { RepositoryService } from '../../services/repository.service';
import { ServerConfigService } from '../../services/server-config.service';

describe('FeedDetailsPage', () => {
  let component: FeedDetailsPage;
  let fixture: ComponentFixture<FeedDetailsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedDetailsPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => {
            mockPlugins(apolloMockController);
            mockRepository(apolloMockController);
          },
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedDetailsPage);
    component = fixture.componentInstance;
    component.repository = {} as any;
    // component.documents = [];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('feedUrl', () => {
    async function feedUrlOf(visibility: GqlVisibility, shareKey: string): Promise<string> {
      jest
        .spyOn(TestBed.inject(RepositoryService), 'getRepositoryById')
        .mockResolvedValue({ ...mocks.repository, id: 'repo-1', visibility, shareKey } as any);
      // keeps the child feed-details component from rendering against unmocked documents
      jest.spyOn((component as any).changeRef, 'detectChanges').mockImplementation(() => {});
      await (component as any).fetch();
      return component.feedUrl;
    }

    it('adds the share key of a private repository', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await feedUrlOf(GqlVisibility.IsPrivate, 'key-1')).toEqual(
        `${apiUrl}/f/repo-1/atom?skey=key-1`
      );
    });

    it('leaves the share key out for a public repository', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await feedUrlOf(GqlVisibility.IsPublic, 'key-1')).toEqual(`${apiUrl}/f/repo-1/atom`);
    });

    it('leaves the share key out when the repository has none', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await feedUrlOf(GqlVisibility.IsPrivate, '')).toEqual(`${apiUrl}/f/repo-1/atom`);
    });
  });
});
