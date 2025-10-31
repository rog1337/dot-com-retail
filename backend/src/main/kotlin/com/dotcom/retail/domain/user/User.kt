package com.dotcom.retail.domain.user

import com.dotcom.retail.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Table(name = "users")
@Entity
class User(

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    var email: String,
    private var passwordHash: String? = null,

    var displayName: String,

    var accessToken: String? = null,
    var refreshToken: String? = null,

    ) : BaseEntity(), UserDetails {

    override fun toString(): String {
        return super.toString() + formatToString(mapOf(
            "id" to id,
            "email" to email,
            "displayName" to displayName,
            "accessToken" to accessToken,
            "refreshToken" to refreshToken,
        ))
    }

    override fun getAuthorities(): Collection<GrantedAuthority?> = emptyList()
    override fun getUsername(): String = email
    override fun getPassword(): String? = passwordHash

    companion object
}
