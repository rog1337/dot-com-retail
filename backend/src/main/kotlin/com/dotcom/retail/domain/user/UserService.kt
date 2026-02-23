package com.dotcom.retail.domain.user

import com.dotcom.retail.common.exception.AppException
import com.dotcom.retail.common.exception.UserError
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    companion object {
        const val DEFAULT_DISPLAY_NAME = "Shopper"
    }

    fun getById(id: UUID): User {
        return userRepository.findByIdOrNull(id) ?: throw AppException(UserError.USER_NOT_FOUND.withIdentifier(id))
    }

    fun findById(id: UUID): User? {
        return userRepository.findByIdOrNull(id)
    }

    fun getByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw AppException(UserError.USER_NOT_FOUND.withIdentifier(email))
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun create(params: CreateUserParams): User {
        val email = params.email
        if (userRepository.existsByEmail(email)) throw AppException(UserError.USER_ALREADY_EXISTS.withIdentifier(email))

        val hashedPasswordOrNull =
            if (params.password != null )
                passwordEncoder.encode(params.password)
            else null

        val user = User(
            email = email,
            passwordHash = hashedPasswordOrNull,
            displayName = params.displayName ?: DEFAULT_DISPLAY_NAME,
            // todo picture?
        )
        return save(user)
    }

    fun save(user: User): User {
        return userRepository.save(user)
    }
}