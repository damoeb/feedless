import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
  viewChild,
} from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonSpinner,
  IonToolbar,
} from '@ionic/angular/standalone';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DarkModeButtonComponent } from '../../../components/dark-mode-button/dark-mode-button.component';
import { ProfileButtonComponent } from '../../../components/profile-button/profile-button.component';
import { MapComponent } from '../../../components/map/map.component';
import { LatLng, Nullable } from '../../../types';
import { SourcesComponent } from '../../../components/sources/sources.component';
import { RepositoryService, Source } from '../../../services/repository.service';
import { AppConfigService } from '../../../services/app-config.service';
import { RepositoryFull } from '../../../graphql/types';
import { GqlSourcesWhereInput } from '../../../../generated/graphql';
import { marker } from 'leaflet';

@Component({
  selector: 'app-management-page',
  templateUrl: './management.page.html',
  styleUrls: ['./management.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonHeader,
    IonContent,
    RouterLink,
    IonToolbar,
    IonButton,
    IonButtons,
    DarkModeButtonComponent,
    ProfileButtonComponent,
    MapComponent,
    SourcesComponent,
    IonSpinner,
  ],
  standalone: true,
})
export class ManagementPage implements OnInit {
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
  protected showSources: boolean;

  constructor() {}

  async ngOnInit() {
    this.repository = await this.repositoryService.getRepositoryById(
      this.getRepositoryId(),
      { page: 0 },
      null
    );
    this.showSources = this.activatedRoute.snapshot.data.sources == true;
    this.changeRef.detectChanges();
  }

  private getRepositoryId(): string {
    return this.appConfigService.customProperties.eventRepositoryId as any;
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

  onSourceChange(sources: Source[]) {
    const map = this.map().getNativeMap();
    sources.map((s) => marker(s.latLng).addTo(map));
  }
}
