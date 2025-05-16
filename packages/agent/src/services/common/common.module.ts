import { Module } from '@nestjs/common';
import { VerboseConfigService } from './verbose-config.service';

@Module({
  providers: [VerboseConfigService],
  exports: [VerboseConfigService],
  imports: [],
})
export class CommonModule {}
