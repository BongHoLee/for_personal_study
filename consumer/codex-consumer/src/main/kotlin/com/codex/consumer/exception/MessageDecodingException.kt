package com.codex.consumer.exception

class MessageDecodingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
