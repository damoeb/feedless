package org.migor.feedless.config

import org.flywaydb.core.api.callback.Callback
import org.flywaydb.core.api.callback.Context
import org.flywaydb.core.api.callback.Event
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


//@Configuration
//class FlywayFactory {
//  @Bean
//  fun flywayConfigurationCustomizer(): FlywayConfigurationCustomizer? {
//    return FlywayConfigurationCustomizer { configuration: FluentConfiguration ->
//      configuration.callbacks(
//        FlywayMigrationCallback()
//      )
//    }
//  }
//}
//
//class FlywayMigrationCallback : Callback {
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
//    log.info("${event.name} ${context.migrationInfo}")
//  }
//
//  override fun getCallbackName(): String {
//    return FlywayMigrationCallback::class.java.simpleName
//  }
//
//}
