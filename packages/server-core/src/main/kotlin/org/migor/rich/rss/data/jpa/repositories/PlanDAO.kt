package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.PlanEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PlanDAO : JpaRepository<PlanEntity, UUID>
