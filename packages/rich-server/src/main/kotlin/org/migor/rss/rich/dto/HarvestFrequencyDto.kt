package org.migor.rss.rich.dto

import java.time.temporal.ChronoUnit

data class HarvestFrequencyDto(var id: String, var timeUnit: ChronoUnit, var intervalValue: Long)
