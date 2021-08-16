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
import { ArticleController } from './modules/controller/article/article.controller';
import { ReadabilityService } from './services/readability/readability.service';
import { OpmlService } from './services/opml/opml.service';
import { OpmlModule } from './services/opml/opml.module';
import { RssProxyModule } from './services/rss-proxy/rss-proxy.module';
import { AuthModule } from './services/auth/auth.module';
import { AuthService } from './services/auth/auth.service';
import { ConfigModule } from '@nestjs/config';
import { Auth } from './modules/typegraphql/auth';

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
          playground: true,
          introspection: true,
          cors: {
            origin: 'http://localhost:3001',
            credentials: true,
          },
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
    OpmlModule,
    RssProxyModule,
    AuthModule,
  ],
  providers: [AppService, FeedService, ReadabilityService, OpmlService],
  controllers: [ArticleController],
})
export class AppModule {}
