package bong.presentationlayer.exception.factory

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.dto.response.V2ResponseBody
import bong.presentationlayer.version.ApiVersion
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class V2ApiErrorResponseFactory : ApiErrorResponseFactory {
    override val version: ApiVersion = ApiVersion.V2

    override fun userNotFound(userIdentifier: UserIdentifier, requestId: String?): BaseResponseBody =
        V2ResponseBody.error(
            requestId = requestId,
            statusCode = HttpStatus.NOT_FOUND.value(),
            userIdentifier = userIdentifier
        )

    override fun badRequest(): BaseResponseBody =
        V2ResponseBody(
            statusCode = HttpStatus.BAD_REQUEST.value(),
            userIdentifier = null,
            requestId = null,
            requestDateTime = null,
            responseDateTime = null,
            data = null
        )

    override fun internalError(): BaseResponseBody =
        V2ResponseBody(
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            userIdentifier = null,
            requestId = null,
            requestDateTime = null,
            responseDateTime = null,
            data = null
        )
}

