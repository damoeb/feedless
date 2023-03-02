package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class ScoreService {

}
