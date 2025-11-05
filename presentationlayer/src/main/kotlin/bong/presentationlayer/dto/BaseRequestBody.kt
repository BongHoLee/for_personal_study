package bong.presentationlayer.dto

/**
 * 모든 Request Body의 기본 인터페이스
 *
 * ApiMetadata를 상속하여 API 호출의 공통 메타데이터를 포함합니다.
 * V1RequestBody, V2RequestBody 등 모든 요청 타입의 상위 인터페이스입니다.
 */
interface BaseRequestBody : ApiMetadata
