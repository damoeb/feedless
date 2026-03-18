package org.migor.feedless.karma

interface KarmaChangeRepository {

  fun append(karmaChange: KarmaChange): KarmaChange

}
