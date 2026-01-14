package com.notia.app.core.utils

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
