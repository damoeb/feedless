import { Module } from '@nestjs/common';
import { StatsService } from './stats.service';
import { CommonModule } from '../common/common.module';

@Module({
  providers: [StatsService],
  exports: [StatsService],
  imports: [CommonModule],
})
export class StatsModule {}
