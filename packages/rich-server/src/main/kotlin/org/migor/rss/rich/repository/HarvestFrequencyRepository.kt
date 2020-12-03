package org.migor.rss.rich.repository

import org.migor.rss.rich.model.HarvestFrequency
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HarvestFrequencyRepository: CrudRepository<HarvestFrequency, String>
