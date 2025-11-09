package bong.presentationlayer.controller

import bong.presentationlayer.dto.request.V1RequestBody
import bong.presentationlayer.dto.request.V2RequestBody
import bong.presentationlayer.dto.common.UserIdentifier
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest(
    private val mockMvc: MockMvc
) : FunSpec({

    val json = Json { ignoreUnknownKeys = true }

    test("V1 API - 정상적인 요청 처리") {
        // given
        val requestBody = V1RequestBody(
            requestId = "test-tx-001",
            requestDateTime = "2025-11-09T10:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-12345")  // Mock에 등록된 사용자
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/process")
                .header("service-id", "test-service-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.encodeToString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.body.requestId").value("test-tx-001"))
            .andExpect(jsonPath("$.body.statusCode").value(200))
            .andExpect(jsonPath("$.body.data").value("Processing completed for user: user-001"))
            .andExpect(jsonPath("$.serviceId").value("test-service-001"))
    }

    test("V1 API - 회원이 아닌 경우 404 에러") {
        // given
        val requestBody = V1RequestBody(
            requestId = "test-tx-002",
            requestDateTime = "2025-11-09T11:00:00",
            userIdentifier = UserIdentifier.CI(value = "ci-unknown")  // 등록되지 않은 사용자
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/process")
                .header("service-id", "test-service-002")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.encodeToString(requestBody))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.statusCode").value(404))
            .andExpect(jsonPath("$.message").value("회원 아님"))
            .andExpect(jsonPath("$.userIdentifier").value("ci-unknown"))
    }

    test("V2 API - DI 타입으로 정상 처리") {
        // given
        val requestBody = V2RequestBody(
            requestId = "test-tx-003",
            requestDateTime = "2025-11-09T12:00:00",
            userIdentifier = UserIdentifier.DI(value = "di-67890")  // Mock에 등록된 사용자
        )

        // when & then
        mockMvc.perform(
            post("/api/v2/process")
                .header("service-id", "test-service-003")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.encodeToString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.body.requestId").value("test-tx-003"))
            .andExpect(jsonPath("$.body.statusCode").value(200))
            .andExpect(jsonPath("$.body.data").value("Processing completed for user: user-002"))
            .andExpect(jsonPath("$.serviceId").value("test-service-003"))
    }

    test("V2 API - Email 타입으로 정상 처리") {
        // given
        val requestBody = V2RequestBody(
            requestId = "test-tx-004",
            requestDateTime = "2025-11-09T13:00:00",
            userIdentifier = UserIdentifier.Email(value = "test@example.com")  // Mock에 등록된 사용자
        )

        // when & then
        mockMvc.perform(
            post("/api/v2/process")
                .header("service-id", "test-service-004")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.encodeToString(requestBody))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.body.statusCode").value(200))
            .andExpect(jsonPath("$.body.data").value("Processing completed for user: user-003"))
    }

    test("V2 API - 회원이 아닌 경우 404 에러") {
        // given
        val requestBody = V2RequestBody(
            requestId = "test-tx-005",
            requestDateTime = "2025-11-09T14:00:00",
            userIdentifier = UserIdentifier.Email(value = "unknown@example.com")
        )

        // when & then
        mockMvc.perform(
            post("/api/v2/process")
                .header("service-id", "test-service-005")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.encodeToString(requestBody))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("회원 아님"))
    }
})
