package org.migor.feedless.trigger.plugins

enum class PluginPhase(val priority: Int) {
//  detectMedia,
//  fulltext,
  harvest(1),

//  filter,
  purate(2),

  finish(3),
  // score
}
