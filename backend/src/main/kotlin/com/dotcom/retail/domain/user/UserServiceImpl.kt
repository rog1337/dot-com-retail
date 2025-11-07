package com.dotcom.retail.domain.user

import com.dotcom.retail.common.exception.EmailAlreadyRegisteredException
import com.dotcom.retail.common.exception.EmailNotFoundException
import com.dotcom.retail.domain.auth.dto.RegisterRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    companion object {
        const val DEFAULT_DISPLAY_NAME = "Shopper"
    }

    override fun getByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw EmailNotFoundException(email)
    }

    override fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun create(params: CreateUserParams): User {
        val email = params.email
        if (userRepository.existsByEmail(email)) throw EmailAlreadyRegisteredException(email)

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

    override fun save(user: User): User {
        return userRepository.save(user)
    }
}