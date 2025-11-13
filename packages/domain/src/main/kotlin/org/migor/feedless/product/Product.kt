package org.migor.feedless.product

import org.migor.feedless.Vertical

data class Product(val name: String, val saas: Boolean, val partOf: Vertical?)
