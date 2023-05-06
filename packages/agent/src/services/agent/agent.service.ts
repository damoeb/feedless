import { Injectable, Logger } from '@nestjs/common';
import { GqlAgentEvent } from '../../generated/graphql';
import { PuppeteerService } from '../puppeteer/puppeteer.service';
import { PuppeteerWaitUntil } from '../puppeteer/puppeteer.controller';
import { GraphqlClient } from '../../graphql-client';

@Injectable()
export class AgentService {
  private readonly log = new Logger(AgentService.name);

  constructor(private readonly puppeteerService: PuppeteerService) {
    this.init();
  }

  private init() {
    const graphqlClient = new GraphqlClient();
    graphqlClient.authenticateAgent().subscribe(
      async (event: GqlAgentEvent) => {
        if (event.harvestRequest) {
          this.log.log(
            `[${event.harvestRequest.corrId}] harvestRequest ${JSON.stringify(
              event,
            )}`,
          );
          try {
            const response = await this.puppeteerService.submit({
              corrId: event.harvestRequest.corrId,
              options: {
                prerenderWaitUntil: PuppeteerWaitUntil.load,
                prerenderScript: event.harvestRequest.prerenderScript,
                emit: event.harvestRequest.emit,
                baseXpath: event.harvestRequest.baseXpath,
              },
              url: event.harvestRequest.websiteUrl,
              timeoutMillis: 10000,
            });

            await graphqlClient.submitJobResponse({
              corrId: event.harvestRequest.corrId,
              jobId: event.harvestRequest.id,
              harvestResponse: {
                url: response.effectiveUrl,
                dataBase64: response.dataBase64,
                dataAscii: response.dataAscii,
                errorMessage: response.errorMessage,
                isError: response.isError,
              },
            });
          } catch (e) {
            this.log.error(`[${event.harvestRequest.corrId}] ${e?.message}`);
          }
        }
      },
      (error) => this.log.error(error),
    );
  }
}
