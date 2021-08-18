import { Injectable } from '@nestjs/common';
import { EventHook } from '.prisma/client';
// import * as Sandbox from 'v8-sandbox';
import { NativeFeedRef } from 'src/modules/typegraphql/feeds';
import { PrismaService } from '../../modules/prisma/prisma.service';

@Injectable()
export class CustomFeedResolverService {
  constructor(private readonly prisma: PrismaService) {}
  async applyCustomResolvers(
    email: string,
    url: string,
    body: any,
  ): Promise<NativeFeedRef[]> {
    const user = await this.prisma.user.findUnique({
      where: {
        email,
      },
      select: {
        eventHooks: true,
      },
    });

    return Promise.all(
      (user.eventHooks || []).map(async (eventHook: EventHook) => {
        if (!this.isUrl(eventHook.scriptOrUrl)) {
          // see https://www.npmjs.com/package/v8-sandbox
          // const sandbox = new Sandbox({});
          //
          // const { error, value } = await sandbox.execute({
          //   code: eventHook.scriptOrUrl,
          //   timeout: 2000,
          //   context: { url, body },
          // });
          //
          // await sandbox.shutdown();
          //
          // console.log(value);
          // if (error) {
          //   console.error(error);
          // }
          return null;
        }
      }),
    );
  }

  isUrl(scriptOrUrl: string) {
    try {
      new URL(scriptOrUrl);
    } catch (e) {
      return false;
    }
    return true;
  }
}
