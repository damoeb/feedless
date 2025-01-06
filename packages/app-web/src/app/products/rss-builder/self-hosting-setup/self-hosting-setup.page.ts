import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnInit,
} from '@angular/core';
import {
  IonButton,
  IonButtons,
  IonCard,
  IonCardContent,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonCheckbox,
  IonContent,
  IonHeader,
  IonIcon,
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonSearchbar,
  IonTitle,
  IonToolbar,
  ModalController,
} from '@ionic/angular/standalone';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { map, merge } from 'rxjs';
import { sortBy, values } from 'lodash-es';
import { addIcons } from 'ionicons';
import { closeOutline } from 'ionicons/icons';

export type Functionality = {
  name: string;
  description?: string;
  groups: FeatureGroup[];
};

type FeatureGroup = {
  control: FormControl<boolean>;
  name: string;
};

function disabled(c: FormControl<boolean>): FormControl<boolean> {
  c.disable();
  return c;
}

function group(name: string, control: FormControl<boolean>): FeatureGroup {
  return {
    name,
    control,
  };
}

@Component({
  selector: 'app-self-hosting-setup',
  templateUrl: './self-hosting-setup.page.html',
  styleUrls: ['./self-hosting-setup.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardContent,
    IonList,
    IonItem,
    IonCheckbox,
    IonCardSubtitle,
    IonCardTitle,
    IonNote,
    IonSearchbar,
    IonLabel,
    ReactiveFormsModule,
    IonToolbar,
    IonHeader,
    IonTitle,
    IonButtons,
    IonButton,
    IonIcon,
  ],
  standalone: true,
})
export class SelfHostingSetupPage implements OnInit {
  private readonly modalCtrl = inject(ModalController);
  queryFc = new FormControl<string>('');

  private readonly groups = {
    database: group('Database', new FormControl(false)),
    api: group('API', new FormControl(false)),
    core: group('Core', disabled(new FormControl(true))),
    agent: group('Agent', new FormControl(false)),
    plugin: group('Plugin', new FormControl(false)),
    telegram: group('Push', new FormControl(false)),
    mail: group('Mail', new FormControl(false)),
    oauth: group('oauth', new FormControl(false)),
  };

  private readonly features: Functionality[] = [
    {
      name: 'Aggregate Feeds',
      description: 'Merge multiple feeds into one',
      groups: [this.groups.database],
    },
    {
      name: 'Fulltext Feeds',
      groups: [this.groups.database],
    },
    {
      name: 'Digest',
      description: 'Aggregate multiple items into one',
      groups: [this.groups.database],
    },
    {
      name: 'Web to Feed',
      description: 'HTML markup to a feed-compatible list using XPaths',
      groups: [this.groups.core],
    },
    {
      name: 'Web to Change Series Feed',
      description:
        'Website fragment into into a series of versions with custom change-trigger rules',
      groups: [this.groups.agent],
    },
    {
      name: 'CSV to Feed',
      // description: 'CSV to a feed',
      groups: [this.groups.core],
    },
    {
      name: 'ICS to Feed',
      description: 'ICS (Internet Calendar Scheduling) to a feed',
      groups: [this.groups.core],
    },
    {
      name: 'Filters Items',
      description: 'Include or exclude feed items based on filters',
      groups: [this.groups.plugin],
    },
    {
      name: 'Aggregate Items',
      description: 'Merge multiple feed items into one (Digest)',
      groups: [this.groups.plugin],
    },
    {
      name: 'API',
      description: '',
      groups: [this.groups.api],
    },
    {
      name: 'Visual',
      description: '',
      groups: [this.groups.agent],
    },
    {
      name: 'Website Click Flows',
      groups: [this.groups.agent],
    },
    {
      name: 'JavaScript Support',
      groups: [this.groups.agent],
    },
    {
      name: 'Push Notification',
      groups: [this.groups.telegram, this.groups.database],
    },
    {
      name: 'Feed to Email',
      groups: [this.groups.mail],
    },
    {
      name: 'OAuth2 Authentication',
      groups: [this.groups.oauth],
    },
  ];

  protected filteredFeatures: Functionality[] = sortBy(this.features, 'name');

  constructor(private changeRef: ChangeDetectorRef) {
    addIcons({
      closeOutline,
    });
  }

  ngOnInit(): void {
    merge(
      ...values(this.groups).map((group) => group.control.valueChanges),
    ).subscribe(() => this.changeRef.detectChanges());

    this.queryFc.valueChanges
      .pipe(map((q) => q.trim().toLowerCase()))
      .subscribe((query) => {
        if (query) {
          this.filteredFeatures = sortBy(
            this.features.filter((feature) => {
              return (
                feature.name.toLowerCase().indexOf(query) > -1 ||
                (feature.description || '').toLowerCase().indexOf(query) > -1
              );
            }),
            'name',
          );
        } else {
          this.filteredFeatures = sortBy(this.features, 'name');
        }
        this.changeRef.detectChanges();
      });
  }

  activeGroups(): FeatureGroup[] {
    return sortBy(
      values(this.groups).filter((group) => group.control.value),
      'name',
    );
  }

  toggle(f: Functionality) {
    // f.groups.forEach(g => g.)
  }

  value(f: Functionality): boolean {
    return f.groups.every((g) => g.control.value);
  }

  isDisabled(f: Functionality): boolean {
    return f.groups.every((g) => g.control.disabled);
  }

  getGroups(f: Functionality): string {
    return f.groups.map((it) => it.name).join(', ');
  }

  closeModal() {
    return this.modalCtrl.dismiss([]);
  }
}
