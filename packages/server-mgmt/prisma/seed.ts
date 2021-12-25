import { PrismaClient, User } from '@prisma/client';
import { PrismaService } from '../src/modules/prisma/prisma.service';
import { HttpService } from '@nestjs/axios';
import { lastValueFrom } from 'rxjs';
import { FeedService } from '../src/services/feed/feed.service';
import { RichJsonService } from '../src/services/rich-json/rich-json.service';
import { sourcesRichJson } from '../resources/sources-rich';

const prisma = new PrismaClient();

async function main() {
  console.log(`Start seeding ...`);
  const httpService = new HttpService();

  await prisma.user.create({
    data: {
      id: 'system',
      name: 'system',
      email: 'sys@tem.ch',
    },
  });

  const user = await lastValueFrom(httpService.put<User>('http://localhost:8080/api/users', {  name: 'Karl May',
        email: 'karl@may.ch',
  })).then(response => response.data)

  const noFollowUrls = ['https://www.paypal.me', 'https://www.patreon.com'];
  await Promise.all(
    noFollowUrls.map((noFollowUrl) =>
      prisma.noFollowUrl.create({
        data: {
          url_prefix: noFollowUrl,
        },
      }),
    ),
  );

  const postProcessors = ['FOLLOW_LINKS'];
  await Promise.all(
    postProcessors.map((type) =>
      prisma.articlePostProcessor.upsert({
        where: {
          type,
        },
        create: {
          type,
        },
        update: {
          type,
        },
      }),
    ),
  );


  const prismaService = new PrismaService();
  // const opmlService = new OpmlService(
  //   prismaService,
  //   new FeedService(prismaService),
  // );
  // const file = 'resources/sources-opml.xml';
  // console.log(`From file ${file}`);
  // const opml = readFileSync(file);
  // await opmlService
  //   .createBucketsFromOpml(
  //     Buffer.from(opml.toString('utf-8'), 'utf-8').toString('base64'),
  //     user,
  //   )
  //   .catch(console.error);

  const richJsonService = new RichJsonService(
    prismaService,
    httpService,
    new FeedService(prismaService, httpService),
  );
  await richJsonService
    .createBucketsFromRichJson(sourcesRichJson, user)
    .catch(console.error);
}

main()
  .then(() => {
    console.log(`Seeding finished.`);
    process.exit(0);
  })
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
