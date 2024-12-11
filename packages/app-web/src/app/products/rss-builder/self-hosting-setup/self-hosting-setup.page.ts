import {
  ChangeDetectionStrategy,
  Component,
  CUSTOM_ELEMENTS_SCHEMA,
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
  IonItem,
  IonLabel,
  IonList,
  IonNote,
  IonSearchbar,
  IonToolbar,
} from '@ionic/angular/standalone';
import { DarkModeButtonComponent } from '../../../components/dark-mode-button/dark-mode-button.component';
import { CodeEditorComponent } from '../../../elements/code-editor/code-editor.component';

export type FunctionalityGroup =
  | 'Database'
  | 'Core'
  | 'Plugin'
  | 'Agent'
  | 'Telegram'
  | 'SMTP'
  | 'Auth';
export type Functionality = {
  name: string;
  description?: string;
  disabled?: boolean;
  checked?: boolean;
  group: FunctionalityGroup;
};

@Component({
  selector: 'app-self-hosting-setup',
  templateUrl: './self-hosting-setup.page.html',
  styleUrls: ['./self-hosting-setup.page.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  imports: [
    IonContent,
    IonCard,
    IonCardHeader,
    IonCardContent,
    DarkModeButtonComponent,
    IonButtons,
    IonHeader,
    IonToolbar,
    IonButton,
    IonList,
    IonItem,
    IonCheckbox,
    IonCardSubtitle,
    IonCardTitle,
    IonNote,
    IonSearchbar,
    IonLabel,
    CodeEditorComponent
  ],
  standalone: true,
})
export class SelfHostingSetupPage {
  list: Functionality[] = [
    {
      name: 'Aggregate Feeds',
      description: 'Merge multiple feeds into one',
      group: 'Database',
    },
    {
      name: 'Fulltext Feeds',
      group: 'Database',
    },
    {
      name: 'Web to Feed',
      description: 'HTML markup to a feed-compatible list using XPaths',
      checked: true,
      disabled: true,
      group: 'Core',
    },
    {
      name: 'Web to Change Series Feed',
      description:
        'Website fragment into into a series of versions with custom change-trigger rules',
      group: 'Core',
    },
    {
      name: 'CSV to Feed',
      // description: 'CSV to a feed',
      group: 'Core',
    },
    {
      name: 'ICS to Feed',
      description: 'ICS (Internet Calendar Scheduling) to a feed',
      group: 'Core',
    },
    {
      name: 'Filters Items',
      description: 'Include or exclude feed items based on filters',
      group: 'Plugin',
    },
    {
      name: 'Aggregate Items',
      description: 'Merge multiple feed items into one (Digest)',
      group: 'Plugin',
    },
    // {
    //   name: 'API',
    //   group: 'Core'
    // },
    {
      name: 'Visual',
      group: 'Agent',
    },
    {
      name: 'Website Click Flows',
      group: 'Agent'
    },
    {
      name: 'JavaScript Support',
      group: 'Agent',
    },
    {
      name: 'Push Notification',
      group: 'Telegram',
    },
    {
      name: 'Feed to Email',
      group: 'SMTP',
    },
    // {
    //   name: 'Newsletter to Feed',
    //   group: 'SMTP',
    // },
    {
      name: 'OAuth2 Authentication',
      group: 'Auth',
    },
  ];

  constructor() {}
}
