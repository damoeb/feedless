import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';
import { AppController } from './app.controller';
import { SocketSubscriptionModule } from './services/socket-subscription/socket-subscription.module';
import { CommonModule } from './services/common/common.module';
import { ConfigModule } from '@nestjs/config';

export interface AgentScope {
  restrictions: {
    // owner: boolean
    // group: boolean
    others: boolean;
  };
  secrets: {
    // provide secrets k/v, cookies
  };
}

@Module({
  imports: [
    PuppeteerModule,
    SocketSubscriptionModule,
    CommonModule,
    ConfigModule.forRoot({
      isGlobal: true,
      ignoreEnvFile: true,
    }),
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
