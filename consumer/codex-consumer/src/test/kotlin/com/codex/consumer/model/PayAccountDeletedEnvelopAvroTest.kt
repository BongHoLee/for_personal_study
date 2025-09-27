package com.codex.consumer.model

import com.codex.consumer.model.avro.PayAccountDeletedEnvelop
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.apache.avro.io.DecoderFactory
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificDatumWriter
import java.io.ByteArrayOutputStream

class PayAccountDeletedEnvelopAvroTest : DescribeSpec({

    describe("PayAccountDeletedEnvelop Avro 직렬화/역직렬화") {
        it("직렬화 후 역직렬화하면 필드 값이 유지된다") {
            val original = PayAccountDeletedEnvelop(
                "6517f633-eee3-4e3f-bd05-15cf98b26068",
                1_725_000_000_000L,
                912_345_678L,
                "CUSTOMER_REQUEST"
            )

            val encoded = encode(original)
            val decoded = decode(encoded)

            decoded shouldBe original
            decoded.reason shouldBe "CUSTOMER_REQUEST"
        }

        it("Optional 필드인 reason이 null일 때도 정상 동작한다") {
            val original = PayAccountDeletedEnvelop(
                "6517f633-eee3-4e3f-bd05-15cf98b26069",
                1_725_000_000_001L,
                912_345_679L,
                null
            )

            val decoded = decode(encode(original))

            decoded.reason shouldBe null
            decoded.payAccountId shouldBe 912_345_679L
        }
    }
})

private fun encode(record: PayAccountDeletedEnvelop): ByteArray {
    val writer = SpecificDatumWriter(PayAccountDeletedEnvelop::class.java)
    val output = ByteArrayOutputStream()
    val encoder = EncoderFactory.get().binaryEncoder(output, null)
    writer.write(record, encoder)
    encoder.flush()
    return output.toByteArray()
}

private fun decode(bytes: ByteArray): PayAccountDeletedEnvelop {
    val reader = SpecificDatumReader(PayAccountDeletedEnvelop::class.java)
    val decoder = DecoderFactory.get().binaryDecoder(bytes, null)
    return reader.read(null, decoder)
}
