import { Module } from '@nestjs/common';
import { PrefixLoggerService } from './prefix-logger.service';
import { VerboseConfigService } from './verbose-config.service';

@Module({
  providers: [PrefixLoggerService, VerboseConfigService],
  exports: [PrefixLoggerService, VerboseConfigService],
  imports: [],
})
export class CommonModule {}
