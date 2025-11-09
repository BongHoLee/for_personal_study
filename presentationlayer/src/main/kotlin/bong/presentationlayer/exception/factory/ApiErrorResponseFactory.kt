package bong.presentationlayer.exception.factory

import bong.presentationlayer.dto.common.UserIdentifier
import bong.presentationlayer.dto.response.BaseResponseBody
import bong.presentationlayer.version.ApiVersion

interface ApiErrorResponseFactory {
    val version: ApiVersion

    fun userNotFound(userIdentifier: UserIdentifier, requestId: String?): BaseResponseBody

    fun badRequest(): BaseResponseBody

    fun internalError(): BaseResponseBody
}

