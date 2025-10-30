package com.dotcom.retail.user

import com.dotcom.retail.auth.RegisterRequest

interface UserService {

    fun getByEmail(email: String): User?

    fun findByEmail(email: String): User?

    fun create(request: RegisterRequest): User

    fun save(user: User): User

    fun toUserResponse(user: User): UserResponse

//    fun getUserByEmail(username: String): User {
//        return userRepository.findByEmail(username) ?: throw UsernameNotFoundException("$username not found")
//    }
}
