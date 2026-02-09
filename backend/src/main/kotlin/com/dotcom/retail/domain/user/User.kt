package com.dotcom.retail.domain.user

import com.dotcom.retail.common.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.UUID

@Table(name = "users")
@Entity
class User(

    @Id
    val id: UUID = UUID.randomUUID(),

    var email: String,
    private var passwordHash: String? = null,

    var displayName: String,

    var twoFactorSecret: String? = null,
    var twoFactorEnabled: Boolean = false


    ) : BaseEntity(), UserDetails {

    override fun toString(): String {
        return "User(id=$id, email='$email', passwordHash=$passwordHash, displayName='$displayName', ${super.toString()})"
    }
    override fun getAuthorities(): Collection<GrantedAuthority?> = emptyList()
    override fun getUsername(): String = email
    override fun getPassword(): String? = passwordHash
}
