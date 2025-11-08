package com.coursy.auth.repository

import com.coursy.auth.model.User
import org.springframework.data.jpa.domain.Specification
import java.util.*

class UserSpecification {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private val predicates = mutableListOf<Specification<User>>()

        fun id(id: UUID?) = apply {
            id?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<UUID>("id"), it)
                }
            }
        }

        fun platformId(platformId: UUID?) = apply {
            predicates.add { root, _, cb ->
                if (platformId == null) {
                    cb.isNull(root.get<UUID>("platformId"))
                } else {
                    cb.equal(root.get<UUID>("platformId"), platformId)
                }
            }
        }

        fun email(email: String?) = apply {
            email?.let {
                predicates.add { root, _, cb ->
                    cb.equal(root.get<String>("email"), it)
                }
            }
        }

        fun build(): Specification<User> {
            return predicates.reduce { acc, spec -> acc.and(spec) }
        }
    }
}
