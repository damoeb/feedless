//package org.migor.feedless.config
//
//import org.flywaydb.core.api.callback.Callback
//import org.flywaydb.core.api.callback.Context
//import org.flywaydb.core.api.callback.Event
//import org.flywaydb.core.api.configuration.FluentConfiguration
//import org.migor.feedless.generated.types.CompositeFilterParamsInput
//import org.migor.feedless.generated.types.FeedlessPlugins
//import org.migor.feedless.generated.types.PluginExecutionParamsInput
//import org.migor.feedless.notification.NotificationService
//import org.migor.feedless.pipeline.plugins.FeedPlugin
//import org.migor.feedless.repository.RepositoryDAO
//import org.migor.feedless.repository.RepositoryEntity
//import org.migor.feedless.util.CryptUtil.newCorrId
//import org.migor.feedless.util.JsonUtil
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.Lazy
//
//
//@Configuration
//class FlywayFactory {
//
//  @Autowired
//  @Lazy
//  lateinit var repositoryDAO: RepositoryDAO
//
//  @Autowired
//  lateinit var notificationService: NotificationService
//
//  @Bean
//  fun flywayConfigurationCustomizer(): FlywayConfigurationCustomizer? {
//    return FlywayConfigurationCustomizer { configuration: FluentConfiguration ->
//      configuration.callbacks(
//        FlywayMigrationCallback(repositoryDAO, notificationService)
//      )
//    }
//  }
//}
//
//class FlywayMigrationCallback(val repositoryDAO: RepositoryDAO, val notificationService: NotificationService) : Callback {
//
//  private val log = LoggerFactory.getLogger(FlywayMigrationCallback::class.simpleName)
//
//  override fun supports(event: Event, context: Context): Boolean {
//    return event == Event.AFTER_EACH_MIGRATE
//  }
//
//  override fun canHandleInTransaction(event: Event, context: Context): Boolean {
//    return true
//  }
//
//  override fun handle(event: Event, context: Context) {
//    val corrId= newCorrId()
////    log.info("${event.name} ${context.migrationInfo}")
//    if (context.migrationInfo.version.version == "11") {
//      log.info("[$corrId] Starting filter migration")
//      repositoryDAO.saveAll(      repositoryDAO.findAllByLegacyFiltersIsNotNull()
//        .map { repository -> migrateLegacyFilters(corrId, repository) }
//)    }
//    log.info("Finished Filter migration")
//  }
//
//  private fun migrateLegacyFilters(corrId: String, repository: RepositoryEntity): RepositoryEntity {
//    val filters = JsonUtil.gson.fromJson(repository.legacyFilters, List::class.java)
//
//    if (filters.size > 1) {
//      val message = """
//Dear User,
//
//Please be advised that the latest version of Feedless introduces changes that unfortunately break the filters you have defined for your feed.
//
//$filters
//
//Thank you for your understanding and continued use of Feedless.
//
//Sincerely,""".trimIndent()
//      notificationService.createNotification(corrId, repository.ownerId, message, repository)
//    }
//
//    // add before feed plugin
//    val plugins = repository.plugins.toMutableList()
//    val filter = PluginExecutionParamsInput(
//      org_feedless_filter = listOf(
//        CompositeFilterParamsInput()
//      )
//    )
//    repository.plugins = plugins.add(plugins.indexOfFirst { it.id == FeedlessPlugins.org_feedless_feed.name }, filter)
//
//    return repository
//  }
//
//  override fun getCallbackName(): String {
//    return FlywayMigrationCallback::class.java.simpleName
//  }
//
//}
