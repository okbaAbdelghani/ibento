package com.okbatech.smartevents.core.common

import java.security.MessageDigest

/** Local-only mock hashing — there is no real backend/credential store behind this build yet. */
fun hashPassword(value: String): String =
    MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        .joinToString("") { "%02x".format(it) }
