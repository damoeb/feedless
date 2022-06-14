package org.migor.rich.rss.util

import java.io.InputStream

object SafeGuards {
  fun guardedToString(stream: InputStream, maxBytes: Int = 1000000): String {
    val array = stream.readNBytes(maxBytes)
    if (stream.available() > 0) {
      throw RuntimeException("maxBytes reached")
    }
    return String(array)
  }

  fun respectMaxSize(stream: InputStream, maxBytes: Int = 1000000): ByteArray {
    val array = stream.readNBytes(maxBytes)
    if (stream.available() > 0) {
      throw RuntimeException("maxBytes reached")
    }
    return array
  }

}
