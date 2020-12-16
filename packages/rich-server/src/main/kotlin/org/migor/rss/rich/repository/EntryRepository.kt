package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Entry
import org.migor.rss.rich.model.EntryStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface EntryRepository: PagingAndSortingRepository<Entry, String> {
  fun findAllBySubscriptionId(subscriptionId: String, pageable: Pageable): Page<Entry>
  fun findAllBySubscriptionIdAndStatusEquals(subscriptionId: String, status: EntryStatus, pageable: Pageable): List<Entry>
  fun findByLink(link: String?): Optional<Entry>

  @Transactional
  @Modifying
  @Query("update Entry e set e.status = :status where e.id = :id")
  fun updateStatus(@Param("id") entryId: String, @Param("status") status: EntryStatus)
}
