package bong.presentationlayer.version

import org.springframework.stereotype.Component

@Component
class ApiVersionResolver {
    fun resolve(uri: String): ApiVersion = when {
        uri.contains("/v1/") -> ApiVersion.V1
        uri.contains("/v2/") -> ApiVersion.V2
        else -> ApiVersion.UNKNOWN
    }
}
