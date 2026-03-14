import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
  viewChild,
} from '@angular/core';
import { IonContent, IonSpinner } from '@ionic/angular/standalone';
import { ActivatedRoute } from '@angular/router';
// eslint-disable-next-line @nx/enforce-module-boundaries
import {
  AppConfigService,
  MapComponent,
  RepositoryService,
  Source,
  SourcesComponent,
} from '@feedless/components';
import { LatLng, Nullable } from '@feedless/core';
import { GqlSourcesWhereInput, RepositoryFull } from '@feedless/graphql-api';

@Component({
  selector: 'app-event-sources-page',
  templateUrl: './event-sources.page.html',
  styleUrls: ['./event-sources.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [IonContent, SourcesComponent, IonSpinner],
  standalone: true,
})
export class EventSourcesPage implements OnInit {
  readonly map = viewChild.required<MapComponent>('map');

  private readonly repositoryService = inject(RepositoryService);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly appConfigService = inject(AppConfigService);
  private readonly changeRef = inject(ChangeDetectorRef);

  mapPosition: LatLng = {
    lat: 47.3744489,
    lng: 8.5410422,
  };
  protected repository: RepositoryFull;
  sourcesFilter: Nullable<GqlSourcesWhereInput> = null;

  async ngOnInit() {
    this.repository = await this.repositoryService.getRepositoryById(
      this.getRepositoryId(),
      { page: 0 },
      null,
    );
    this.changeRef.detectChanges();
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties['eventRepositoryId'] as any;
  }

  onMapChange(event: { northEast: LatLng; southWest: LatLng }) {
    this.sourcesFilter = {
      latLng: {
        near: {
          point: {
            lat: (event.northEast.lat + event.southWest.lat) / 2,
            lng: (event.northEast.lng + event.northEast.lng) / 2,
          },
          distanceKm: 150,
        },
      },
    };
    this.changeRef.detectChanges();
  }

  async onSourceChange(sources: Source[]) {
    // const L = await import('leaflet');
    // const map = this.map().getNativeMap();
    // sources.map((s) => L.marker(s.latLng).addTo(map));
  }
}
