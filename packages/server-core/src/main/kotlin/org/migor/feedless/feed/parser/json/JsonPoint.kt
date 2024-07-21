package org.migor.feedless.feed.parser.json

import com.google.gson.annotations.SerializedName
import org.migor.feedless.generated.types.GeoPoint
import java.io.Serializable
import java.util.*

open class JsonPoint : Serializable, Cloneable {

  @SerializedName("x")
  var x: Double = 0.0

  @SerializedName("y")
  var y: Double = 0.0

}
