package com.dotcom.retail.domain.user

import com.dotcom.retail.common.exception.AlreadyExistsException
import com.dotcom.retail.common.exception.NotFoundException
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
        return userRepository.findByIdOrNull(id) ?: throw NotFoundException(User::class.simpleName, id)
    }

    fun findById(id: UUID): User? {
        return userRepository.findByIdOrNull(id)
    }

    fun getByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw NotFoundException(User::class.simpleName, email)
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun create(params: CreateUserParams): User {
        val email = params.email
        if (userRepository.existsByEmail(email)) throw AlreadyExistsException(User::class.simpleName, email)

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