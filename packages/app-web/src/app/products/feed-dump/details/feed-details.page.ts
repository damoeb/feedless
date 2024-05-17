import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ServerSettingsService } from '../../../services/server-settings.service';
import { Subscription } from 'rxjs';
import { RepositoryService } from '../../../services/repository.service';
import { Repository, WebDocument } from '../../../graphql/types';
import { DocumentService } from '../../../services/document.service';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  items: WebDocument[];
  repository: Repository;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly repositoryService: RepositoryService,
    private readonly documentService: DocumentService,
    private readonly router: Router,
    private readonly changeRef: ChangeDetectorRef,
    readonly serverSettings: ServerSettingsService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.id) {
          this.repository = await this.repositoryService.getRepositoryById(params.id)
          this.changeRef.detectChanges();

          this.items = await this.documentService.findAllByStreamId({
            cursor: {
              page: 0,
              pageSize: 20
            },
            where: {
              repository: {
                where: {
                  id: params.id
                }
              }
            }
          });
          this.changeRef.detectChanges();
        } else {
          console.error('Param id not provided');
          await this.router.navigateByUrl('/')
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
