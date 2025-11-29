package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

@kotlinx.serialization.Serializable
open class JsonPoint : Serializable, Cloneable {

  @SerializedName("x")
  var x: Double = 0.0

  @SerializedName("y")
  var y: Double = 0.0

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JsonPoint) return false

    if (x != other.x) return false
    if (y != other.y) return false

    return true
  }

  override fun hashCode(): Int {
    var result = x.hashCode()
    result = 31 * result + y.hashCode()
    return result
  }

}
