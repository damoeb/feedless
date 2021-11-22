package org.migor.rss.rich.service

import org.asynchttpclient.BoundRequestBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URL

@Service
class BypassConsentService {
  private val log = LoggerFactory.getLogger(BypassConsentService::class.simpleName)

  fun tryBypassConsent(cid: String, request: BoundRequestBuilder, url: String) {
    val domain = URL(url).host
    when (domain) {
      "www.youtube.com" -> setCookie(
        cid,
        request,
        "PREF=tz=Europe.Zurich&f6=40000000; CONSENT=YES+yt.406819520.en+FX+671"
      )
      "www.derstandard.at" -> setCookie(
        cid,
        request,
        "__adblocker=false; _sp_v1_uid=1:192:eee5158c-dce7-43d5-b52a-606bcd10e895; _sp_v1_data=2:396765:1636645964:0:3:0:3:0:0:_:-1; _sp_v1_ss=1:H4sIAAAAAAAAAItWqo5RKimOUbLKK83J0YlRSkVil4AlqmtrlXSGk7JoYtTHkmIQiJEHYhjg1ofbwFgAuNVQ-YUBAAA%3D; _sp_v1_opt=1:login|true:last_id|11:; _sp_v1_consent=1!1:1:1:0:0:0; _sp_v1_csv=null; _sp_v1_lt=1:; tcfs=1; DSGVO_ZUSAGE_V1=true; __pnahc=3"
      )
    }
  }

  private fun setCookie(cid: String, request: BoundRequestBuilder, cookies: String) {
    this.log.info("[${cid}] Setting cookie to bypass consent")
    request.setHeader("Cookie", cookies)
  }
}
