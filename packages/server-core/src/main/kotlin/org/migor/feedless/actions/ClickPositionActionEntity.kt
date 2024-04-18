package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.util.*

@Entity
//@Table(
//  name = "t_action_click_position"
//)
@DiscriminatorValue("xy")
open class ClickPositionActionEntity : ScrapeActionEntity() {

  @Column
  open var x: Int = 0

  @Column
  open var y: Int = 0
}
