package com.codex.consumer.support

import com.codex.consumer.exception.MessageDecodingException
import com.codex.consumer.model.avro.PayAccountDeletedEnvelop
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PayAccountDeletedDecoder {

    private val reader = SpecificDatumReader(PayAccountDeletedEnvelop::class.java)

    fun decode(payload: ByteArray): PayAccountDeletedEnvelop {
        return runCatching {
            val decoder = DecoderFactory.get().binaryDecoder(payload, null)
            reader.read(null, decoder)
        }.getOrElse { throwable ->
            log.error("Failed to decode pay-account deletion payload", throwable)
            throw MessageDecodingException("Failed to decode pay-account deletion payload", throwable)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PayAccountDeletedDecoder::class.java)
    }
}
