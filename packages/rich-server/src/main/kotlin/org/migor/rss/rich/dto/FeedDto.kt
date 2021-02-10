package org.migor.rss.rich.dto

import java.util.*

data class FeedDto(val id: String?, val title: String?, val link: String?, val description: String?, val createdAt: Date, val pubDate: Date?, val language: String?, val copyright: String?, val entries: List<EntryDto?>?, val subscriptionId: String?)
