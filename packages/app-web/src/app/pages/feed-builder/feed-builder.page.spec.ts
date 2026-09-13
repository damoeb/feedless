import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FeedBuilderPage } from './feed-builder.page';
import { AppTestModule, mockRepositories, mocks } from '../../app-test.module';
import { GqlSourceInput, GqlVisibility } from '../../../generated/graphql';
import { ServerConfigService } from '../../services/server-config.service';

describe('FeedBuilderPage', () => {
  let component: FeedBuilderPage;
  let fixture: ComponentFixture<FeedBuilderPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FeedBuilderPage,
        AppTestModule.withDefaults({
          configurer: (apolloMockController) => mockRepositories(apolloMockController),
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(FeedBuilderPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('handleRepository', () => {
    async function remixedFeedUrlOf(visibility: GqlVisibility, shareKey: string): Promise<string> {
      const handleSource = jest
        .spyOn(component as any, 'handleSource')
        .mockResolvedValue(undefined);
      await component.handleRepository({
        ...mocks.repository,
        id: 'repo-1',
        visibility,
        shareKey,
      } as any);
      const source = handleSource.mock.calls[0][2] as GqlSourceInput;
      return source.flow.sequence[0].fetch.get.url.literal;
    }

    it('adds the share key of a private repository', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await remixedFeedUrlOf(GqlVisibility.IsPrivate, 'key-1')).toEqual(
        `${apiUrl}/f/repo-1/atom?skey=key-1`
      );
    });

    it('leaves the share key out for a public repository', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await remixedFeedUrlOf(GqlVisibility.IsPublic, 'key-1')).toEqual(
        `${apiUrl}/f/repo-1/atom`
      );
    });

    it('leaves the share key out when the repository has none', async () => {
      const apiUrl = TestBed.inject(ServerConfigService).apiUrl;
      expect(await remixedFeedUrlOf(GqlVisibility.IsPrivate, '')).toEqual(
        `${apiUrl}/f/repo-1/atom`
      );
    });
  });
});
