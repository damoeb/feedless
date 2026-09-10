package org.migor.feedless.http

/**
 * A request body [field] that passed schema validation but breaks a rule of the endpoint itself.
 * Answers 400 `VALIDATION_ERROR` with a per-field error, like a bean-validation failure.
 */
class InvalidFieldException(val field: String, override val message: String) : RuntimeException(message)
