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
