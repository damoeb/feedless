import { GraphqlClient } from './graphql-client';

const client = new GraphqlClient();

const token = ''
client
  .token(token)
  .articles({
    stream: {
      bucket: {
        archive: true,
      },
    },
  })
  .subscribe((article) => process.stdout.write(`${JSON.stringify(article)}\n`));
process.on('SIGPIPE', process.exit);
