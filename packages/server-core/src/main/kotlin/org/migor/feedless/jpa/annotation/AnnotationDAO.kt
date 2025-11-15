package org.migor.feedless.jpa.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile("${AppProfiles.annotation} & ${AppLayer.repository}")
interface AnnotationDAO : JpaRepository<AnnotationEntity, UUID>
