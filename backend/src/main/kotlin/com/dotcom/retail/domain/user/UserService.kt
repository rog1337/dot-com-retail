package com.dotcom.retail.domain.user

import java.util.UUID

interface UserService {

    fun getById(id: UUID): User

    fun findById(id: UUID): User?

    fun getByEmail(email: String): User

    fun findByEmail(email: String): User?

    fun create(params: CreateUserParams): User

    fun save(user: User): User
}
