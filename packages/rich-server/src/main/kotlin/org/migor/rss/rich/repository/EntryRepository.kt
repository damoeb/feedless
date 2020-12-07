package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Entry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface EntryRepository: PagingAndSortingRepository<Entry, String> {
  fun existsBySubscriptionIdAndLink(subscriptionId: String, link: String): Boolean
  fun findAllBySubscriptionId(subscriptionId: String, pageable: Pageable): Page<Entry>
}
