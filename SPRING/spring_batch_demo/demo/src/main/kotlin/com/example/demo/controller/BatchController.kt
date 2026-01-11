package com.example.demo.controller

import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * 배치 Job 실행을 위한 REST API 컨트롤러
 *
 * ## JobLauncher 개념
 * - Job을 실행하기 위한 진입점
 * - Job과 JobParameters를 받아 실행
 * - JobExecution(실행 결과)을 반환
 *
 * ## JobParameters
 * - Job 실행 시 전달되는 파라미터
 * - Job + JobParameters = JobInstance (고유한 실행 단위)
 * - 같은 JobParameters로 재실행하면 이미 완료된 Job이라 오류 발생
 *   → RunIdIncrementer 사용으로 해결
 *
 * ## 실행 방법
 * ```bash
 * curl -X POST http://localhost:8080/api/batch/run
 * ```
 */
@RestController
@RequestMapping("/api/batch")
class BatchController(
    private val jobLauncher: JobLauncher,
    private val importCustomerJob: Job
) {

    /**
     * 배치 Job 실행 API
     *
     * @return Job 실행 결과 (jobId, status, 시작/종료 시간)
     */
    @PostMapping("/run")
    fun runBatchJob(): ResponseEntity<Map<String, Any>> {
        // JobParameters 생성 (현재 시간을 파라미터로 추가)
        val jobParameters = JobParametersBuilder()
            .addString("startTime", LocalDateTime.now().toString())
            .toJobParameters()

        // Job 실행
        val jobExecution = jobLauncher.run(importCustomerJob, jobParameters)

        // 결과 반환
        return ResponseEntity.ok(
            mapOf(
                "jobId" to (jobExecution.jobId ?: -1),
                "jobName" to (jobExecution.jobInstance?.jobName ?: "unknown"),
                "status" to jobExecution.status.name,
                "startTime" to (jobExecution.startTime?.toString() ?: "N/A"),
                "endTime" to (jobExecution.endTime?.toString() ?: "N/A"),
                "exitStatus" to jobExecution.exitStatus.exitCode
            )
        )
    }
}
