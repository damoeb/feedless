import { FeedDiscoveryResult, FeedService } from '../../services/feed.service';
import { ChangeDetectorRef } from '@angular/core';
import { WizardContext, WizardStepId } from './wizard/wizard.component';

export class WizardHandler {
  private discovery: FeedDiscoveryResult;

  constructor(
    private context: WizardContext,
    private readonly feedService: FeedService,
    private readonly changeRef: ChangeDetectorRef
  ) {}

  getContext(): WizardContext {
    return this.context;
  }

  async updateContext(update: Partial<WizardContext>) {
    this.context = {
      ...this.context,
      ...update,
    };
    if (update.url) {
      await this.fetchDiscovery();
    }
    this.changeRef.detectChanges();
  }

  hasMimeType(mime: string): boolean {
    const discovery = this.getDiscovery();
    return (
      discovery &&
      !discovery.failed &&
      discovery.document.mimeType.startsWith(mime)
    );
  }

  getDiscovery(): FeedDiscoveryResult {
    return this.discovery;
  }

  hasEmptyHistory(): boolean {
    return this.context.history.length === 0;
  }

  getCurrentStepId(): WizardStepId {
    return this.context.currentStepId;
  }

  private async fetchDiscovery() {
    this.discovery = await this.feedService.discoverFeeds({
      fetchOptions: {
        websiteUrl: this.context.url,
        prerender: this.context.prerender,
        prerenderScript: this.context.prerenderScript,
        prerenderWaitUntil: this.context.prerenderWaitUntil,
        prerenderWithoutMedia: false,
      },
      parserOptions: {
        strictMode: false,
      },
    });
  }
}
