package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("xy")
open class ClickPositionActionEntity : BrowserActionEntity() {

  @Column
//  @Min(0)
  open var x: Int = 0

  @Column
//  @Min(0)
  open var y: Int = 0
}
