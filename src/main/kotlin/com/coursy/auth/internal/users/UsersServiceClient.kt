package com.coursy.auth.internal.users

import com.coursy.auth.model.Role
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
    fun getUserRole(userId: UUID): Role? {
        return webClient
            .get()
            .uri("$authServiceUrl/api/internal/users/$userId/role")
            .header("Content-Type", "application/json")
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                Mono.error(RuntimeException("Users service error: ${response.statusCode()}"))
            }
            .bodyToMono<Role>()
            .block()
    }
}
