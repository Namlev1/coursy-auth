package com.coursy.auth.internal.users

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.util.*

@Component
class UsersServiceClient(
    private val webClient: WebClient,
    @param:Value("\${services.users.url}")
    private val authServiceUrl: String
) {
    fun getUser(userId: UUID): UserResponse {
        return webClient
            .get()
            .uri("$authServiceUrl/api/internal/users/$userId")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                Mono.error(RuntimeException("USERS service error: ${response.statusCode()}"))
            }
            .bodyToMono<UserResponse>()
            .blockOptional()
            .orElseThrow { RuntimeException("USERS service returned null for id: $userId") }
    }
}
