import { Component, OnInit, ViewChild } from '@angular/core';
import { WizardService } from '../../services/wizard.service';
import { GqlPuppeteerWaitUntil } from '../../../generated/graphql';
import { PageHeaderComponent } from '../../components/page-header/page-header.component';

export const isUrl = (value: string): boolean => {
  if (!value || value.length < 3) {
    return false;
  }
  if (value.startsWith('http://') || value.startsWith('https://')) {
    try {
      new URL(value);

      const urlPattern =
        /[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)?/gi;
      return !!value.match(new RegExp(urlPattern));
    } catch (e) {
      return false;
    }
  } else {
    return isUrl(`https://${value}`);
  }
};

export const fixUrl = (value: string): string => {
  if (value.startsWith('http://') || value.startsWith('https://')) {
    return value;
  } else {
    return `https://${value}`;
  }
};

@Component({
  selector: 'app-getting-started',
  templateUrl: './getting-started.page.html',
  styleUrls: ['./getting-started.page.scss'],
})
export class GettingStartedPage implements OnInit {
  @ViewChild('headerComponent')
  headerComponent: PageHeaderComponent;

  constructor(private readonly wizardService: WizardService) {}

  ngOnInit() {}

  async openWizard(url: string) {
    if (isUrl(url)) {
      await this.wizardService.openFeedWizard({
        fetchOptions: {
          websiteUrl: fixUrl(url),
          prerender: false,
          prerenderWaitUntil: GqlPuppeteerWaitUntil.Load,
          prerenderWithoutMedia: false,
          prerenderScript: '',
        },
      });
      this.headerComponent.refresh();
    }
  }
}
