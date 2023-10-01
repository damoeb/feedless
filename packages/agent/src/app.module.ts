import { Module } from '@nestjs/common';
import { AppService } from './app.service';
import { PuppeteerModule } from './services/puppeteer/puppeteer.module';
import { AppController } from './app.controller';
import { AgentModule } from './services/agent/agent.module';
import { CommonModule } from './services/common/common.module';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [
    PuppeteerModule,
    AgentModule,
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
