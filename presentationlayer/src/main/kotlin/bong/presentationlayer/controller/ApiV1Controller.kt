package bong.presentationlayer.controller

import bong.presentationlayer.context.RequestContext
import bong.presentationlayer.domain.UserId
import bong.presentationlayer.dto.request.V1RequestBody
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.service.UserIdResolver
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1")
class ApiV1Controller(
    private val userIdResolver: UserIdResolver,
    private val requestContext: RequestContext
) {
    private val logger = LoggerFactory.getLogger(ApiV1Controller::class.java)

    @PostMapping("/process", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun processV1(@RequestBody requestBody: V1RequestBody): V1ResponseBody {
        val serviceId = requestContext.serviceId ?: "unknown"
        logger.info("Processing V1 request - serviceId: $serviceId, requestId: ${requestBody.requestId}")

        requestContext.requestBody = requestBody

        val userId: UserId = userIdResolver.resolve(requestBody.userIdentifier, requestBody.requestId)
        logger.info("Resolved userId: $userId for userIdentifier: ${requestBody.userIdentifier.value}")

        return V1ResponseBody.from(
            request = requestBody,
            responseDateTime = Instant.now().toString(),
            statusCode = 200,
            data = "Processing completed for user: $userId"
        )
    }
}
