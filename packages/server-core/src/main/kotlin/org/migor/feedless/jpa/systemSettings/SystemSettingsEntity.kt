package org.migor.feedless.jpa.systemSettings

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID


@Entity
@Table(name = "t_system_settings")
open class SystemSettingsEntity : EntityWithUUID() {

  @Column(name = "name", nullable = false, unique = true)
  open lateinit var name: String

  @Column(name = "value_int")
  open var valueInt: Int? = null

  @Column(name = "value_bool")
  open var valueBoolean: Boolean? = null

  @Column(name = "value_text")
  open var valueString: String? = null

}
