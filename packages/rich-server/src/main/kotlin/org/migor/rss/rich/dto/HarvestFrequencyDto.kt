package org.migor.rss.rich.dto

import java.util.concurrent.TimeUnit

data class HarvestFrequencyDto(var id: String?, var timeUnit: TimeUnit?, var intervalValue: Int?)
