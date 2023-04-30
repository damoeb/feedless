import { GraphqlClient } from './graphql-client';

const client = new GraphqlClient();

const token = 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZDE0ZmEwNzctMTUxZi00NDE5LWJjZTAtZThmMmExNDgxYWI4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiaWQiOiJyaWNoIiwiZXhwIjoxNzEzNDMwOTkyLCJ0b2tlbl90eXBlIjoiQVBJIiwiaWF0IjoxNjgyNjcyNTkyLCJhdXRob3JpdGllcyI6WyJSRUFEIl19.5bBM5OZCWONRrgBi_4dfcSE1fP2ZWp14sEOygVEgzbI'
client
  .token(token)
  .articles({
    stream: {
      bucket: {
        tags: {
          some: ['kinder']
        }
      },
    },
  })
  .subscribe((article) => process.stdout.write(`${JSON.stringify(article)}\n`));
process.on('SIGPIPE', process.exit);
