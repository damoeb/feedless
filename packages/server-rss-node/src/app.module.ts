import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PrismaModule } from './modules/prisma/prisma.module';
import { GqlModuleOptions, GraphQLModule } from '@nestjs/graphql';
import { applyMiddleware } from 'graphql-middleware';
import { buildSchema } from 'type-graphql';
import { resolvers } from '@generated/type-graphql';
import { IncomingMessage, ServerResponse } from 'http';
import { PrismaService } from './modules/prisma/prisma.service';
import { Feeds } from './modules/typegraphql/feeds';
import { FeedService } from './services/feed/feed.service';
import { FeedModule } from './services/feed/feed.module';
import { RssProxyModule } from './services/rss-proxy/rss-proxy.module';
import { AuthModule } from './services/auth/auth.module';
import { AuthService } from './services/auth/auth.service';
import { ConfigModule } from '@nestjs/config';
import { Auth } from './modules/typegraphql/auth';
import { PluginModule } from './services/plugin/plugin.module';
import { MessageBrokerModule } from './services/message-broker/message-broker.module';
import { ReadabilityModule } from './services/readability/readability.module';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';

@Module({
  imports: [
    PrismaModule,
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    GraphQLModule.forRootAsync({
      imports: [PrismaModule, FeedModule, AuthModule],
      inject: [PrismaService, FeedService, AuthService],
      useFactory: async (
        prismaService: PrismaService,
        feedService: FeedService,
        authService: AuthService,
      ) => {
        const options: GqlModuleOptions = {
          schema: applyMiddleware<IncomingMessage, ServerResponse, any>(
            await buildSchema({
              resolvers: [...resolvers, Feeds, Auth],
            }),
          ),
          path: 'graphql',
          introspection: true,
          cors: {
            origin: 'http://localhost:3001',
            credentials: true,
          },
          // autoSchemaFile: './schema.gql',
          debug: true,
          context: (context: any) => {
            context.prisma = prismaService;
            context.feedService = feedService;
            context.authService = authService;
            return context;
          },
        };
        return options;
      },
    }),
    FeedModule,
    RssProxyModule,
    ReadabilityModule,
    AuthModule,
    PluginModule,
    MessageBrokerModule,
    PuppeteerModule,
  ],
  providers: [AppService],
})
export class AppModule {}
