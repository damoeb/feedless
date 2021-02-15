package org.migor.rss.rich.dto

import java.util.*

data class UserDto(var id: String?, var emailHash: String?, var name: String?, var description: String?, var createdAt: Date, var feeds: List<FeedDto>)
