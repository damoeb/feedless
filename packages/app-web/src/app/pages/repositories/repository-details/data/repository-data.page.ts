import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FetchPolicy } from '@apollo/client/core';
import { dateTimeFormat } from '../../../../services/session.service';
import { Subscription } from 'rxjs';
import { WebDocument } from '../../../../graphql/types';
import { DocumentService } from '../../../../services/document.service';

@Component({
  selector: 'app-repository-data-page',
  templateUrl: './repository-data.page.html',
  styleUrls: ['./repository-data.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RepositoryDataPage implements OnInit, OnDestroy {
  loadingSource: boolean;
  repositoryId: string;
  isLast: boolean;
  entities: WebDocument[] = [];

  private subscriptions: Subscription[] = [];
  private currentPage = 0;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly webDocumentService: DocumentService,
    private readonly changeRef: ChangeDetectorRef,
  ) {}

  async ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe((params) => {
        this.repositoryId = params.repositoryId;
        this.fetch();
      }),
    );
  }

  async fetch(page: number = 0, fetchPolicy: FetchPolicy = 'cache-first') {
    const entities = await this.webDocumentService.findAllByStreamId(
      {
        cursor: {
          page,
        },
        where: {
          repository: {
            where: {
              id: this.repositoryId,
            },
          },
        },
      },
      fetchPolicy,
    );

    this.isLast = entities.length < 10;
    this.entities.push(...entities);
    this.changeRef.detectChanges();
  }

  async nextPage(event: any) {
    this.currentPage += 1;
    await this.fetch(this.currentPage);
    await event.target.complete();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }

  protected readonly dateTimeFormat = dateTimeFormat;
}
