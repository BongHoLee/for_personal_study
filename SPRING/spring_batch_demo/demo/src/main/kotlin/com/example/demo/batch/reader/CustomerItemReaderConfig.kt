package com.example.demo.batch.reader

import com.example.demo.dto.CustomerCsvRow
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.util.concurrent.atomic.AtomicInteger

/**
 * ItemReader 설정
 *
 * CSV 파일을 읽어서 CustomerCsvRow로 매핑합니다.
 * type 필드에 따라 EXISTING(Primary DB) 또는 NEW(Secondary DB)로 분류됩니다.
 *
 * ## Step 분리 방식
 * 각 Step이 동일한 CSV 파일을 독립적으로 읽어야 하므로
 * 팩토리 메서드를 통해 새로운 Reader 인스턴스를 생성합니다.
 */
@Configuration
class CustomerItemReaderConfig {

    private val readerCounter = AtomicInteger(0)

    /**
     * 새로운 FlatFileItemReader 인스턴스를 생성합니다.
     *
     * 각 Step에서 독립적으로 CSV 파일을 처음부터 읽을 수 있도록
     * 매번 새 인스턴스를 반환합니다.
     */
    fun createCustomerItemReader(): FlatFileItemReader<CustomerCsvRow> {
        val readerId = readerCounter.incrementAndGet()
        return FlatFileItemReaderBuilder<CustomerCsvRow>()
            .name("customerItemReader-$readerId")
            .resource(ClassPathResource("data/customers.csv"))
            .linesToSkip(1)  // 헤더 건너뛰기
            .delimited()
            .names("type", "firstName", "lastName", "email", "age")
            .fieldSetMapper(BeanWrapperFieldSetMapper<CustomerCsvRow>().apply {
                setTargetType(CustomerCsvRow::class.java)
            })
            .build()
    }
}
