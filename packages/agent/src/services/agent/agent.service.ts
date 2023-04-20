import { Injectable, Logger } from '@nestjs/common';
import { GqlAgentEvent } from '../../generated/graphql';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { PuppeteerWaitUntil } from '../puppeteer/puppeteer.controller';
import { GraphqlClient } from '../../graphql-client';

@Injectable()
export class AgentService {
  private readonly log = new Logger(AgentService.name);

  constructor(private readonly puppeteerService: PuppeteerService) {
    const graphqlClient = new GraphqlClient();
    graphqlClient.authenticateAgent().subscribe(
      async (event: GqlAgentEvent) => {
        if (event.harvestRequest) {
          try {
            const response = await this.puppeteerService.submit({
              corrId: event.harvestRequest.corrId,
              options: {
                prerenderWaitUntil: PuppeteerWaitUntil.load,
                prerenderScript: event.harvestRequest.prerenderScript,
                prerenderWithoutMedia: false,
              },
              url: event.harvestRequest.websiteUrl,
              timeoutMillis: 10000,
            });

            await graphqlClient.submitJobResponse({
              corrId: event.harvestRequest.corrId,
              jobId: event.harvestRequest.id,
              harvestResponse: {
                url: response.effectiveUrl,
                html: response.html,
                errorMessage: response.errorMessage,
                isError: response.isError,
              },
            });
          } catch (e) {
            this.log.error(e);
          }
        }
      },
      (error) => this.log.error(error),
    );
  }
}
