package com.dotcom.retail.domain.user

interface UserService {

    fun getByEmail(email: String): User

    fun findByEmail(email: String): User?

    fun create(params: CreateUserParams): User

    fun save(user: User): User
}
