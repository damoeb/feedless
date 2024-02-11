package org.migor.feedless.util

import java.io.InputStream

object SafeGuards {

  fun respectMaxSize(stream: InputStream, maxBytes: Int = 4000000): ByteArray {
    val array = stream.readNBytes(maxBytes)
    if (stream.available() > 0) {
      throw IllegalArgumentException("maxBytes reached (${stream.available()} left)")
    }
    return array
  }

}
