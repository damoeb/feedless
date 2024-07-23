package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class JsonPoint : Serializable, Cloneable {

  @SerializedName("x")
  var x: Double = 0.0

  @SerializedName("y")
  var y: Double = 0.0

}
