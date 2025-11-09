package bong.presentationlayer.exception.factory

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.dto.response.V1ResponseBody
import bong.presentationlayer.version.ApiVersion
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class V1ApiErrorResponseFactory : ApiErrorResponseFactory {
    override val version: ApiVersion = ApiVersion.V1

    override fun userNotFound(userIdentifier: UserIdentifier, requestId: String?): BaseResponseBody =
        V1ResponseBody.error(
            requestId = requestId,
            statusCode = HttpStatus.NOT_FOUND.value(),
            userIdentifier = userIdentifier as UserIdentifier.CI
        )

    override fun badRequest(): BaseResponseBody =
        V1ResponseBody(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            userIdentifier = null,
            requestId = null,
            requestDateTime = null,
            responseDateTime = null,
            data = null
        )

    override fun internalError(): BaseResponseBody =
        V1ResponseBody(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            userIdentifier = null,
            requestId = null,
            requestDateTime = null,
            responseDateTime = null,
            data = null
        )
}

