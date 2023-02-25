package org.migor.rich.rss.api

//import org.springframework.security.oauth2.core.http

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.migor.rich.rss.data.jpa.models.OneTimePasswordEntity
import org.migor.rich.rss.data.jpa.repositories.OneTimePasswordDAO
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.messaging.handler.annotation.Header
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.util.Assert
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

data class AuthorizationCode(
  val nonce: String
)

@RestController
class OAuthEndpoint {

  private val log = LoggerFactory.getLogger(OAuthEndpoint::class.simpleName)

//  @Autowired
//  lateinit var oAuth2AuthorizedClientService: AuthorizedClientServiceOAuth2AuthorizedClientManager

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var oneTimePasswordDAO: OneTimePasswordDAO

  @Autowired
  lateinit var clientRegistration: ClientRegistration

  val expectedGrantType = "authorization_code"

  @PostMapping("/api/oauth2/token")
  fun authorizationCodeRequest(
    @Header(ApiParams.corrId, required = false) corrId: String?,
    @RequestParam("grant_type") grantType: String,
    @RequestParam("code") code: String,
    @RequestParam("client_id") clientId: String,
    httpRequest: HttpServletRequest
  ): ResponseEntity<Map<String, Any>> {
    // https://www.oauth.com/oauth2-servers/access-tokens/authorization-code-request/
    Assert.notNull(code, "code cannot be null")
    Assert.notNull(clientId, "client_id cannot be null")
    Assert.notNull(grantType, "grant_type cannot be null")
    Assert.isTrue(grantType == expectedGrantType, "grant_type must be set to $expectedGrantType")

    log.info("token ${httpRequest.parameterMap.entries.joinToString()}")
    val token = authService.encodeJwt(emptyMap())
      .tokenValue
    log.info("token $token")

    // https://www.oauth.com/oauth2-servers/access-tokens/access-token-response/
    val response = mapOf(
      OAuth2ParameterNames.ACCESS_TOKEN to token,
      OAuth2ParameterNames.TOKEN_TYPE to OAuth2AccessToken.TokenType.BEARER.value,
      OAuth2ParameterNames.EXPIRES_IN to Duration.ofMinutes(10).seconds, // https://www.rfc-editor.org/rfc/rfc6749#section-5.1
    )
    return ResponseEntity.ok().body(response)
  }

  @GetMapping("/api/oauth2/authorization")
  fun authorizationRequest(
    @Header(ApiParams.corrId, required = false) corrId: String?,
    @RequestParam("response_type") responseType: String,
    @RequestParam("client_id") clientId: String,
    @RequestParam("state") state: String,
    @RequestParam("redirect_uri") redirectUri: String,
    response: HttpServletResponse
  ): ResponseEntity<Void> {
    // https://www.oauth.com/oauth2-servers/authorization/the-authorization-request/
    Assert.notNull(responseType, "response_type cannot be null")
    Assert.notNull(clientId, "client_id cannot be null")
    Assert.notNull(state, "state cannot be null")
    Assert.notNull(redirectUri, "redirect_uri cannot be null")

    log.info("authorization responseType: $responseType, clientId: $clientId, state: $state, redirectUri: $redirectUri")
    val otp = OneTimePasswordEntity()
    otp.password = UUID.randomUUID().toString()
    otp.validUntil = Timestamp.valueOf(LocalDateTime.now().plusMinutes(3))
    oneTimePasswordDAO.save(otp)

    val authorizationCode = AuthorizationCode(nonce = otp.password)
    val json = JsonUtil.gson.toJson(authorizationCode)
    val encoded = URLEncoder.encode(json, StandardCharsets.UTF_8)

    // https://www.oauth.com/oauth2-servers/authorization/the-authorization-response/
    return ResponseEntity
      .status(302)
      .header("location", "$redirectUri/?code=$encoded&state=$state")
      .build()
  }

  @GetMapping("/api/oauth2/code")
  fun handleCode(
    @Header(ApiParams.corrId, required = false) corrId: String?,
    @RequestParam("code") code: String,
    httpResponse: HttpServletResponse
  ) {
    log.info("handleCode $code")

    val authorizationCode = JsonUtil.gson.fromJson(code, AuthorizationCode::class.java)

    val authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
      .attributes(mapOf("authorizationCode" to authorizationCode))
      .authorizationUri(clientRegistration.providerDetails.authorizationUri)
      .clientId(clientRegistration.clientId)
      .build()
    val authorizationResponse =
      OAuth2AuthorizationResponse.success(code).redirectUri(clientRegistration.redirectUri).build()
    val authorizationExchange = OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse)
    val grantRequest = OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange)
    val client = DefaultAuthorizationCodeTokenResponseClient()
    val tokenResponse = client.getTokenResponse(grantRequest)
    log.info("${tokenResponse}")

    val authCookie = Cookie("accessToken", tokenResponse.accessToken.tokenValue)
    authCookie.isHttpOnly = true
    authCookie.domain = propertyService.domain
    authCookie.maxAge = Duration.ofMinutes(10).seconds.toInt()

    httpResponse.addCookie(authCookie)
    httpResponse.sendRedirect("http://localhost:8080")
  }
}
