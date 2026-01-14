package com.notia.app.core.utils

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString
