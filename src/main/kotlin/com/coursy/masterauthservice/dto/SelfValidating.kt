package com.coursy.masterauthservice.dto

import arrow.core.Either

interface SelfValidating<A, B> {
    fun validate(): Either<A, B>
}
