package org.migor.feedless.template

/**
 * Which template variant a send should use.
 *
 * The backend stays generic: it knows no products and never branches on one.
 * It's simply told a variant name and looks up `<template>-<variant>` first,
 * before falling back to `<template>`. A product like upcoming can thus drop
 * its own templates alongside, with no line in the send path aware of it.
 */
@JvmInline
value class TemplateVariant(val value: String) {
  init {
    require(value.isNotBlank()) { "template variant must not be blank" }
  }
}
