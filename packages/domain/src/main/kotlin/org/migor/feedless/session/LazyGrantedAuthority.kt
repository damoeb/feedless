package org.migor.feedless.session

import org.springframework.security.core.GrantedAuthority

class LazyGrantedAuthority(private val authority: String, internal val payload: String) : GrantedAuthority {
  override fun getAuthority(): String {
    return authority
  }

}

fun LazyGrantedAuthority.getPayload(): String {
  return payload;
}
