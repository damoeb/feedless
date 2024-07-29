import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ServerConfigService } from '../../../services/server-config.service';
import { Subscription } from 'rxjs';
import { RepositoryService } from '../../../services/repository.service';
import { RepositoryFull, WebDocument } from '../../../graphql/types';
import { DocumentService } from '../../../services/document.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-feed-details-page',
  templateUrl: './feed-details.page.html',
  styleUrls: ['./feed-details.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FeedDetailsPage implements OnInit, OnDestroy {
  private subscriptions: Subscription[] = [];
  items: WebDocument[];
  repository: RepositoryFull;

  constructor(
    private readonly activatedRoute: ActivatedRoute,
    private readonly repositoryService: RepositoryService,
    private readonly documentService: DocumentService,
    private readonly router: Router,
    private readonly titleService: Title,
    private readonly changeRef: ChangeDetectorRef,
    readonly serverConfig: ServerConfigService,
  ) {}

  ngOnInit() {
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(async (params) => {
        if (params.id) {
          this.repository = await this.repositoryService.getRepositoryById(
            params.id,
          );
          this.titleService.setTitle(this.repository.title);
          this.changeRef.detectChanges();

          this.items = await this.documentService.findAllByRepositoryId({
            cursor: {
              page: 0,
              pageSize: 20,
            },
            where: {
              repository: {
                id: params.id,
              },
            },
          });
          this.changeRef.detectChanges();
        } else {
          console.error('Param id not provided');
          await this.router.navigateByUrl('/');
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach((s) => s.unsubscribe());
  }
}
