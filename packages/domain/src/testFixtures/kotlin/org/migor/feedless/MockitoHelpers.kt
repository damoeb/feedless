package org.migor.feedless

import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito

fun <T> anyList(): List<T> = Mockito.anyList<T>()
fun <T> any(type: Class<T>): T = Mockito.any<T>(type)
fun <T> any2(): T = ArgumentMatchers.any<T>()
fun <T> anyOrNull2(): T? = ArgumentMatchers.any<T?>()
fun <T> argThat(matcher: ArgumentMatcher<T>): T = Mockito.argThat<T>(matcher)
fun <T> anyOrNull(type: Class<T>): T? = Mockito.any<T?>(type)
fun <T> eq(type: T): T = Mockito.eq<T>(type) ?: type
