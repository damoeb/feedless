package org.migor.feedless.http

/** A field that passed schema validation but breaks an endpoint rule; answers 400 VALIDATION_ERROR like bean validation. */
class InvalidFieldException(val field: String, override val message: String) : RuntimeException(message)
