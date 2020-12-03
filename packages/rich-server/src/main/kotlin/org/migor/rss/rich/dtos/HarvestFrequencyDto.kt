package org.migor.rss.rich.dtos

import java.util.concurrent.TimeUnit

data class HarvestFrequencyDto(var uuid: String?, var timeUnit: TimeUnit?, var intervalValue: Int?)
