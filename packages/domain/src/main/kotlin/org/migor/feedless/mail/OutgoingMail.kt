package org.migor.feedless.mail

data class OutgoingMail(val from: String, val to: List<String>, val subject: String, val htmlContent: String)
