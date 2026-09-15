import { ComponentFixture, TestBed } from '@angular/core/testing';
import type { MockInstance } from 'vitest';

import { SourcesComponent } from './sources.component';
import { AppTestModule } from '@feedless/testing';
import { ModalProvider } from '../../modals';
import { RepositoryService } from '../../services';
import { RepositorySource } from '@feedless/graphql-api';

describe('SourcesComponent', () => {
  let component: SourcesComponent;
  let fixture: ComponentFixture<SourcesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SourcesComponent, AppTestModule.withDefaults()],
      providers: [
        {
          provide: ModalProvider,
          useValue: { openFeedBuilder: vi.fn().mockResolvedValue(undefined) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SourcesComponent);
    component = fixture.componentInstance;
    const componentRef = fixture.componentRef;
    componentRef.setInput('repository', { sources: [] });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('next harvest', () => {
    it('shows when it is scheduled', () => {
      fixture.componentRef.setInput('repository', {
        sources: [],
        nextUpdateAt: Date.now() + 5 * 60_000,
      });
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        'Next harvest in 5 minutes',
      );
    });

    it('shows the rescheduled time after a sync', async () => {
      const repositoryService = TestBed.inject(RepositoryService);
      vi.spyOn(repositoryService, 'forceSourceSync').mockResolvedValue(
        Date.now() + 10 * 60_000,
      );

      await component.forceSync();
      fixture.detectChanges();

      expect(fixture.nativeElement.textContent).toContain(
        'Next harvest in 10 minutes',
      );
    });
  });

  describe('last harvest', () => {
    async function renderSource(source: object) {
      const repositoryService = TestBed.inject(RepositoryService);
      vi.spyOn(repositoryService, 'getSourcesByRepository').mockResolvedValue([
        { id: 's1', title: 'Source', lastRefreshedAt: Date.now(), ...source },
      ] as any);

      await component.fetchSources(0);
      fixture.detectChanges();
      return fixture.nativeElement.textContent as string;
    }

    it('names the column after the harvest', () => {
      expect(fixture.nativeElement.textContent).toContain('Last Harvest');
    });

    it('shows found and new items', async () => {
      const actual = await renderSource({
        lastRecordsRetrieved: 12,
        harvests: [{ itemsAdded: 3, finishedAt: Date.now() }],
      });

      expect(actual).toContain('12 found');
      expect(actual).toContain('3 new');
    });

    it('omits new items while the harvest is still running', async () => {
      const actual = await renderSource({
        lastRecordsRetrieved: 12,
        harvests: [{ itemsAdded: 0, finishedAt: null }],
      });

      expect(actual).toContain('12 found');
      expect(actual).not.toContain('new');
    });
  });

  describe('feed-builder-modal is openened', () => {
    let openFeedBuilderSpy: MockInstance;

    beforeEach(() => {
      const repositoryService = TestBed.inject(RepositoryService);
      vi
        .spyOn(repositoryService, 'getSourceFullByRepository')
        .mockResolvedValue({} as any);
      const modalProvider = TestBed.inject(ModalProvider);
      openFeedBuilderSpy = vi
        .spyOn(modalProvider, 'openFeedBuilder')
        .mockResolvedValue();
    });

    it('for add source', async () => {
      await component.editOrAddSource();

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });

    it('for edit source', async () => {
      const source: RepositorySource = { id: '' } as any;
      await component.editOrAddSource(source);

      expect(openFeedBuilderSpy).toHaveBeenCalled();
    });
  });
});
