package com.dotcom.retail.user

import com.dotcom.retail.auth.RegisterRequest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService {


    override fun getByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    override fun create(request: RegisterRequest): User {
        val email = request.email
        if (userRepository.existsByEmail(email)) throw EmailAlreadyRegisteredException(email)

        val hashedPassword = passwordEncoder.encode(request.password)
        val user = User(
            email = email,
            passwordHash = hashedPassword,
            displayName = request.displayName,
        )
        return save(user)
    }

    override fun save(user: User): User {
        return userRepository.save(user)
    }

    override fun toUserResponse(user: User): UserResponse = UserResponse(
        id = user.id.toString(),
        email = user.email,
        displayName = user.displayName,
    )
}