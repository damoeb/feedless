package org.migor.feedless.session

class AuthUserNotFoundException(message: String) : RuntimeException(message)

class AuthCredentialsException(message: String) : RuntimeException(message)
